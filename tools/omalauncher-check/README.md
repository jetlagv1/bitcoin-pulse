# Native OWL render check

Run `./tools/verify-omalauncher.ps1` from PowerShell with Java 17 / Android SDK configured and the Android project's Gradle dependencies available. The helper can reuse this workspace's local Android environment; elsewhere it uses the Gradle wrapper. `-RefreshSources` re-downloads the pinned upstream files.

The script downloads the **official Omalauncher parser and Compose renderer** at commit `edab9fba94d2d491b8ef474aa74e5715cae7db18`, plus its MIT license, into an ignored tools cache. The checked-in Kotlin files supply deterministic launcher-service fixtures. Only an unrelated upstream point-map helper gets an equivalent block-body syntax adjustment for Kotlin 2.1; text measurement, layout, Canvas rendering, colors and input handling remain upstream code.

An init script temporarily adds these files to the Android **test** source set. They are not part of production APKs/AABs or ordinary tests. Public Kraken/mempool.space responses are recorded for the run. The test loads the actual packaged `bitcoin-pulse.owl`, evaluates its real data expressions and renders it with Robolectric's native graphics at 320, 360 and 390 dp wide and 560 dp high. A deliberately conflicting green launcher palette and monospace default exercise the explicit colors and sans-serif text.

The check asserts no text overflow/ellipsis, horizontal bounds, visible footer, opaque background, red and green pixels within the candle plot, plus working EUR/USD, daily/weekly period and refresh taps. Captures are under `app/build/reports/omalauncher-native/`. This validates the widget content under an upstream renderer with recorded data, **not a complete installed launcher, its outer frame, scheduling, import flow or physical device**.
