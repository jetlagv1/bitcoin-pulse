# Bitcoin Pulse project instructions

- Every requested feature, behavior, visual change and text change must be implemented for both the standard Android widgets/app and the corresponding Omalauncher OWL widgets in the same update. This is the user's explicit standing preference.
- Keep equivalent widgets aligned in information, colors, layout, sizing and independent per-instance settings. Include new widget variants for Omalauncher too.
- Verify both implementations before calling an update complete. If a launcher limitation prevents equivalent behavior, explain the limitation and resolve the interaction without silently omitting Omalauncher.
- Deliver updated APK/AAB and any changed/new OWL assets together, update installation instructions, and push the source to the existing GitHub repository.

- The adaptive OWL program is canonical in `app/src/main/assets/bitcoin-pulse.owl`. Regenerate mini/compact import variants with `python tools/sync-owl-variants.py` after every edit; never maintain separate feature logic in these variants.
