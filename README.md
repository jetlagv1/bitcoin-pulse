# Bitcoin Pulse — Build App test

Native Android Bitcoin dashboard and home-screen widget.

## Build the current source

Current version: **1.5.0**, versionCode **12**. Open this repository in Android Studio with Java 17 and Android SDK 36 installed, or configure your SDK path in a local `local.properties` and run:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

On Windows, use `gradlew.bat`. Release bundles require your own upload key and the `PULSE_UPLOAD_*` environment variables described in [PLAY-STORE.md](PLAY-STORE.md). Signing keys, passwords, SDK paths, build caches, generated APK/AAB files, and private design context are excluded from Git. Store-asset export helpers in `tools/` are optional and are not needed to build the Android app.

## One or two widget charts (1.5.0)

Expanded Android widgets and OWL 4.0 show at most two charts. Choose one/two at the top, then tap each period title to select 4-hour, daily or weekly candles. Each widget stores its own distinct pair, independent of the app; the app retains three-chart Combi and pinch zoom. Standard widgets migrate earlier selections to at most two. OWL now caches each currency/period separately and activates only the selected one/two candle feeds. Existing OWL imports need replacement. See [WIDGET-GRAPHS.md](WIDGET-GRAPHS.md).

## Horizontal zoom (1.4.0)

Pinch each app chart to show 24–90 candles; each period retains its own zoom, including Combi. Standard Android widgets and OWL 3.1 use compact −/+ buttons beside each chart title, with 24/36/60/90 steps and independent persisted settings. The cloud uses the full existing 240-candle history, keeping its 20/60/120/30 parameters. No extra API calls are made. Existing Omalauncher imports must be replaced to receive the controls. See [ZOOM.md](ZOOM.md) for behavior, minimum widget sizes and validation.

## Ichimoku cloud (1.3.1)

Version 1.3.1 fixes missing cloud fill in the app and standard Android widgets: both colored paths are now populated before drawing. Omalauncher's independent OWL renderer was unaffected; existing OWL imports need no replacement for this fix.

Every existing candle graph has a cloud on/off option, initially off: the app's switch applies to all selected intervals including Combi; each expanded Android widget has a separate Cloud button in its footer; the Omalauncher widget has a persistent Cloud button above the chart. Compact Android widgets have no candle graph and are unchanged. App and widget settings survive restarts. Existing widget-specific choices take precedence over the app default.

The cloud uses 20/60/120/30. Only Leading Span A, Leading Span B and transparent green/red fill are drawn behind the candles. Displacement follows inclusive bar counting (`30 - 1 = 29` index positions). Up to 240 real historical candles are retained for calculation; the visible window defaults to 36 candles and can be zoomed to 24–90. At the default zoom, at least 184 candles are required to cover the visible window and the projected cloud. Insufficient history leaves candles visible with a refresh/history message. No future price candles are fabricated. See [ICHIMOKU.md](ICHIMOKU.md).

## Omalauncher support (1.2.3)

In 1.2.5, OWL widget version 2.0 implements the approved compact design: opaque blue-charcoal background, sans-serif text, explicit colors, stable row widths, hollow green/filled red candles with reserved axes, grouped block details and three fee estimates. Import the new copy from the Widgets tab; updating the Android app does not update a previously imported OWL file. The native renderer check in `tools/verify-omalauncher.ps1` validates text bounds, candle colors and controls at three phone widths using upstream rendering code and recorded public responses.

In 1.2.4 the Omalauncher panel appears at the bottom of the Widgets tab, after both standard Android widget previews and their instructions.

The Widgets tab can import a separate OWL widget into Omalauncher (`dev.omalauncher`). It fetches public Kraken and mempool.space data directly using Android HTTP, without Linux, shell commands, accounts or API keys. EUR/USD and 4-hour/daily/weekly candles are selected within that widget. Last values survive failed requests; timestamps refer to observed value changes, not every successful HTTP check. Repeated unchanged responses may therefore show an older-value warning. Sources run while the widget page or a neighbouring page is visible; the launcher controls scheduling and storage. Its settings are independent of this app's standard Android widgets.

The source is [bitcoin-pulse.owl](app/src/main/assets/bitcoin-pulse.owl). The app creates Omalauncher's documented `owl1` raw-DEFLATE/base64url import link locally and opens it explicitly in Omalauncher; the user confirms the launcher's import review. See [OMALAUNCHER.md](OMALAUNCHER.md) for installation and validation limits.

## Google Play bundle 1.2.1

A separately upload-signed, non-debuggable AAB targets Android 16 / API 36. It adds an in-app privacy statement and removes the unused foreground-worker service/permission; normal periodic widget jobs remain enabled. See [PLAY-STORE.md](PLAY-STORE.md) for upload, signing-key backup, privacy hosting, store assets and testing. Public upload artifacts are in `deliverables/play-store/`; no Play Console upload or publication has been performed.

## Version 1.2

Applies the approved stronger palette across the native app, charts, both widgets, refresh controls and launcher icon: deep blue-charcoal surfaces, vivid Bitcoin orange, brighter green/red candles and restrained cyan network headings. The selected widget interval now uses a solid orange fill with dark text and an accessible selected label. Data behavior and widget dimensions are unchanged from 1.1.

## Version 1.1

The approved compact layout now includes real Kraken 4-hour, daily, and weekly OHLC candles, configurable two/three-chart combinations, and independent interval controls on each large widget. App charts provide candle selection, OHLC, opening time in UTC, and BTC volume. Extended mempool.space block data adds pool attribution, median/range of actual fees, total fees, reward, and weight-based fullness. Difficulty estimates and a 250vB fee example appear in the app. No demonstration data is embedded in production code.

## Implementation brief

- Kotlin, Jetpack Compose, Material 3; Android 8.0 or later.
- Main screen: Bitcoin spot price, selected currency, fee recommendations, latest block, mempool size and pending transaction count.
- EUR and USD selector, persisted across app launches and shared with widgets.
- Compact and expanded widget layouts with price first, then fees and network status.
- Pin-widget action inside the app, where supported by the launcher.
- Refresh while the app is visible; background widget refresh through WorkManager, plus manual refresh.
- Show source attribution, retrieval timestamps, loading state, offline state, and stale data explicitly.
- Cache each source independently so a price outage does not hide blockchain statistics.
- No accounts, API keys, wallet access, or analytics required.

## Data contracts

| Data | Endpoint | Interpretation |
| --- | --- | --- |
| Fee recommendations | `https://mempool.space/api/v1/fees/recommended` | `fastestFee`, `halfHourFee`, `hourFee`, `economyFee`, `minimumFee`, in sat/vB |
| Mempool | `https://mempool.space/api/mempool` | `count` pending transactions; `vsize` in virtual bytes |
| Recent blocks | `https://mempool.space/api/v1/blocks` | First item; height, timestamp, tx_count, weight and extras: medianFee, feeRange, totalFees, reward, pool.name |
| Spot prices | `https://api.kraken.com/0/public/Ticker?pair=XBTEUR,XBTUSD` | `c[0]` last trade price; `h[1]` and `l[1]` rolling 24-hour range |
| Candles | `https://api.kraken.com/0/public/OHLC?pair=XBTEUR&interval=240` | Also XBTUSD; intervals 240/1440/10080 minutes; last row is provisional; keep latest 36 |
| Difficulty estimate | `https://mempool.space/api/v1/difficulty-adjustment` | difficultyChange, progressPercent, remainingBlocks; estimates, not guaranteed outcomes |

The ticker's `o` field is today's opening price at midnight UTC, not a rolling 24-hour opening price. Any comparison using it must be labelled "Today (UTC)" rather than "24h change".

## Update behavior

Target a 30-second interval while the app is visible and a 15-minute WorkManager interval for the widget. Android may defer background refresh to conserve battery. A widget is a periodically refreshed snapshot, not a continuously streaming view. Show the time of each successful source retrieval and allow manual refresh. Keep the last successful values on errors; never replace missing prices or fee rates with zero.

## Design direction

Charcoal background, softly elevated cards, warm Bitcoin orange, off-white primary text, muted secondary text, and green for fresh status. Large tabular price numerals, clear sat/vB labels, generous touch targets, rounded widget corners, and accessible contrast. Use the same hierarchy in the app and on the home screen.

## Status

Interactive design preview is available: [Bitcoin Pulse preview](https://p.superdesign.dev/draft/a63c2245-3f52-47d1-a868-4654af291497). Switch between Dashboard and Widgets, or select EUR/USD on the dashboard. Preview values are samples, not live data.

Design version 2 was approved and implemented as a native Android application with two real home-screen widgets. The Android app uses live API responses; its separate Superdesign preview continues to use sample data.

The source includes Gradle wrapper scripts, JVM data tests, native Android widget layout tests, and device smoke tests. See [INSTALL.md](INSTALL.md) for installation and build instructions and [VALIDATION.md](VALIDATION.md) for verification results and limits. Installable APK and source bundle are in `deliverables/`.

The native network widget now targets a 4×5 area (minimum 280×360dp) and the compact widget a 2×2 area, subject to launcher sizing. Two-chart combinations require 460dp height, three-chart combinations 580dp. Existing undersized widgets retain a compact view with resizing instructions. The chart uses the remaining height, with a fixed 48dp refresh control rather than a stretched footer.

## References

- [mempool.space REST API](https://mempool.space/docs/api/rest)
- [Kraken ticker documentation](https://docs.kraken.com/api-reference/market-data/get-ticker-information)
- [Android widget update guidance](https://developer.android.com/develop/ui/views/appwidgets/advanced)
