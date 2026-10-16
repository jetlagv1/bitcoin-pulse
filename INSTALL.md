# Install Bitcoin Pulse

1. Open **Dropbox → Bitcoin Pulse → Bitcoin-Pulse-1.2-debug.apk** on your phone, or transfer it from the deliverables folder. You can install it over version 1.0 or 1.1 without uninstalling; this build uses the same signing certificate and preserves your preferences.
2. Open the APK on Android 8.0 or newer. If prompted, allow the browser or file manager you are using to install this APK.
3. Open **Bitcoin Pulse** with an internet connection. The dashboard retrieves live prices from Kraken and network data from mempool.space.
4. Select **EUR** or **USD** on the dashboard. This setting applies to both widget types.
5. Select **Widgets**, then **Add network widget** or **Add compact widget**. Confirm placement in your launcher. Alternatively, long-press your home screen, choose Widgets, and select Bitcoin Pulse.

The expanded network widget targets 4 columns by 5 rows, with a minimum 280×360dp area. Exact grid sizes depend on your launcher. Resize an existing 1.0 widget to at least 360dp high to show the new chart, or remove and re-add it. Below that height it keeps a compact price/fee view and asks you to enlarge it. The separate compact widget remains 2×2.

Choose **4 uur**, **1 dag**, **1 week**, or **Combi** using the widget buttons. Each widget remembers its own interval. In the app, Combi lets you choose any two or all three intervals; that selection is shared by the widgets. A two-chart widget needs at least 460dp height, three charts 580dp. Otherwise it asks for more space, and you can choose a single interval or open the app.

Intervals describe the duration of each candle. Charts show up to 36 recent observations, have independent price axes, and show UTC dates. Rising candles are hollow, falling candles filled; a dot marks the final candle, which is still in progress. In the app, tap a candle or use **Vorige**, **Volgende**, and **Laatste** to inspect OHLC values, BTC volume, and its exact UTC opening time.

Latest-block details include transaction count, recognized mining pool, median paid fee, weight-based fullness, fee range, total fees, and reward including fees when available. Unknown values show a dash or “Onbekend”. Actual block fees are separate from current recommendations. Difficulty change is an estimate. The 250vB transaction example is only a size-based fee illustration.

The app refreshes prices/network data about every 30 seconds while visible. Candle responses are cached for 90 seconds and difficulty for five minutes. Widgets request updates every 15 minutes, subject to Android battery restrictions. Tap refresh to request an update (the same short cache windows apply), or tap the main surface to open the app. Cached data stays visible if a source fails, with its original retrieval timestamp. The widget footer uses the oldest retrieval time of its displayed sources. The block age in a widget is calculated at render time, with that clock time next to it; it is not a continuously running timer. The app recomputes age every second from the latest known block, with source freshness shown separately.

This APK is a debug-signed testing build. It is not published on Google Play. Installing updates requires the same signing key; a build made on another computer may require uninstalling this version first, which clears its local settings and cache.

No account or API key is required. The app has internet and network-state permissions, accesses public data, and stores only its currency preference, cached public responses, and refresh status locally. It has no wallet access or analytics.

## Build from source

Open the project in Android Studio with Java 17 and Android SDK 35, or run:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Android Studio can generate `local.properties` for your SDK location. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Device smoke tests are included under `app/src/androidTest`. With an Android test device connected, run `gradlew.bat :app:connectedDebugAndroidTest`.
