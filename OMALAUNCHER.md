# Bitcoin Pulse in Omalauncher

Dit is ondersteuning voor **Omalauncher van Simon Schubert**, pakket `dev.omalauncher`, met OWL-taalversie 8. De widget is een apart OWL-bestand en vereist geen Linux-installatie.

## Installeren

1. Installeer of update Omalauncher en Bitcoin Pulse 1.2.3 (versionCode 6).
2. Open Bitcoin Pulse → **Widgets** → **Toevoegen aan Omalauncher**.
3. Lees Omalaunchers importcontrole en bevestig het toevoegen.
4. Houd de widget ingedrukt om hem te verplaatsen of te vergroten. Begin met 6×10 cellen; de inhoud kan scrollen.

De knop EUR/USD wisselt de valuta. Kies 4 uur, 1 dag of 1 week voor de candles. De laatste candle is nog niet afgesloten. Tik buiten de knoppen op de widget om Bitcoin Pulse te openen. Met Vernieuwen vraag je de bronnen opnieuw op.

De widget toont koers, wijziging sinds middernacht UTC, 36 candles, blokhoogte, blokleeftijd, transactieaantal, minerpool, mediane gerealiseerde blokfee, fee-advies en mempoolomvang. Blokleeftijd rekent door vanuit de laatst bekende bloktijd; dat bewijst niet dat er geen nieuwer blok is.

## Verversen en privacy

Koers- en netwerkbronnen vragen ongeveer elke minuut gegevens op; candles elke vijf minuten. Alleen de gekozen candlebron is actief. Omalauncher bepaalt wanneer de bronnen op zichtbare en naburige pagina's draaien. Er is geen achtergrondverversgarantie.

Laatst ontvangen waarden en instellingen worden door Omalauncher lokaal bewaard. Bij mislukte API-antwoorden blijven de eerdere waarden staan. De tijdregels tonen **waargenomen wijzigingen**: een identiek API-antwoord verandert de tijd niet. Een waarschuwing voor oudere gegevens is daarom bewust voorzichtig. Controleer bij twijfel met Vernieuwen of in Bitcoin Pulse.

De widget maakt rechtstreeks HTTPS-verzoeken naar `api.kraken.com` en `mempool.space`. Die diensten ontvangen je IP-adres en verzoekgegevens. Er worden geen API-sleutels of Bitcoin Pulse-accountgegevens gebruikt. Verwijderen van Bitcoin Pulse verwijdert de onafhankelijke OWL-widget en zijn cache niet; verwijder die ook in Omalauncher.

## Techniek en controle

- OWL-bron: `app/src/main/assets/bitcoin-pulse.owl`.
- Import: UTF-8 → raw DEFLATE → base64url zonder padding, in het fragment van de officiële `https://simonschubert.github.io/omarchy-widgets/w#owl1.…`-link. De code wordt hiervoor niet naar een server geüpload.
- Gecontroleerd met de officiële OWL-compiler en echte openbare API-antwoorden voor alle zes combinaties van valuta en interval. De controles omvatten eerste laden, correcte koers- en candlevelden, candlebereik, offline cache en scheiding tussen perioden. De compiler meldt geen waarschuwingen en geen Linux-afhankelijkheid.
- De Android-tests controleren ook het verliesvrij coderen van Unicode in de importlink.
- De import en visuele weergave zijn nog niet op een fysieke telefoon met Omalauncher getest.

Officiële documentatie: [OWL](https://github.com/SimonSchubert/omalauncher/blob/main/app/src/main/assets/widgets/README.md) en [importformaat](https://github.com/SimonSchubert/omalauncher/blob/main/owl/src/main/kotlin/dev/omalauncher/widgets/market/Share.kt).
