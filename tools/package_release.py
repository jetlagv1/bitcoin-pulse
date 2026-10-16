"""Package the verified debug APK and a clean source bundle without build caches."""
from pathlib import Path
import hashlib
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "deliverables"
EXCLUDED = {".gradle", ".kotlin", ".idea", ".superdesign", "build", "deliverables", "__pycache__"}

def main():
    OUT.mkdir(exist_ok=True)
    apk = OUT / "Bitcoin-Pulse-1.2-debug.apk"
    shutil.copy2(ROOT / "app/build/outputs/apk/debug/app-debug.apk", apk)
    source = OUT / "Bitcoin-Pulse-1.2-source.zip"
    with zipfile.ZipFile(source, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(ROOT.rglob("*")):
            relative = path.relative_to(ROOT)
            if path.is_file() and not (set(relative.parts) & EXCLUDED) and path.name != "local.properties":
                archive.write(path, Path("Bitcoin-Pulse") / relative)
    for name in ("INSTALL.md", "VALIDATION.md"):
        shutil.copy2(ROOT / name, OUT / name)
    with (OUT / "SHA256SUMS.txt").open("w", encoding="utf-8") as checksum_file:
        for path in (apk, source):
            with path.open("rb") as artifact:
                checksum = hashlib.file_digest(artifact, "sha256").hexdigest()
            checksum_file.write(f"{checksum}  {path.name}\n")
            print(f"{path.name}: {path.stat().st_size:,} bytes; SHA256 {checksum}")

if __name__ == "__main__":
    main()
