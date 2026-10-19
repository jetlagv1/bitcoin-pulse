# Ichimoku cloud — Bitcoin Pulse 1.5.1

De bestaande vormgeving blijft behouden. Er zijn alleen een schakelaar, twee cloudgrenzen en een transparante groen/rode vulling toegevoegd. Er zijn geen extra Conversion-, Base- of Lagging-lijnen en geen volumegrafiek toegevoegd.

## Bediening

- **App:** de schakelaar `Ichimoku cloud` boven de candlekeuzes geldt voor alle weergegeven grafieken, inclusief twee of drie Combi-grafieken. De keuze wordt lokaal bewaard.
- **Standaard Android-widget met grafiek:** tik op `Cloud Aan/Uit` naast de verversknop. De keuze geldt voor alle Combi-grafieken in die widget en wordt per widget bewaard. Een nog niet aangepaste widget volgt de appinstelling; een eigen widgetkeuze blijft daarna onafhankelijk.
- **Omalauncher:** tik op `Cloud Aan/Uit` boven de grafiek. De keuze blijft bij het wisselen van EUR/USD en 4 uur / 1 dag / 1 week behouden en wordt door Omalauncher opgeslagen.
- De cloud is standaard uit. De compacte 2×2-widget heeft geen candleplot en blijft ongewijzigd.

Een app-update vervangt een eerder geïmporteerde Omalauncher-widget niet: verwijder de oude kopie en voeg de nieuwe toe via **Widgets → Toevoegen aan Omalauncher**. De OWL-widget heeft versie 4.1. Begin met 6 kolommen × 13 rijen; de native inhoud is gecontroleerd op 320–390 dp breed bij 840 dp hoog. Vergroot de tegel als de launcher kleinere cellen gebruikt.

## Berekening

Instellingen: Conversion **20**, Base **60**, Leading Span B **120**, displacement **30**. Een midpoint is `(hoogste high + laagste low) / 2`, inclusief de huidige broncandle. Span A is het gemiddelde van de 20- en 60-midpoints; Span B is het 120-midpoint. Het is geen gemiddelde van slotkoersen. De instelling 30 wordt inclusief de huidige candle geteld: de plotindex is de bronindex plus 29. De pure reken- en OWL-pariteitstests leggen deze conventie vast; de eerdere HTML-preview gebruikte nog 30 ruwe indexposities.

De app en OWL-widget bewaren maximaal 240 candles van het bestaande Kraken-antwoord. Ze tonen standaard de laatste 36 echte candles, met horizontale zoom van 24–90 candles. De eerdere candles blijven beschikbaar voor berekening. Volledige clouddekking vereist 148 plus het zichtbare aantal candles: 184 bij de standaardstand en 238 bij 90 candles. Zie `ZOOM.md` voor bediening. Bij minder historie blijven candles zichtbaar met een melding om te vernieuwen. Ook de voorlopige, nog open candle kan de cloud veranderen.

Voor de vooruitgeschoven cloud worden alleen afgeleide indicatorwaarden getekend; er worden geen toekomstige OHLC-candles verzonnen. De as past zich aan de zichtbare candle- én cloudwaarden aan. Uitschakelen verwijdert de cloud en herstelt de volledige candlebreedte. In de app kun je geen candle selecteren door op de lege toekomstzone te tikken.

De vulling is groen wanneer A boven B ligt en rood wanneer B boven A ligt. Bij kruisingen wordt op het exacte lineaire snijpunt gesplitst. De vulling wordt als aaneengesloten vlakken getekend om transparantienaden te voorkomen. De twee grenslijnen zijn lichtgroen en lichtrood.

Berekeningsreferentie: [TradingView — Ichimoku Cloud](https://www.tradingview.com/support/solutions/43000589152-ichimoku-cloud/). De ingestelde perioden zijn die van de gebruiker; de koersbron blijft Kraken. Een andere beurs, tijdzone of OHLC-feed kan andere waarden opleveren dan een externe grafiek.

## Controle

Handmatig berekende high/low-windowfixtures testen 20/60/120, de eerste geldige waarde, de verschuivingsindex en het ontbreken van toekomstinformatie in eerdere punten. Bitmaptests controleren transparante vulling, het oorspronkelijke beeld bij uitschakelen en het ontbreken van een verzonnen cloud bij onvoldoende historie. Alle drie de RemoteViews-Combi-plots worden op cloudvulling gecontroleerd.

De geïsoleerde officiële OWL-renderer controleert aan/uit, tekstvelden, footer, kleuren en cloudgrenzen op drie telefoonbreedtes. De OWL-cloudwaarden worden voor EUR/USD en alle drie de perioden numeriek vergeleken met de Kotlin-berekening uit de productie-app. Zie `tools/verify-omalauncher.ps1` en `docs/design/ichimoku-native.png`. Deze native controle vervangt geen test van import en gebruik op een fysieke telefoon.
