# Ichimoku — alleen een grafisch voorbeeld

[Interactieve preview](https://p.superdesign.dev/draft/0bcebac0-1c32-4583-b6c5-4724d3debb3a).

Dit is het goedgekeurde oorspronkelijke voorbeeld met fictieve gegevens; de HTML en PNG blijven een ontwerp en geen live koersgrafiek. De cloud is inmiddels uitgevoerd in versie 1.3.0 voor alle bestaande app-, standaardwidget-, Omalauncher- en Combi-grafieken. Zie `ICHIMOKU.md` en `ichimoku-native.png` voor de implementatie en native controle.

Het voorbeeld berekent de cloud uit 300 synthetische OHLC-candles: Conversion 20, Base 60, Leading Span B 120, verplaatsing 30. Span A is het gemiddelde van de twee high/low-midpunten over 20 en 60 candles; Span B is het high/low-midpunt over 120 candles. Alleen Span A, Span B en de transparante vulling worden getekend. De candles staan voor de cloud; er is geen extra volumeplot, Conversion-, Base- of Lagging-lijn. De toekomstzone bevat uitsluitend verschoven indicatorwaarden, geen verzonnen toekomstige candles of koersvoorspelling.

Het prototype gebruikt 30 indexposities vooruit. De exacte indexconventie van de indicator uit de referentie moet bij eventuele productie-uitwerking worden vergeleken; deze visualisatie claimt geen pixel- of indexidentieke TradingView-implementatie. Berekeningsreferentie: [TradingView — Ichimoku Cloud](https://www.tradingview.com/support/solutions/43000589152-ichimoku-cloud/).

Visueel en functioneel gecontroleerd op 320, 360 en 390 CSS-pixels breed: geen horizontale overflow of afgekapte hoofdvelden; twee cloudgrenzen en groen/rode vulling; de cloud verdwijnt volledig bij uitschakelen terwijl dezelfde 36 candles blijven; EUR/USD en 4 uur / 1 dag / 1 week schakelen. De gegevens zijn zichtbaar als voorbeelddata gemarkeerd. De persoonlijke referentiefoto's zijn niet geüpload of gekopieerd.
