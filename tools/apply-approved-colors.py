"""Apply the approved 16 October color palette consistently across native surfaces."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
palette={
    '101114':'0C1018','1B1D22':'182230','252830':'243246',
    '343840':'38485F','353840':'38485F','454A54':'243246',
    'F5F5F7':'F7FAFF','ADB2BD':'BAC7DB','FF9E45':'FF8A1F',
    '8DDDB7':'35E89A','FFB4AB':'FF647C','F2CB7C':'FFD166',
}
for p in (root/'app/src/main').rglob('*'):
    if p.suffix not in {'.kt','.xml'}: continue
    original=p.read_text(encoding='utf-8')
    updated=original
    for old,new in palette.items():
        updated=updated.replace('#'+old,'#'+new).replace('0xFF'+old,'0xFF'+new)
    if updated!=original:
        p.write_text(updated,encoding='utf-8')
        print(p.relative_to(root))
