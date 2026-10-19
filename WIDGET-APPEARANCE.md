# Gelijke widgetvormgeving — Bitcoin Pulse 1.5.1

De controle van versie 1.5.0 liet echte verschillen zien: de standaardwidget had een andere kop, afgeronde intervalknoppen, een cloudknop onderaan, drie kolommen voor blokdetails en andere feevelden. De grote standaardwidget is nu gelijkgetrokken met het goedgekeurde widgetontwerp. De compacte widget zonder grafiek en de grafiekbediening in de app zijn niet gewijzigd.

## Dezelfde visuele opbouw

- Achtergrond `#182230`, afgeronde inhoud met straal 28 dp, padding 14 dp, sans-serif tekst en dezelfde oranje/cyaan/groen/rode accenten.
- Kop met Bitcoin Pulse, vetgedrukte koers met twee decimalen, valutavak rechts en koerswijziging eronder. Prijsgrootte 28 sp op smalle widgets en 32 sp vanaf 340 dp.
- Eén/twee-grafiekenkeuze in vakken van 88 × 44 dp, daarna Ichimoku-label met parameters en dezelfde Cloud Aan/Uit-knop van 76 × 48 dp.
- Grafiektitel met intervalkeuze, zoomknoppen met 40 dp aanraakvlak en 30 dp zichtbaar vlak, en candleaantal. Grafieken van 116 dp met dezelfde marges, afgekorte prijsas, monospaced aslabels, cloudkleuren, holle stijgingscandles en markering van de lopende candle. De prijsas omvat dezelfde zichtbare candle- en cloudwaarden met dezelfde marge.
- Blokkaart met cyaan blokhoogte, leeftijd rechts, transacties en miner op één regel en mediane fee daaronder. Daarna drie feevelden met waarde boven label, mempoolregel en footer met hetzelfde ververssymbool.

De standaardwidget toont nu ook de mempoolwaarde, die al door de app werd opgehaald. Voor die zichtbare waarde tellen fouten en de ophaaltijd mee in zijn status; er is geen nieuwe gegevensbron toegevoegd.

## Afmetingen

Deze volledige opbouw vraagt **620 dp hoogte voor één grafiek** en **800 dp voor twee**. De vergelijking gebruikt 360 × 840 dp. Een eerder kleinere standaardwidget moet worden vergroot; onder de minimumhoogte wordt om meer ruimte gevraagd. Omalauncher kan daarnaast scrollen en begint voor twee grafieken op 6 × 13 cellen.

## Controle en grenzen

De echte RemoteViews-widget en de officiële Omalauncher Compose-renderer worden met dezelfde synthetische koers-, candle-, blok-, fee- en mempoolwaarden getekend. Afbeeldingen en verticale ankerpunten worden naast elkaar gecontroleerd. De overige OWL-tests blijven alle perioden, beide aantallen, zoom, clouds en gescheiden caches controleren met opgenomen openbare bronantwoorden.

Het native intervalmenu blijft een Android-dialoog; Omalauncher klapt zijn keuzes in de widget open. De valutakeuze van de standaardwidget volgt de app en opent de app; Omalauncher houdt zijn eigen valuta. Blokleeftijd en statustijd hebben hun bestaande verversgedrag: de standaardwidget toont de leeftijd op het moment van tekenen, Omalauncher heeft een klokbron. Footerlabels houden dat betekenisverschil zichtbaar. Dit zijn geen wijzigingen aan de gegevenslogica om een afbeelding identiek te laten lijken.

Een launcher kan zelf buitenmarges of clipping toevoegen. De gecontroleerde vormgeving betreft de inhoud; een fysieke telefoon-/volledige launchertest is nog niet uitgevoerd. Een eerder geïmporteerde Omalauncher-widget moet worden vervangen om **OWL 4.1** te ontvangen.
