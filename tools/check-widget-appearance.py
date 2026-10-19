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

resize_folder = folder.parent / "widget-resize"
resize_results = {}
for count, heights in [(1, [400, 480, 560, 640, 840]), (2, [400, 480, 560])]:
    for height in heights:
        stem = f"320-{height}-{count}"
        native_resize = json.loads((resize_folder / f"android-{stem}.json").read_text())
        owl_resize = json.loads((resize_folder / f"omalauncher-{stem}.json").read_text())
        assert native_resize.keys() == owl_resize.keys()
        delta = {key: round(abs(native_resize[key] - owl_resize[key]), 3) for key in native_resize}
        assert max(delta.values()) <= 2, f"Resize layout differs at {stem}: {delta}"
        resize_results[stem] = delta
(folder / "resize-comparison.json").write_text(json.dumps(resize_results, indent=2) + "\n")
print(f"PASS: {len(resize_results)} resize variations; maximum difference {max(max(v.values()) for v in resize_results.values())} dp.")
