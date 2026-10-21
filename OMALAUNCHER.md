# Bitcoin Pulse in Omalauncher

Dit is ondersteuning voor **Omalauncher van Simon Schubert**, pakket `dev.omalauncher`, met OWL-taalversie 8. De widget is een apart OWL-bestand en vereist geen Linux-installatie.

Alle drie de startformaten gebruiken nu **OWL 5.0** en groeien automatisch van mini naar compact naar network. Een miniwidget kan dus na vergroten grafieken tonen; verkleinen behoudt zijn valuta en grafiekinstellingen. Een brede én hoge ruimte van minimaal 250×320 dp krijgt de networkweergave. Compact begint bij 160×160 dp en voegt bij meer hoogte extra gegevens toe. Zie [ADAPTIVE-WIDGETS.md](ADAPTIVE-WIDGETS.md).

**Vervang oude geïmporteerde OWL-widgets één keer** om dit gedrag te krijgen. Omalauncher bewaart zelfstandige kopieën; alleen de APK bijwerken vervangt die niet. Je kunt onderaan het tabblad Widgets kiezen uit network, compact en mini als startformaat. Daarna schaalt elk exemplaar in beide richtingen mee.

## Mini 1×1 toevoegen

1. Open Bitcoin Pulse → **Widgets** en ga naar het onderdeel **Omalauncher** onderaan.
2. Kies bij **Miniwidget · 1×1** de beginvaluta **€ EUR / $ USD**.
3. Tik op **Miniwidget toevoegen aan Omalauncher** en bevestig de import.
4. Voeg op dezelfde manier nog een exemplaar toe met de andere valuta. Elk exemplaar heeft eigen, door Omalauncher bewaarde instellingen.
5. Tik op een geplaatste miniwidget om alleen dat exemplaar tussen EUR en USD te wisselen. Er zijn geen extra knoppen nodig.

De kleur, afgeronde vorm en informatievolgorde zijn gelijk aan de standaard miniwidget: volledige blokhoogte in cyaan, fee-advies voor het volgende blok in oranje en een compacte BTC-prijs in wit als die past. Bij weinig ruimte verdwijnt de prijs; in het kleinste formaat ook het label Blok. Bij vergroten verschijnen de compact- en networkweergaven automatisch. Omalauncher bepaalt de werkelijke celafmetingen en schaalt zijn eigen lettertypen.

De beginvaluta wordt in de app ingesteld. Bestaande OWL-exemplaren bewaart Omalauncher in zijn eigen proces; de app kan die niet uitlezen of wijzigen. Daarom gebeurt later wisselen direct met een tik op de betreffende miniwidget. Dit verandert geen andere miniwidget of appinstelling. In de miniweergave zijn alleen ticker-, blok- en feegegevens actief. Bij vergroten naar network worden de gekozen candlebronnen automatisch actief; eerder geladen historie blijft bij verkleinen bewaard.

De gegevensbronnen worden ongeveer elke minuut opgevraagd wanneer Omalauncher de widget actief houdt. Bij fouten blijven de laatste waarden staan; waar ruimte is verschijnt Blok · oud. Deze tijdstempels weerspiegelen waargenomen bronwijzigingen, zoals bij de grafiekwidget. De launcher bepaalt de achtergrondplanning.

## Installeren

1. Installeer of update Omalauncher en Bitcoin Pulse **1.8.0 (versionCode 22)**.
2. Heb je de oude widget al toegevoegd? Verwijder die eerst uit Omalauncher. Een app-update vervangt Omalaunchers eigen kopie niet automatisch.
3. Open Bitcoin Pulse → **Widgets**, kies één of twee grafieken en tik op **Toevoegen aan Omalauncher**.
4. Lees Omalaunchers importcontrole en bevestig het toevoegen.
5. Houd de widget ingedrukt om hem te verplaatsen of te vergroten. Network begint op 4×5; vergroten naar 4×6 of groter en verkleinen tot mini blijven mogelijk. De compacte indeling past grafiekhoogtes aan de beschikbare ruimte aan. De launcher bepaalt de celmaten; vergroot de widget als de inhoud niet geheel past. Bij één grafiek blijft alle informatie in de widget staan en neemt de grafiek de vrije hoogte in. De tweegrafiekweergave behoudt scrollen bij weinig ruimte.

De knop EUR/USD wisselt de valuta. Kies het eerste aantal in de app vóór het importeren; deze keuze geldt ook wanneer een mini- of compactwidget later naar network groeit. Daarna wissel je direct in de widget met **1 ↔ / 2 ↔** naast verversen. Tik op elke titel met ▾ om 4 uur, 1 dag of 1 week te kiezen. Dubbelen worden voorkomen. De laatste candle is nog niet afgesloten. De datums onder de grafiek gebruiken de tijdzone van je telefoon; de koersvergelijking blijft sinds middernacht UTC. Tik buiten de knoppen op de widget om Bitcoin Pulse te openen. Met ↻ vraag je de bronnen opnieuw op.

Met de compacte **−/+** knoppen naast de grafiektitel kies je 24, 36, 60, 90, 120, 180 of 270 candles; het getal ertussen is het zichtbare aantal. Elke periode onthoudt zijn eigen zoomstand. Dit is OWL-widget **5.0**.

De widget toont koers, wijziging sinds middernacht UTC, standaard 36 candles, blokhoogte, blokleeftijd, transactieaantal, minerpool, mediane gerealiseerde blokfee, fee-advies en mempoolomvang. Met **Cloud Aan/Uit** schakel je de Ichimoku-cloud (20/60/120/30) in of uit; standaard staat hij uit. Er worden 480 historische candles voor de berekening bewaard. Zie `ICHIMOKU.md`. Blokleeftijd rekent door vanuit de laatst bekende bloktijd; dat bewijst niet dat er geen nieuwer blok is.

## Verversen en privacy

Koers- en netwerkbronnen vragen ongeveer elke minuut gegevens op; candles elke vijf minuten. Alleen de één of twee gekozen candlebronnen voor de gekozen valuta zijn actief. Cache en tijdstempel worden per valuta en interval bewaard. Omalauncher bepaalt wanneer de bronnen op zichtbare en naburige pagina's draaien. Er is geen achtergrondverversgarantie.

Laatst ontvangen waarden en instellingen worden door Omalauncher lokaal bewaard. Bij mislukte API-antwoorden blijven de eerdere waarden staan. De tijdregels tonen **waargenomen wijzigingen**: een identiek API-antwoord verandert de tijd niet. Een waarschuwing voor oudere gegevens is daarom bewust voorzichtig. Controleer bij twijfel met Vernieuwen of in Bitcoin Pulse.

De widget maakt rechtstreeks HTTPS-verzoeken naar `api.kraken.com` en `mempool.space`. Die diensten ontvangen je IP-adres en verzoekgegevens. Er worden geen API-sleutels of Bitcoin Pulse-accountgegevens gebruikt. Verwijderen van Bitcoin Pulse verwijdert de onafhankelijke OWL-widget en zijn cache niet; verwijder die ook in Omalauncher.

## Techniek en controle

- OWL-bronnen: `app/src/main/assets/bitcoin-pulse.owl`, `app/src/main/assets/bitcoin-pulse-compact.owl` en `app/src/main/assets/bitcoin-pulse-mini.owl` (alle drie 5.0). De laatste twee worden gegenereerd met `python tools/sync-owl-variants.py`.
- Import: UTF-8 → raw DEFLATE → base64url zonder padding, in het fragment van de officiële `https://simonschubert.github.io/omarchy-widgets/w#owl1.…`-link. De code wordt hiervoor niet naar een server geüpload.
- Gecontroleerd met de officiële OWL-compiler en echte openbare API-antwoorden voor alle zes combinaties van valuta en interval. De controles omvatten eerste laden, correcte koers- en candlevelden, candlebereik, offline cache en scheiding tussen perioden. De compiler meldt geen waarschuwingen en geen Linux-afhankelijkheid.
- De Android-tests controleren ook het verliesvrij coderen van Unicode in de importlink.
- Versie 2.0 is bovendien getekend met de officiële Compose-renderer in een geïsoleerde Robolectric-test, op 320, 360 en 390 dp breed bij 560 dp hoogte. De test gebruikt opgenomen openbare API-antwoorden en een afwijkend launcher-thema. Alle tekstvelden passen, de footer blijft zichtbaar, rood en groen komen in de grafiek voor en de knoppen voor EUR/USD, perioden en verversen werken. Zie `tools/omalauncher-check/README.md` voor de fixturegrenzen en reproductie.
- De import, buitenrand, achtergrondplanning en weergave van een volledige geïnstalleerde launcher zijn nog niet op een fysieke telefoon getest. De native controle vervangt die laatste telefooncontrole niet.

Officiële documentatie: [OWL](https://github.com/SimonSchubert/omalauncher/blob/main/app/src/main/assets/widgets/README.md) en [importformaat](https://github.com/SimonSchubert/omalauncher/blob/main/owl/src/main/kotlin/dev/omalauncher/widgets/market/Share.kt).
