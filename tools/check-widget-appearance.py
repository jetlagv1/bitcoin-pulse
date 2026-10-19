"""Compare vertical anchors exported by both native widget-render tests."""
import json
from pathlib import Path

folder = Path(__file__).resolve().parents[1] / "app/build/reports/widget-parity"
native = json.loads((folder / "android-anchors.json").read_text())
owl = json.loads((folder / "omalauncher-anchors.json").read_text())
expected = {"price", "cloud", "first", "second", "block", "fee", "mempool", "refresh"}
assert set(native) == set(owl) == expected, "Missing visual anchors"
differences = {key: round(abs(native[key] - owl[key]), 3) for key in sorted(expected)}
assert max(differences.values()) <= 2, f"Widget layout differs by more than 2 dp: {differences}"
(folder / "comparison.json").write_text(json.dumps({"verticalCenterDifferenceDp": differences, "toleranceDp": 2}, indent=2) + "\n")
print(f"PASS: {len(expected)} layout anchors; maximum difference {max(differences.values())} dp.")
