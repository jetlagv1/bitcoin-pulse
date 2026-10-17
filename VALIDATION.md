# Bitcoin Pulse Play release 1.2.2 verification

Version 1.2.2 uses versionCode 5 because Play Console reports versionCode 4 is already used. This rebuild changes only publishing version metadata, retaining application ID, upload key, API targets, supported architectures, privacy text and app behavior. Verification tooling now reads the expected version from Gradle and checks the built manifest against it. Current release artifacts include the version code in the AAB filename to avoid selecting the previous upload by mistake. Full bundle verification results are saved separately in `deliverables/play-store/VERIFICATION.json`.

## Historical Play release 1.2.1 verification

Verified on 16 October 2026. Deliverable: `Bitcoin-Pulse-1.2.1-release.aab`, versionCode 4, package `com.buildapptest.bitcoinpulse`, minimum API 26, compile/target API 36.

## Play bundle results

- Release AAB built successfully using Android SDK 36, AGP 8.9.2, Gradle 8.11.1 and Java 17. Native app colors/data behavior from 1.2 are retained. A local privacy statement is reachable through a native app dialog. The unused WorkManager foreground service/permission was removed; this app uses normal short background jobs.
- All 31 JVM tests passed after the target SDK change. Release lint: 0 errors, 63 warnings, with no baseline or added suppressions.
- Bundle is not debuggable and is signed with a newly generated, dedicated 3072-bit RSA upload key; it does not use the previously distributed debug key. JAR signature verification passed. The private key/password stay outside the project source archive and public deliverables.
- Official Google bundletool 1.18.3 validated the AAB and generated a signed universal APK locally. No app or signing key was uploaded during verification.
- 16KB bundle configuration, universal APK ZIP page alignment and ELF PT_LOAD alignment checks passed for all four packaged architectures' `libandroidx.graphics.path.so` libraries.
- Detailed metadata and AAB checksum are in `deliverables/play-store/VERIFICATION.json`. Packaging refuses a bundle whose checksum differs from that verified artifact. The source ZIP is checked not to contain the actual upload-key password and excludes keystores/private configuration/build caches.
- No physical-device or emulator test was run for the API 36/release variant. Internal Play testing remains necessary. Source/metadata checks and signing do not guarantee Google policy approval.
- No Play account was accessed and no release was uploaded or published. Public privacy hosting, publisher contact details, store graphics/screenshots and Play declarations remain publisher tasks described in PLAY-STORE.md.

## Historical version 1.2 verification

Verified on 16 October 2026. Current deliverable: `Bitcoin-Pulse-1.2-debug.apk`, version 1.2.0, versionCode 3.

## Version 1.2 results

- Implements the color proposal approved by the user: blue-charcoal background/cards, orange active controls, bright green/red candles, cyan network headings, and readable light primary/secondary text. Both widgets, chart rendering, app theme, refresh assets, and launcher icon use the approved colors.
- Selected widget intervals use a filled orange background and dark text, plus a textual selected-state accessibility description. The app uses the same orange selection treatment.
- All 31 existing JVM data, parser, layout and native chart tests passed. Native widget snapshots were regenerated and visually reviewed for the new palette. No new tests were added for literal color substitutions.
- Android lint completed with 0 errors and 73 warnings (existing categories plus XML color string guidance). No baseline or suppression was added.
- APK signature verified; certificate matches the 1.0/1.1 builds. Package ID and minimum/target Android SDK remain unchanged. Installation can update the previous app without uninstalling.
- No network/data behavior changed, so the successful production API checks from 1.1 were not repeated.
- No physical-phone or launcher test was run for this update. Automated native widget rendering does not replace on-device verification. Background refresh remains subject to Android scheduling.

## Historical version 1.1 verification

Verified on 15 October 2026. Current deliverable: `Bitcoin-Pulse-1.1-debug.apk`, version 1.1.0, versionCode 2.

## Version 1.1 results

- APK, device smoke-test APK, JVM tests, and Android lint built successfully with the existing SDK/toolchain. Offline build completed with no new dependencies.
- All 31 JVM tests passed: 13 existing data/freshness tests, 10 candle/extended-block/difficulty tests, 3 layout tests, and 5 native-graphics widget chart tests.
- Tests cover real OHLC semantics, invalid OHLC rejection, currency isolation, provisional final candles, retention of the latest 36 candles, chosen combinations, optional/unknown mining pools, fractional fees, weight-based fullness, cached failure timestamps, single/combined charts, missing data, and resize guidance instead of clipping.
- Native RemoteViews screenshots were rendered and visually inspected for the single-chart 320×360dp widget and three-chart 320×580dp widget. Minimum-width 280×360dp layout and compact 160×160dp refresh bounds also passed. Rendered test screenshots use fixtures only; production screens never use these fixtures.
- Chart font scaling was corrected during visual review so chart labels use actual display density rather than shrinking with chart width.
- Lint: **0 errors, 72 warnings**. Warnings include dependency update notices, XML string extraction, optional widget metadata on older APIs, and existing layout/style guidance. No lint baseline or error suppression was added.
- Read-only production endpoint checks passed for all six Kraken OHLC combinations (EUR/USD × 240/1440/10080 minutes), extended recent blocks including actual pool/median fee/weight, and difficulty estimate. See `deliverables/live-api-check-1.1.txt`; these requests were made from the build computer.
- APK signature verified. The signing certificate SHA-256 is `da9d435f1a0209f03272900aaca4167bc91ce48aa053f955a7ef7407c2333bf4`, identical to the previously delivered 1.0 APK. Package ID remains `com.buildapptest.bitcoinpulse`, minimum API 26, target API 35.
- Device smoke tests were updated for scrollable candle content and the new widget dimensions, and compiled successfully. They were **not executed on a phone/emulator**. Launcher placement, touch behavior, visual accessibility settings, and background scheduling still require on-device verification.

The expanded widget uses independent per-widget interval selection. Combination membership is selected in the app. Single charts require 360dp height; combinations require 460dp for two or 580dp for three. Older undersized widgets use the compact layout with resizing instructions. Widget block age is an as-of-render value with a clock time, not a live countdown. Android may postpone background updates. OHLC responses have a 90-second cache and difficulty a five-minute cache.

This is a debug-signed testing APK. It updates the prior build without uninstalling. The source archive excludes caches, machine-specific SDK paths, signing keys, and private design context.

## Repeat 1.1 checks

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
python tools/check-live-data.py
python tools/package_release.py
```

## Historical version 1.0 verification

Verified on 15 October 2026.

## Completed

- Debug APK assembled successfully with Android Gradle Plugin 8.9.2, Gradle 8.11.1, Java 17, and Android SDK 35.
- APK signature verified with Android `apksigner`: APK Signature Scheme v2, one signer.
- APK metadata verified: application ID `com.buildapptest.bitcoinpulse`, version `1.0.0`, minimum Android API 26 (Android 8.0), target API 35.
- All 15 JVM tests passed: 13 data/parser/freshness tests and 2 Android widget layout tests.
- Both widget XML layouts successfully inflated through RemoteViews in Robolectric's Android API 35 runtime with native font measurement. Tests checked that refresh controls remain inside the network widget at 320×200dp and compact widget at 160×160dp and preserve 48dp touch targets.
- Android lint completed with 0 errors and 42 warnings. Warnings cover newer available dependency versions, XML text extraction, optional API 31 widget metadata on older Android versions, backup configuration guidance, layout performance suggestions, and Kotlin style suggestions. Lint errors were fixed without suppressing checks or adding a baseline.
- Device smoke-test APK compiled successfully. Tests cover navigation, currency propagation to widget previews, and widget layout sizing.
- Read-only requests to all four production API endpoints passed response-contract checks: mempool.space recommended fees, mempool state, recent blocks, and Kraken EUR/USD ticker.
- The approved Superdesign prototype remains available separately; it uses labelled sample data. The native application uses the production endpoints and has no embedded demonstration prices.

## Limits

No physical Android device was connected. An official Android emulator was installed and started in software mode, but it did not finish booting into a usable device on this computer. Device smoke tests were compiled but could not be executed. On-device appearance, launcher pinning, long-running background scheduling, rotation, and font accessibility settings still need verification on an Android phone.

The JVM widget tests validate Android inflation and measured bounds; they do not replace launcher testing. The live endpoint checks ran on the build computer, not inside a phone. Android can defer the requested 15-minute widget refresh interval. Retrieval timestamps and cached/error states are displayed so older data is not silently replaced by zero.

This deliverable is a debug-signed testing APK, not a production-signed Play Store release. The source bundle excludes build caches, machine-specific SDK paths, and signing keys.

## Repeat checks

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
python tools/check_live_api.py
```

With an Android test device connected:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Test reports are generated under `app/build/reports/`. The separate `SHA256SUMS.txt` records the hashes of the packaged APK and source archive.
# Omalauncher update — 1.2.3 / versionCode 6 (17 October 2026)

- `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`, and the upload-signed `:app:bundleRelease` / `:app:lintRelease` completed successfully. All 32 JVM/Robolectric tests passed, including the raw-DEFLATE/base64url Unicode import-link round trip.
- The official Omalauncher OWL compiler accepted `app/src/main/assets/bitcoin-pulse.owl` without warnings or Linux dependencies. Its evaluator was exercised with public Kraken and mempool.space responses for EUR/USD × 240/1440/10080-minute candles: all six produced 36 candles and valid price ranges. Initial loading, cached values after unsuccessful responses and period isolation were checked.
- Bundletool 1.18.3 validated the AAB. Its upload certificate remains SHA-256 `A7:65:3C:69:26:4A:67:F9:35:64:8B:5C:12:48:F9:09:74:47:BB:4B:39:B0:33:EB:6F:8C:C5:E8:3E:F6:57:F7`.
- Both final APK and AAB contain an exact copy of the checked OWL source.
- No physical Omalauncher device test was available. Import confirmation, theme appearance and scrolling still need checking on the user's phone. See `OMALAUNCHER.md` for the conservative timestamp semantics and independent widget storage.
# Omalauncher redesign — 1.2.5 / versionCode 8 (17 October 2026)

- Implemented the approved design as OWL widget 2.0: opaque background, explicit palette, sans-serif Text nodes, controlled row widths, reserved chart axes, hollow up/filled down candles, grouped block data and three fee estimates. App dashboard and standard Android widget layouts are unchanged.
- Official OWL compiler: accepted, no warnings, no Linux dependency. Data evaluation passed EUR/USD × 240/1440/10080-minute candles, initial loading, failed-response cache retention and period isolation.
- **Native renderer check:** production OWL rendered using the official Omalauncher Compose layout/drawing sources at pinned commit `edab9fba94d2d491b8ef474aa74e5715cae7db18`, with recorded public API responses and deterministic launcher-service fixtures. A small block-body syntax compatibility patch only affects the unused upstream point-map helper. At 320×560, 360×560 and 390×560 dp, all text stayed within the widget without overflow/ellipsis, the footer remained visible, both red and green pixels appeared inside the candle plot despite an incompatible green launcher palette, and EUR/USD, daily/weekly and refresh taps worked. Capture: `docs/design/omalauncher-v2-native.png`. Reproduction: `tools/verify-omalauncher.ps1`.
- These checks validate the widget content with upstream rendering, not an installed full launcher, physical phone, outer launcher frame, import review or background scheduling. Re-import on a phone is still necessary. Chart date labels use the phone's local time zone; daily price change remains based on midnight UTC.
- APK build, all **32 regular tests**, upload-signed AAB build and release lint succeeded (0 errors, 64 warnings). Bundletool 1.18.3 validation passed. APK and AAB contain the exact final checked OWL source. Existing APK/debug and Play/upload signing identities were preserved.
- APK SHA-256: `2bfe29430bfc785b48c5b87859bedb4f1b54a9297d2f011208dac82e86c1dcb0`.
- AAB SHA-256: `42c941ca6ed8cfa360d6dd68574288564abd8dd864776108a3c52dee48b00852`.
