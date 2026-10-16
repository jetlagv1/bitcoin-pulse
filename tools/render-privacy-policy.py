"""Export the privacy document as accessible, static HTML without scripts or external assets."""
from pathlib import Path
import html
import re

root=Path(__file__).resolve().parents[1]
source=root/'deliverables/privacy/PRIVACYBELEID-CONCEPT.md'
def inline(text):
    text=html.escape(text)
    text=re.sub(r'`([^`]+)`',r'<code>\1</code>',text)
    text=re.sub(r'\*\*([^*]+)\*\*',r'<strong>\1</strong>',text)
    text=re.sub(r'https://[^\s<]+',lambda m:'<a href="'+m.group()+'">'+m.group()+'</a>',text)
    return text

output=[]
paragraph=[]
in_list=False
def flush():
    if paragraph:
        output.append('<p>'+inline(' '.join(paragraph))+'</p>')
        paragraph.clear()

for line in source.read_text(encoding='utf-8').splitlines():
    if not line.strip():
        flush()
        if in_list: output.append('</ul>'); in_list=False
    elif line.startswith('# '):
        flush(); output.append('<h1>'+inline(line[2:])+'</h1>')
    elif line.startswith('## '):
        flush(); output.append('<h2>'+inline(line[3:])+'</h2>')
    elif line.startswith('- '):
        flush()
        if not in_list: output.append('<ul>'); in_list=True
        output.append('<li>'+inline(line[2:])+'</li>')
    else:
        paragraph.append(line)
flush()
if in_list: output.append('</ul>')
document='''<!doctype html>
<html lang="nl">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Privacybeleid — Bitcoin Pulse</title>
<style>
body{font:16px/1.65 system-ui,sans-serif;max-width:48rem;margin:2rem auto;padding:0 1rem;color:#17202a;background:#fff}
h1{font-size:1.8rem;line-height:1.25}h2{font-size:1.2rem;margin-top:1.8rem}a{color:#0759a3;overflow-wrap:anywhere}
code{font-size:.95em;overflow-wrap:anywhere}li{margin:.35rem 0}strong{font-weight:650}
@media(prefers-color-scheme:dark){body{background:#111820;color:#edf1f7}a{color:#8ac8ff}}
</style>
</head><body><main>'''+ '\n'.join(output)+'''</main></body></html>'''
target=source.with_name('privacybeleid-concept.html')
target.write_text(document,encoding='utf-8')
assert '[NAAM ONTWIKKELAAR]' in document and '[CONTACT-E-MAILADRES]' in document
assert '<script' not in document and '<form' not in document
print('Privacybeleid HTML geëxporteerd; twee soorten contact-invulvelden blijven zichtbaar.')
