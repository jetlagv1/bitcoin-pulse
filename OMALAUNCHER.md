# Bitcoin Pulse in Omalauncher

Dit is ondersteuning voor **Omalauncher van Simon Schubert**, pakket `dev.omalauncher`, met OWL-taalversie 8. De widget is een apart OWL-bestand en vereist geen Linux-installatie.

## Installeren

1. Installeer of update Omalauncher en Bitcoin Pulse **1.4.0 (versionCode 11)**.
2. Heb je de oude widget al toegevoegd? Verwijder die eerst uit Omalauncher. Een app-update vervangt Omalaunchers eigen kopie niet automatisch.
3. Open Bitcoin Pulse → **Widgets** → **Toevoegen aan Omalauncher**.
4. Lees Omalaunchers importcontrole en bevestig het toevoegen.
5. Houd de widget ingedrukt om hem te verplaatsen of te vergroten. Begin met de volle breedte van 6 kolommen en 10 rijen. De geteste inhoudsmaten zijn 320–390 dp breed en 620 dp hoog. De launcher bepaalt de celmaten; vergroot de widget als de inhoud niet geheel past. Scrollen blijft beschikbaar bij weinig ruimte of grotere systeemtekst.

De knop EUR/USD wisselt de valuta. Kies 4 uur, 1 dag of 1 week voor de candles. De laatste candle is nog niet afgesloten. De datums onder de grafiek gebruiken de tijdzone van je telefoon; de koersvergelijking blijft sinds middernacht UTC. Tik buiten de knoppen op de widget om Bitcoin Pulse te openen. Met ↻ vraag je de bronnen opnieuw op.

Met de compacte **−/+** knoppen naast de grafiektitel kies je 24, 36, 60 of 90 candles; het getal ertussen is het zichtbare aantal. Elke periode onthoudt zijn eigen zoomstand. Dit is OWL-widget **3.1**.

De widget toont koers, wijziging sinds middernacht UTC, standaard 36 candles, blokhoogte, blokleeftijd, transactieaantal, minerpool, mediane gerealiseerde blokfee, fee-advies en mempoolomvang. Met **Cloud Aan/Uit** schakel je de Ichimoku-cloud (20/60/120/30) in of uit; standaard staat hij uit. Er worden 240 historische candles voor de berekening bewaard. Zie `ICHIMOKU.md`. Blokleeftijd rekent door vanuit de laatst bekende bloktijd; dat bewijst niet dat er geen nieuwer blok is.

## Verversen en privacy

Koers- en netwerkbronnen vragen ongeveer elke minuut gegevens op; candles elke vijf minuten. Alleen de gekozen candlebron is actief. Omalauncher bepaalt wanneer de bronnen op zichtbare en naburige pagina's draaien. Er is geen achtergrondverversgarantie.

Laatst ontvangen waarden en instellingen worden door Omalauncher lokaal bewaard. Bij mislukte API-antwoorden blijven de eerdere waarden staan. De tijdregels tonen **waargenomen wijzigingen**: een identiek API-antwoord verandert de tijd niet. Een waarschuwing voor oudere gegevens is daarom bewust voorzichtig. Controleer bij twijfel met Vernieuwen of in Bitcoin Pulse.

De widget maakt rechtstreeks HTTPS-verzoeken naar `api.kraken.com` en `mempool.space`. Die diensten ontvangen je IP-adres en verzoekgegevens. Er worden geen API-sleutels of Bitcoin Pulse-accountgegevens gebruikt. Verwijderen van Bitcoin Pulse verwijdert de onafhankelijke OWL-widget en zijn cache niet; verwijder die ook in Omalauncher.

## Techniek en controle

- OWL-bron: `app/src/main/assets/bitcoin-pulse.owl`.
- Import: UTF-8 → raw DEFLATE → base64url zonder padding, in het fragment van de officiële `https://simonschubert.github.io/omarchy-widgets/w#owl1.…`-link. De code wordt hiervoor niet naar een server geüpload.
- Gecontroleerd met de officiële OWL-compiler en echte openbare API-antwoorden voor alle zes combinaties van valuta en interval. De controles omvatten eerste laden, correcte koers- en candlevelden, candlebereik, offline cache en scheiding tussen perioden. De compiler meldt geen waarschuwingen en geen Linux-afhankelijkheid.
- De Android-tests controleren ook het verliesvrij coderen van Unicode in de importlink.
- Versie 2.0 is bovendien getekend met de officiële Compose-renderer in een geïsoleerde Robolectric-test, op 320, 360 en 390 dp breed bij 560 dp hoogte. De test gebruikt opgenomen openbare API-antwoorden en een afwijkend launcher-thema. Alle tekstvelden passen, de footer blijft zichtbaar, rood en groen komen in de grafiek voor en de knoppen voor EUR/USD, perioden en verversen werken. Zie `tools/omalauncher-check/README.md` voor de fixturegrenzen en reproductie.
- De import, buitenrand, achtergrondplanning en weergave van een volledige geïnstalleerde launcher zijn nog niet op een fysieke telefoon getest. De native controle vervangt die laatste telefooncontrole niet.

Officiële documentatie: [OWL](https://github.com/SimonSchubert/omalauncher/blob/main/app/src/main/assets/widgets/README.md) en [importformaat](https://github.com/SimonSchubert/omalauncher/blob/main/owl/src/main/kotlin/dev/omalauncher/widgets/market/Share.kt).
