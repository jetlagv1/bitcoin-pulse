"""Validate the signed publishing bundle and a locally generated universal APK."""
from pathlib import Path
import hashlib
import json
import os
import re
import struct
import subprocess
import xml.etree.ElementTree as ET
import zipfile

root=Path(__file__).resolve().parents[1]
gradle=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
expected_version_code=int(re.search(r'versionCode\s*=\s*(\d+)',gradle).group(1))
expected_version_name=re.search(r'versionName\s*=\s*"([^"]+)"',gradle).group(1)
build=root.parent/'.tools/android-build'
jdk=next(p for p in (build/'jdk').iterdir() if p.is_dir())
java=str(jdk/'bin/java.exe')
bundletool=build/'bundletool-all-1.18.3.jar'
bundle=root/'app/build/outputs/bundle/release/app-release.aab'
out=root/'deliverables/play-store'
out.mkdir(parents=True,exist_ok=True)
reports=build/'play-verification'
reports.mkdir(exist_ok=True)
def run(args):
    result=subprocess.run(args,capture_output=True,text=True,encoding='utf-8',errors='replace')
    if result.returncode:
        raise RuntimeError(result.stdout+result.stderr)
    return result.stdout+result.stderr

signature=run([str(jdk/'bin/jarsigner.exe'),'-verify',str(bundle)])
(reports/'signature.txt').write_text(signature,encoding='utf-8')
with zipfile.ZipFile(bundle) as archive:
    assert any(n.startswith('META-INF/') and n.endswith('.RSA') for n in archive.namelist()),'Bundle has no RSA signature'
    assert any(n.startswith('META-INF/') and n.endswith('.SF') for n in archive.namelist()),'Bundle has no signing manifest'
    assert 'base/assets/privacy-policy.txt' in archive.namelist()
run([java,'-jar',str(bundletool),'validate',f'--bundle={bundle}'])
xml=run([java,'-jar',str(bundletool),'dump','manifest',f'--bundle={bundle}','--module=base'])
# JVM diagnostics, if any, are on stderr after the valid manifest.
manifest_text=xml[xml.index('<manifest'):xml.index('</manifest>')+len('</manifest>')]
(reports/'manifest.xml').write_text(manifest_text,encoding='utf-8')
manifest=ET.fromstring(manifest_text)
android='{http://schemas.android.com/apk/res/android}'
assert manifest.attrib['package']=='com.buildapptest.bitcoinpulse'
assert int(manifest.attrib[android+'versionCode'])==expected_version_code
assert manifest.attrib[android+'versionName']==expected_version_name
assert manifest.find('uses-sdk').attrib[android+'targetSdkVersion']=='36'
assert manifest.find('application').attrib.get(android+'debuggable','false')=='false'
config=run([java,'-jar',str(bundletool),'dump','config',f'--bundle={bundle}'])
(reports/'bundle-config.json').write_text(config,encoding='utf-8')
assert 'PAGE_ALIGNMENT_16K' in config,'Bundle does not request 16KB alignment'
credentials=json.loads((build/'release-signing/upload-key.json').read_text(encoding='utf-8'))
password_file=build/'release-signing/temporary-bundletool-password.txt'
password_file.write_text(credentials['password'],encoding='utf-8')
apks=reports/'universal.apks'
try:
    run([java,'-jar',str(bundletool),'build-apks',f'--bundle={bundle}',f'--output={apks}',
        '--mode=universal','--overwrite',f"--ks={credentials['storeFile']}",f"--ks-key-alias={credentials['keyAlias']}",
        f'--ks-pass=file:{password_file}',f'--key-pass=file:{password_file}'])
finally:
    password_file.unlink(missing_ok=True)
with zipfile.ZipFile(apks) as archive:
    apk=reports/'universal.apk'
    apk.write_bytes(archive.read('universal.apk'))
zipalign=build/'sdk/build-tools/35.0.0/zipalign.exe'
run([str(zipalign),'-c','-P','16','4',str(apk)])
native=[]
with zipfile.ZipFile(apk) as archive:
    for name in archive.namelist():
        if not name.endswith('.so'): continue
        data=archive.read(name)
        assert data[:4]==b'\x7fELF'
        endian='<' if data[5]==1 else '>'
        is64=data[4]==2
        offset=struct.unpack_from(endian+('Q' if is64 else 'I'),data,32 if is64 else 28)[0]
        size,count=struct.unpack_from(endian+'HH',data,54 if is64 else 42)
        alignments=[]
        for i in range(count):
            start=offset+i*size
            if struct.unpack_from(endian+'I',data,start)[0]==1:
                alignment=struct.unpack_from(endian+('Q' if is64 else 'I'),data,start+(48 if is64 else 28))[0]
                alignments.append(alignment)
        assert alignments and min(alignments)>=16384,f'Native library lacks 16KB load alignment: {name}, {alignments}'
        native.append(name)
report={
    'applicationId':manifest.attrib['package'],'versionName':manifest.attrib[android+'versionName'],
    'versionCode':expected_version_code,'minSdk':26,'targetSdk':36,'debuggable':False,
    'signature':'verified RSA upload signature; self-signed upload certificate is expected',
    'bundletool':'1.18.3 validation and universal APK generation passed',
    'pageAlignment':'16KB bundle configuration, APK ZIP alignment and ELF PT_LOAD checks passed',
    'nativeLibraries':native,'sizeBytes':bundle.stat().st_size,
    'sha256':hashlib.sha256(bundle.read_bytes()).hexdigest(),
    'deviceTesting':'Not executed on a physical device or emulator; submit first to Play internal testing.'
}
(out/'VERIFICATION.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
