"""Create a dedicated upload key locally; never print credentials or include them in source bundles."""
import json
import os
import secrets
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parents[1]
private=root.parent/'.tools/android-build/release-signing'
private.mkdir(parents=True,exist_ok=True)
credentials=private/'upload-key.json'
keystore=private/'bitcoin-pulse-upload.p12'
jdk=next(p for p in (root.parent/'.tools/android-build/jdk').iterdir() if p.is_dir())
if not credentials.exists():
    if keystore.exists():
        raise RuntimeError('Existing keystore has no credential file; refusing to replace it.')
    data={'storeFile':str(keystore),'keyAlias':'bitcoin-pulse-upload','password':secrets.token_urlsafe(36)}
    with credentials.open('x',encoding='utf-8') as stream:
        json.dump(data,stream,indent=2)
else:
    data=json.loads(credentials.read_text(encoding='utf-8'))
env=os.environ.copy()
env['PULSE_KEY_PASSWORD']=data['password']
if not keystore.exists():
    subprocess.run([str(jdk/'bin/keytool.exe'),'-genkeypair','-keystore',str(keystore),
        '-storetype','PKCS12','-storepass:env','PULSE_KEY_PASSWORD','-keypass:env','PULSE_KEY_PASSWORD',
        '-alias',data['keyAlias'],'-keyalg','RSA','-keysize','3072','-validity','10000',
        '-dname','CN=Bitcoin Pulse Upload, O=Bitcoin Pulse, C=NL','-noprompt'],check=True,env=env)
certificate=root/'deliverables/play-store/upload-certificate.pem'
certificate.parent.mkdir(parents=True,exist_ok=True)
subprocess.run([str(jdk/'bin/keytool.exe'),'-exportcert','-rfc','-keystore',str(keystore),
    '-storepass:env','PULSE_KEY_PASSWORD','-alias',data['keyAlias'],'-file',str(certificate)],check=True,env=env)
print('Upload key retained locally outside source archives; public certificate exported.')
