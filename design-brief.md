# Bitcoin Pulse preview brief

## Audience and purpose

Android users who want a quick, legible view of Bitcoin's price and network conditions without opening a block explorer. The primary deliverable is a useful home-screen widget, supported by a native Android dashboard.

## Preview content

Show a coherent Android dashboard and home-screen widget design. Any numerical values in the preview are sample values and must be visibly labelled as such. The production app will use live API data.

### Dashboard

1. Header: Bitcoin Pulse, Bitcoin mainnet, freshness status, refresh action.
2. Price: large tabular numerals, selected EUR/USD currency, "Today (UTC)" change, rolling 24-hour high and low.
3. Fees: next block, approximately 30 minutes, approximately one hour, each with its fee rate in sat/vB. Confirmation times are estimates.
4. Network: latest block height, time since block, pending transaction count, virtual mempool size in vMB.
5. Widget action: "Add to home screen" with a compact preview.
6. Source labels: mempool.space and Kraken; retrieval timestamps and stale/offline states.

### Home-screen widget

- Expanded 4x2-style layout: Bitcoin price and selected currency; three fee recommendations; latest block and pending transactions; refresh action and last-updated time.
- Compact layout: price, next-block fee rate, refresh action and last-updated time. Prioritize legibility over fitting all metrics.
- Main body opens the dashboard. Refresh is a separate accessible action.
- Widget previews must look like native Android widgets, with rounded corners and enough separation from a neutral home-screen background.

## Visual direction

Modern Material 3-inspired Android design. Charcoal base, subtle layered surfaces, warm orange emphasis for Bitcoin and selected actions, restrained green freshness indicators, off-white text, muted secondary labels. Typography should be a clean system-style sans serif with tabular numeric values. Rounded cards, clear alignment, comfortable spacing, 48dp touch targets, and accessible contrast. Avoid decorative price charts with invented historical data; hierarchy and readable metrics carry the design.

## Product behavior to communicate

Live API retrieval while the app is open. Battery-conscious scheduled refresh for widgets, subject to Android delays. Manual refresh and visible timestamps. Preserve cached data during a source outage, distinguish source failures, and mark old data rather than calling it live. No accounts or wallet connections are needed by the finished Android app.

## Open preference

EUR default with a USD switch is the proposed default, pending the user's currency preference.
