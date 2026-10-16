"""Package the verified AAB and public publication materials, excluding all signing credentials."""
from pathlib import Path
import hashlib
import html
import json
import shutil
import zipfile

root=Path(__file__).resolve().parents[1]
out=root/'deliverables/play-store'
report=json.loads((out/'VERIFICATION.json').read_text(encoding='utf-8'))
version=report['versionName']
version_code=report['versionCode']
source_bundle=root/'app/build/outputs/bundle/release/app-release.aab'
assert hashlib.sha256(source_bundle.read_bytes()).hexdigest()==report['sha256'],'Re-verify the current AAB before packaging.'
artifact=out/f'Bitcoin-Pulse-{version}-vc{version_code}-release.aab'
shutil.copy2(source_bundle,artifact)
for name in ('PLAY-STORE.md','STORE-LISTING-NL.md'):
    shutil.copy2(root/name,out/name)
policy=(root/'app/src/main/assets/privacy-policy.txt').read_text(encoding='utf-8')
(out/'privacy-policy.html').write_text('<!doctype html><html lang="nl"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Privacyverklaring — Bitcoin Pulse</title><style>body{font:16px/1.6 system-ui,sans-serif;max-width:48rem;margin:2rem auto;padding:0 1rem;white-space:pre-wrap}</style><body>'+html.escape(policy)+'</body></html>',encoding='utf-8')
excluded={'.gradle','.kotlin','.idea','.superdesign','build','deliverables','__pycache__','.git'}
archive=out/f'Bitcoin-Pulse-{version}-source.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as bundle:
    for p in sorted(root.rglob('*')):
        rel=p.relative_to(root)
        if not p.is_file() or set(rel.parts)&excluded: continue
        if p.name in {'local.properties','keystore.properties','upload-key.json'} or p.suffix.lower() in {'.jks','.p12','.keystore'}: continue
        bundle.write(p,Path('Bitcoin-Pulse')/rel)
# This ZIP is the phone-friendly handoff; it contains no private key or password.
public_files=[artifact,out/'PLAY-STORE.md',out/'STORE-LISTING-NL.md',out/'privacy-policy.html',out/'upload-certificate.pem',out/'VERIFICATION.json']
checksums=out/'SHA256SUMS.txt'
checksums.write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in public_files),encoding='utf-8')
package=root/f'deliverables/Bitcoin-Pulse-{version}-Play-Store.zip'
with zipfile.ZipFile(package,'w',zipfile.ZIP_DEFLATED) as bundle:
    for p in public_files+[checksums]: bundle.write(p,p.name)
secret=json.loads((root.parent/'.tools/android-build/release-signing/upload-key.json').read_text(encoding='utf-8'))['password'].encode()
with zipfile.ZipFile(archive) as bundle:
    assert all(secret not in bundle.read(name) for name in bundle.namelist()),'Credential unexpectedly present in source archive'
for p in (artifact,package):
    print(f'{p.name}: {p.stat().st_size} bytes; SHA256 {hashlib.sha256(p.read_bytes()).hexdigest()}')
