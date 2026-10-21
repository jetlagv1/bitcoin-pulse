# Install Bitcoin Pulse

1. Open **Dropbox → Bitcoin Pulse → Bitcoin-Pulse-1.8.1-debug.apk** on your phone, or transfer it from deliverables. Install it as an update of the directly installed APK to retain preferences.
2. Open the APK on Android 8.0 or newer. If prompted, allow your browser or file manager to install this APK.
3. Open Bitcoin Pulse with an internet connection. Prices come from Kraken; blockchain and fee data come from mempool.space.
4. In **Widgets**, choose network, compact or mini as the starting size. Confirm placement in your launcher, or use the launcher's widget picker. Mini settings are below compact settings. Mini currency is selected per instance in the app; compact/network offer their own currency controls.
5. Resize any instance: mini text grows, compact appears at 160×160 dp, and network with charts appears at 250×320 dp. Compact adds more details as height increases. Shrinking preserves currency, periods, chart count, cloud and zoom. Actual cell sizes vary by launcher.
6. For Omalauncher, use its section at the bottom of the Widgets tab. It offers the same three start sizes, all OWL 5.1. Replace previously imported copies once; updating the APK does not replace Omalauncher's stored copies. See [OMALAUNCHER.md](OMALAUNCHER.md).

In network mode, choose one or two charts, intervals (4 hours, 1 day or 1 week), Ichimoku cloud and zoom directly in the widget. Zoom reaches 270 candles. The app dashboard can display all three Combi intervals and uses two-finger zoom. See [ADAPTIVE-WIDGETS.md](ADAPTIVE-WIDGETS.md) for current sizing and behavior.

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
