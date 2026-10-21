# Bitcoin Pulse 1.8.1 / versionCode 23

Verified on 21 October 2026.

- Changed only mini-mode tapping: the primary slot alternates block height and compact BTC price, while the fee stays visible and currency is retained. If the secondary slot fits, it shows the other value. Android uses an owned-widget broadcast and per-instance persisted choice; the app is not opened. OWL 5.1 uses an independent persisted miniShowPrice state instead of toggling currency.
- All 65 ordinary Android tests passed, with zero failures/errors/skips. The updated actual-click test checks two independent IDs, currency retention, no activity launch, tiny-tile price fitting, second-tap restoration and the choice surviving compact/network resize and return to mini.
- The official OWL compiler/renderer mini test passed at six sizes (40×40 through 120×120 dp), including real first/second taps, visible price and unchanged fee/currency, no text overflow, independent instances and missing/failed feeds. Compact/network branches and resizing logic were not otherwise changed.
- Release lint: zero errors and 183 warnings. APK and upload-signed AAB built for 1.8.1 / 23. Official bundletool 1.18.3 verified version/API/signature, universal APK generation and 16KB bundle/ZIP/ELF alignment. All three OWL assets match their packaged APK/AAB copies.
- No physical-device/full-launcher test or Play Console publication was performed. Imported Omalauncher copies must be replaced once with OWL 5.1 to receive the new tap action.

## Historical verification

# Bitcoin Pulse 1.8.0 / versionCode 22

Verified on 21 October 2026.

- All three Android providers now choose mini, compact or network from the available content size. Mini text grows within bounds; compact progressively adds change, block age/transactions/pool, 30-minute/hour fee estimates, median block fee and mempool. Network starts at the existing 250×320 dp minimum. All providers can resize back to 40dp, while retaining identity and settings.
- Android 12+ uses exact-size RemoteViews maps for launcher-provided sizes, including portrait/landscape. Legacy size ranges produce paired orientation layouts. A framework selection test applies the actual generated layout at mini/compact/network sizes. History fetching includes currencies of any possible network size. Resizing requests missing history on entry to network, rather than queuing a job for every drag step.
- 65 ordinary Android JVM/Robolectric tests passed with zero failures/skips. Added coverage includes real provider resize sequences, retained visible currency/intervals/count/cloud/270 zoom, promoted mini/compact controls, large system text, exact-size layout selection, candle-fetch currency scope and actual compact/mini app import links with selected initial settings. The final resize tests were rerun after aligning their screenshot settings.
- Six isolated official OWL compiler/renderer checks passed: the existing three render/data cases, resize case, mini case and adaptive case. All three imports now use the same OWL 5.0 program and differ only in name/start size/default dimensions. Adaptive tests cover all three assets, active candle requests only in network mode, real currency/period/cloud/refresh taps, shrink/grow persistence and compact/mini overflow checks. Normal text scaling is covered in OWL; the full installed launcher and its font settings still require phone testing.
- Existing network appearance comparison passed: eight anchors differ by at most 0.25 dp, and eight one/two-chart resize variants by at most 1.75 dp. Actual adaptive render sequences were visually compared; exported contact sheets contain example data. The app's compact preview now uses actual RemoteViews; mini settings follow compact settings and Omalauncher remains last.
- Release lint: zero errors and 180 warnings. The two added warnings are KTX style suggestions. APK and RSA upload-signed AAB built for 1.8.0 / 22; bundletool 1.18.3 verified version/API/signature, universal APK generation and 16KB bundle/ZIP/ELF alignment.
- No physical-device/full-launcher test or Play Console upload/publication was performed. Existing native widgets update with the app. Previously imported OWL copies must be replaced once with OWL 5.0.

## Historical verification

# Bitcoin Pulse 1.7.1 / versionCode 21

Verified on 21 October 2026.

- Added the matching Omalauncher Mini 1×1 as a separate OWL 1.0 asset. The app's bottom Omalauncher panel imports a new instance with the selected EUR/USD currency. A real tap changes only that imported instance; standard Android per-instance controls remain in the app. Existing OWL instances live in Omalauncher's independent storage, which the app cannot enumerate or change.
- The miniwidgets share information order, colors, rounded background, full block height, next-block fee estimate and conditional compact price. The official OWL compiler/renderer passed six size variants from 40×40 to 120×120 dp, no text overflow, real taps, independent instances, missing values and cached values after failed feeds. The normal-size native and OWL captures were visually compared. Omalauncher uses its renderer's own sans-serif font weights and text scaling.
- All 61 ordinary Android tests passed, with no failures/skips, plus the isolated OwlMiniTest. The new ordinary import test decodes actual app-generated OWL links and verifies independent starting currencies. Release lint remains at zero errors and 178 warnings.
- APK and RSA upload-signed AAB built; bundletool 1.18.3 validated version 1.7.1 / 21, API levels, signature and 16KB bundle/ZIP/ELF alignment. Both actual OWL sources are included unchanged in the APK/AAB. The existing chart OWL 4.5 is unchanged and needs no reimport.
- Project AGENTS.md records the user's standing instruction to implement and verify changes for standard Android and Omalauncher together. No physical-device/full-launcher test or Play Console upload/publication was performed.

## Historical verification

# Bitcoin Pulse 1.7.0 / versionCode 20

Verified on 21 October 2026.

- Added a standard 1×1 miniwidget with full block height, next-block fee estimate in sat/vB and a compact EUR/USD BTC price when space permits. Each added instance has independent, persistent currency controls in the app's Widgets tab. Adding another widget does not alter existing widgets or the app currency; deleting one clears only its own preferences.
- 60 Android JVM/Robolectric tests passed, with no failures or skipped tests. Four new tests exercise actual RemoteViews geometry, independent currencies/deletion, unique activity intents and real Compose currency buttons for two instances. Geometry covers 40×40, 56×56, 64×64, 80×72, 80×88 and 120×120 dp, larger system letters, seven-digit block heights, fractional fees, missing values and stale data. Rendered layouts and app settings were visually inspected; screenshots are in docs/design/miniwidget-*.png.
- Release lint: zero errors and 178 warnings, without new suppressions. The miniwidget adds nine warnings for small caption text, initial-layout strings, API-31 preview/cell attributes and a KTX style suggestion. Older launchers use the 40dp minimum dimensions instead of API-31 cell attributes.
- APK and RSA upload-signed AAB built for 1.7.0 / 20. Official bundletool 1.18.3 validated version/API levels, signature, universal APK generation and 16KB bundle/ZIP/ELF alignment.
- The existing OWL 4.5 asset is byte-for-byte unchanged; no reimport is required. Existing chart widget behavior is retained. No physical-device/full-launcher test or Play Console upload/publication was performed.

## Historical verification

# Bitcoin Pulse 1.6.2 / versionCode 19

Verified on 20 October 2026.

- Translated only the newly marked network section: Network activity → Netwerkactiviteit, Latest block → Laatste blok, Block age → Blokleeftijd, and Blocks → Blokken. Other headings, labels, explanations and widget assets remain unchanged from 1.6.1.
- 56 existing Android tests passed. Release lint: zero errors and 169 warnings. No label-mirroring tests were added for this text-only change.
- APK and upload-signed AAB built for version 1.6.2 / 19. Bundletool 1.18.3 checks version, API levels, RSA signature and 16KB alignment.
- No physical-device test or Play Console upload/publication was performed. OWL remains 4.5; no reimport is required.

## Historical verification

# Bitcoin Pulse 1.6.1 / versionCode 18

Verified on 20 October 2026.

- Translated the English explanatory paragraphs about app/widget refreshing, fee confirmation estimates, widget previews, connectivity and source-refresh failures into Dutch. Widget installation instructions and repository error explanations are also Dutch.
- Translated Add to home screen as Toevoegen aan startscherm, as specifically requested. Other headings, buttons, navigation, short status labels, technical terminology and number/date formatting remain unchanged.
- The production change affects explanatory strings only. 56 existing Android tests passed; release lint reports zero errors and 169 warnings. No extra label-mirroring tests were added.
- APK and upload-signed AAB built for version 1.6.1 / 18. Bundletool 1.18.3 verifies version, API levels, RSA signature and 16KB bundle/ZIP/ELF alignment.
- The OWL asset is byte-for-byte unchanged from 1.6.0 (OWL 4.5); no widget reimport is required for this app text update. No physical-device test or Play Console upload/publication was performed.

## Historical verification

# Bitcoin Pulse 1.6.0 / versionCode 17

Verified on 20 October 2026.

- Extended app pinch zoom and both widgets to 270 visible candles. Widget steps: 24/36/60/90/120/180/270. The cache retains 480 actual candles; complete 20/60/120/30 cloud coverage at maximum zoom needs 418. The projected cloud retains its 29-position displacement.
- EUR/USD now toggles in each standard widget through an explicit broadcast, without opening an activity or changing app/other-widget currency. Refresh fetches the app's and active widgets' currencies; switching currency during a refresh queues a follow-up when required.
- 56 ordinary JVM/Robolectric tests passed, covering cloud fill at all seven zooms, cache length, actual widget button taps up to 270, local currency and independent count/period choices. One-chart metadata and all-height growth remain covered.
- Four isolated upstream OWL compiler/renderer checks passed, including maximum-zoom cloud values for EUR/USD and all three periods, currency/count/period/zoom/cloud/refresh interactions and eight resize variants. Actual test screenshots were visually compared.
- Appearance comparison: eight full-layout anchors differ by at most 0.25 dp; eight single/two-chart resize variants differ by at most 1.75 dp. Dense zoom backgrounds, currency/count text colors and footer controls match the standard widget. Crowded Cloud axis labels are hidden at 120–270 while the projected cloud remains visible.
- Release lint: zero errors, 169 warnings, without added suppressions. APK and RSA upload-signed AAB built. Official bundletool 1.18.3 checks version/API/signature and 16KB bundle/ZIP/ELF alignment.
- No physical-device/full-launcher test or Play Console upload/publication was performed. Replace previously imported OWL copies once with version 4.5 using Widgets → Toevoegen aan Omalauncher.

## Historical verification

# Bitcoin Pulse 1.5.4 / versionCode 16

Verified on 20 October 2026.

- Added a compact 1 ↔ / 2 ↔ toggle beside refresh in both widget footers. Existing footer height and chart space are retained. The app setting remains available.
- 55 ordinary tests passed, including actual RemoteViews count toggles at 400/480/640/840 dp with one and two charts. Toggling does not launch an activity, preserves interval choices, clears an open picker and changes no other widget's count. Existing geometry/metadata/growth checks remain green.
- All four isolated OWL checks passed. Real count-button taps work at three phone widths and in the resize suite; counts and intervals survive switching back, the app is not opened, and normal cloud/period/zoom/refresh checks pass.
- Full layout comparison retains eight anchors with maximum difference 0.25 dp. The footer was visually inspected. Release lint: zero errors, 167 warnings, without new suppressions.
- APK and RSA upload-signed AAB built successfully. Official bundletool 1.18.3 validates versionName 1.5.4 / versionCode 16, API targets, signature and 16KB bundle/ZIP/ELF alignment.
- No physical device/full-launcher test or Play Console publication was performed. Reimport OWL 4.4 once to receive the button; subsequent count changes can be made directly in the launcher widget.

## Historical verification

# Bitcoin Pulse 1.5.3 / versionCode 15

Verified on 19 October 2026.

- 55 ordinary JVM/Robolectric tests passed. The new actual-RemoteViews click test opens inline selectors and switches both slots on single/two-chart widgets at 400, 480, 640 and 840 dp. It verifies broadcast handling, disabled duplicate intervals, closing the selector, persisted choices and absence of any started activity.
- Single-chart RemoteViews retain median block fee, mempool, fee estimates, candle status and source/parameter labels at every measured size. Across 480/560/640/840 dp heights, every added dp goes to the chart. Compact padding resets when a previously tiny widget is enlarged. The 320 dp minimum retains a 40 dp plot with all metadata.
- Three upstream OWL renderer/compiler tests and the isolated resize interaction test passed. Single-chart plots use measured remaining Canvas height at 400/480/560/640/840 dp, preserve all metadata and receive all added height. Inline period/zoom/cloud/refresh taps work without androidApp actions. Two-chart layout and behavior remain covered by the original cases.
- Full two-chart appearance comparison retains eight anchors with maximum center difference 0.25 dp. Actual single-chart and inline-menu renders were inspected at short and tall sizes. Reference images are in docs/design/widget-single-short.png, widget-single-tall.png and widget-inline-periods.png.
- APK and upload-signed AAB built successfully; release lint reports 0 errors and 162 warnings, without a new suppression or baseline.
- Official bundletool 1.18.3 verifies version 1.5.3, versionCode 15, application ID, minimum API 26 / target API 36, non-debuggable release, RSA signature, universal APK generation and 16KB bundle/ZIP/ELF alignment.
- No physical-device/full-launcher test or Play Console upload/publication was performed. OWL 4.3 must be reimported because the launcher stores its own independent copy.

## Historical verification

# Bitcoin Pulse 1.5.2 / versionCode 14

Verified on 19 October 2026.

- 54 regular JVM/Robolectric tests passed. Actual standard RemoteViews were measured at 280, 320 and 360 dp wide, 320/400/480/560/640/840 dp high, with one and two charts. Charts and refresh remain inside the widget, and both charts retain at least 40 dp at the smallest supported size. App count changes update existing widgets and defaults without losing selected intervals.
- Three original upstream OWL renderer/compiler tests passed. The additional compact interaction suite passed in a separate test JVM at 320 dp wide and 400/480/560 dp high with both counts. It sends real taps to zoom, cloud, both interval pickers and refresh. A fresh JVM avoids retained Compose frame scheduling across independent UI suites.
- Full appearance comparison with identical synthetic data: eight vertical anchors, maximum center difference 0.25 dp. Compact Android/OWL captures were inspected for graph readability, footer visibility and separated price/date labels.
- APK and upload-signed release AAB built successfully. Release lint: zero errors, 115 warnings; no added lint suppression/baseline.
- Official bundletool 1.18.3 validates version 1.5.2, versionCode 14, package com.buildapptest.bitcoinpulse, minimum API 26, target API 36 and non-debuggable release. RSA signature, universal APK generation and 16KB bundle/ZIP/ELF alignment checks passed.
- No Play Console upload/publication or physical-phone/full-launcher test was performed. Cell sizes depend on the installed launcher. Existing OWL imports must be replaced; their graph count cannot be remotely changed by the separate Android app.

## Historical verification

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
# Ichimoku — 1.3.0 / versionCode 9 (18 October 2026)

- Added the approved cloud-only 20/60/120/30 option to the app, expanded Android widgets and OWL widget 3.0. Combi plots share their surface's toggle. Each Android widget can override the app default; OWL keeps its own persisted state. Defaults are off. No change to the compact chartless widget or data endpoints.
- Retain 240 real candles, draw the last 36, require 184 observations for full cloud coverage. Inclusive displacement 30 is index offset 29 in both implementations. Hand-computed window fixtures verify the first and last values, offset, warmup and lack of lookahead. Crossings split at their interpolated intersection, with continuous fills rather than per-candle transparency seams.
- All **41 regular tests passed**, including real Compose switch operation in single/Combi mode, settings persistence, translucent rendering, off-state equivalence, insufficient-history behavior and all three RemoteViews Combi cloud plots. APK, upload-signed AAB and release lint builds succeeded.
- Isolated official OWL renderer passed at **320/360/390 × 620 dp**, including visible boundaries, both cloud toggle states, normal controls and non-clipped text/footer. All cloud points for all six currency/period pairs numerically match the production Kotlin calculation within 1e-6. Native snapshot: `docs/design/ichimoku-native.png` (recorded public API responses).
- Physical-phone import, full-launcher framing and background scheduling remain unverified. Imported OWL copies must be replaced by the user. See `ICHIMOKU.md` for calculation and setting semantics.

# Ichimoku fill correction — 1.3.1 / versionCode 10 (19 October 2026)

- A phone screenshot revealed missing fill in the app. The shared Kotlin bitmap renderer painted empty paths before populating them. Both paths are now populated before drawing their translucent interiors, followed by boundary lines and candles. This fixes the app and standard Android widgets. The independent OWL renderer already painted its polygons correctly and is unchanged.
- The previous alpha-only assertion falsely counted antialiased lines/text as cloud fill. Color-and-alpha-specific area assertions now require substantial green/red interiors. Before the fix, the rising, falling and all-Combi-widget assertions failed; after the fix they passed.
- All **42 regular tests passed**. APK and upload-signed AAB builds succeeded; release lint reported **0 errors, 80 existing warnings**. Native Android bitmap captures for deterministic rising/falling fixtures were visually inspected: `app/build/reports/ichimoku-fill/rising.png` and `falling.png`. These validate the actual app/widget chart renderer, independently of OWL. No physical-phone verification was available.

# Horizontal zoom — 1.4.0 / versionCode 11 (19 October 2026)

- Added two-finger app zoom from 24–90 visible candles with independent persisted period settings, latest-candle anchoring and screen-reader actions. One-finger scrolling and candle selection remain available. Real Android multi-pointer events in the scrollable Compose test verify spreading and closing fingers, sequential gestures using the previous zoom and independent Combi state.
- Expanded Android widgets use compact 30 dp visible buttons inside 40 dp targets beside each chart title. Independent widget/period settings use 24/36/60/90 steps; ownership checks reject invalid widget IDs and periods; deleting a widget clears its zoom settings. Headers, disabled bounds, counts, cloud fill and readable plot height are checked in RemoteViews. Minimum heights are now 390/500/600 dp for one/two/three plots.
- OWL 3.1 implements matching buttons and separately persisted period zoom. The pinned official compiler/Compose renderer passed at 320/360/390 × 620 dp with recorded public API data. Real taps check both bounds, all four zoom levels, independent period state and text bounds. Cloud values at the zoom extremes and all six currency/period pairs match the Kotlin calculator. Captures: `docs/design/zoom-omalauncher-native.png`, `zoom-android-widget-native.png` and `zoom-app-native.png`. The app/widget captures use synthetic deterministic test candles, not live data.
- All **47 regular tests passed**. APK, upload-signed AAB and release lint builds succeeded; lint reported **0 errors, 91 warnings**. Bundletool 1.18.3 validated the AAB; existing debug and upload signing identities were preserved. Both packaged OWL assets match the tested source exactly. No extra API calls or data providers were added.
- No physical-phone test or Play Console upload was performed. Existing OWL imports need replacement for the new controls. Small existing Android widgets may need resizing. See `ZOOM.md` for behavior and history requirements.

# One or two widget graphs — 1.5.0 / versionCode 12 (19 October 2026)

- Expanded Android widgets and OWL 4.0 now show one or two independently selected periods. Tapping the period title opens the interval choices; duplicate periods are unavailable. Counts and pairs persist per widget, with zoom controls retained. Standard widgets migrate earlier Combi selections to at most two periods and then keep their own settings. The compact chartless widget and app chart behavior are unchanged; the existing app test still verifies three Combi graphs and pinch zoom.
- All **51 regular tests passed**, including all ordered interval pairs, hiding the second plot, legacy migration, independent widget storage, count limits, invalid ID/slot rejection, deletion cleanup and clicking the actual native interval chooser. Release lint: **0 errors, 90 warnings**. Debug APK and upload-signed AAB builds succeeded.
- Both isolated official OWL tests passed: native interaction/render checks at 320/360/390 × 840 dp, and cache-isolation checks for missing/failed feeds. The native test exercises one/two counts, both period menus, zoom limits, all currency/period cloud values and both plots for every distinct pair. The host now draws full frames after remeasurement so taps follow layout changes. No upstream input/render code was modified for that fixture correction.
- OWL retains separate caches and timestamps for each currency/period and enables only the selected one/two candle feeds. Failed/missing sources never substitute another period or currency. The two cloud plots continue using the existing 20/60/120/30 calculation and 240-candle history.
- Visually inspected native captures: `docs/design/two-chart-android-native.png`, `two-chart-picker-native.png`, `two-chart-omalauncher-native.png`. Standard Android captures use synthetic deterministic fixtures; OWL uses recorded public API responses. Physical-phone/complete-launcher behavior remains unverified. Existing OWL imports must be replaced to receive version 4.0.
- Bundletool validated the final AAB; both signing identities remain unchanged and both packaged OWL assets match the tested source. No Play Console upload/publication was performed. See `WIDGET-GRAPHS.md` for installation and controls.

# Consistent widget appearance — 1.5.1 / versionCode 13 (19 October 2026)

- The requested comparison found that the large Android widget and OWL still differed in header, cloud position, block/fee layout, chart axes and refresh icon. The expanded RemoteViews layout now follows the approved composition, with matching fonts/weights, palette, padding, rounded content, controls, block card, fee fields and mempool row. Widget-only bitmap styling aligns hollow candle bodies and monospaced abbreviated axes; the app's default chart renderer and compact widget remain unchanged.
- Both real renderers use `WidgetVisualFixture` at **360 × 840 dp**. Visually inspected captures: `docs/design/widget-parity-android.png`, `widget-parity-omalauncher.png` and `widget-parity-comparison.png`. Nine independent layout anchors have a **maximum vertical-center difference of 0.25 dp**; `tools/check-widget-appearance.py` enforces a 2 dp tolerance. These captures use identical synthetic prices, OHLC, block, fee and mempool values. Status timestamps reflect each run and retain their actual semantics.
- Full layout minimum heights are **620 dp for one graph / 800 dp for two**. Standard Android target cell count remains five; existing smaller widgets use the compact fallback or a resize message. Tests cover footer bounds, both graph counts and the production compact fallback. OWL retains scrolling and its own explicit rounded background; disabled zoom faces now dim like the native widget.
- All **52 regular tests** and **3 isolated official OWL tests** passed, including native interactions on 320/360/390 dp widths, shared visual data, cache isolation, chart selection, zoom and cloud calculation parity. Release lint: **0 errors, 98 warnings**. Debug APK and upload-signed AAB builds succeeded. Bundletool validation and signing/packaged-source checks passed; signing identities remain unchanged.
- The native interval chooser remains an Android dialog, while OWL uses an inline menu. Native currency follows the app; OWL keeps its own. Native block age is captured at redraw and OWL has a clock source. These existing data/interaction differences were preserved. No physical-phone/full-launcher check or Play upload was performed. Import OWL **4.1** again to receive this update. See `WIDGET-APPEARANCE.md`.
