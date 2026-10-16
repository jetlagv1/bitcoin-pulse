# Bitcoin Pulse — uploaden naar Google Play

## Geleverde uploadversie

- Bestand: **Bitcoin-Pulse-1.2.2-vc5-release.aab**
- Appnaam: **Bitcoin Pulse**
- Package/application ID: `com.buildapptest.bitcoinpulse`
- Versie: **1.2.2**, versionCode **5**
- Minimaal Android 8.0; target Android 16 / API 36.
- Niet-debuggable release, ondertekend met een aparte RSA-uploadsleutel.

De AAB is een uploadbestand voor Play Console, geen bestand dat je op je telefoon installeert. De app is hiermee nog niet gepubliceerd. Een geslaagde technische bundlecontrole is geen garantie op goedkeuring door Google.

## Uploaden

1. Open [Google Play Console](https://play.google.com/console/) met je eigen ontwikkelaarsaccount. Rond eventuele account- en identiteitsverificatie af.
2. Maak een nieuwe app aan. Kies Bitcoin Pulse als naam, de gewenste taal, app in plaats van game en je distributiekeuze. Het package-ID wordt bij de eerste bundle-upload vastgelegd; verander dit daarna niet voor updates.
3. Begin bij **Testen en releasen → Testen → Intern testen**. Maak een release en upload `Bitcoin-Pulse-1.2.2-vc5-release.aab`. Verwijder eerst een afgewezen upload met versiecode 4 uit de conceptrelease. Wacht totdat de nieuwe bundle verwerkt en zichtbaar is als versiecode 5 voordat je doorgaat.
4. Gebruik **Play App Signing** en laat Google de app-signingsleutel genereren. De meegeleverde bundle is al ondertekend met de aparte uploadsleutel. Je hoeft de privésleutel of het wachtwoord niet naar Google of een andere partij te sturen.
5. Voeg testgebruikers toe, sla de release op en volg de controle-instructies in Play Console. Test de door Play geleverde app, inclusief widgets, vernieuwen, EUR/USD, Combi en offline gedrag, op echte telefoons voordat je een openbare release aanvraagt.
6. Maak de winkelvermelding en App-content-secties af: beschrijving, contactgegevens, app-icoon, echte telefoonscreenshots, feature graphic, doelgroep, contentclassificatie, advertenties, eventuele financiële-functievragen en Gegevensveiligheid. `STORE-LISTING-NL.md` bevat een concepttekst. Deze publicatie-assets zijn niet automatisch door een AAB inbegrepen.
7. Publiceer de meegeleverde `privacy-policy.html` op een eigen actieve, openbare HTTPS-webpagina en vul die URL in Play Console in. Vermeld een bereikbaar privacycontact en controleer dat dit overeenkomt met de ontwikkelaarscontactgegevens van je winkelvermelding. Dezelfde inhoud is in de app bereikbaar via **Privacyverklaring**. De huidige tekst verwijst voor contact naar de officiële winkelvermelding.
8. Dien pas daarna de gewenste test- of productierelease in. Voor persoonlijke accounts die na 13 november 2023 zijn aangemaakt kan eerst een gesloten test met minimaal 12 voortdurend aangemelde testers gedurende 14 dagen en een aanvraag voor productietoegang vereist zijn.

## Gegevensveiligheid invullen

De code bevat geen eigen account, advertenties, analysetracker, wallet, handel of betalingen. Instellingen en openbare datacaches staan lokaal; Android-back-up is uitgeschakeld. HTTPS-verzoeken naar Kraken en mempool.space geven deze diensten wel het openbare IP-adres en technische verzoekgegevens. Beoordeel hun verwerking en eventuele logboeken bij het beantwoorden van Gegevensveiligheid. Neem geen ongetoetste verklaring over dat er categorisch geen gegevens worden verzameld. Jij bent als uitgever verantwoordelijk voor de uiteindelijke antwoorden en het overeenkomen daarvan met de privacyverklaring.

Er is geen login of afgeschermd account nodig voor beoordeling van de app. De app heeft internet nodig voor actuele gegevens. De grafieken bevatten in de echte app geen voorbeelddata.

## Sleutels en toekomstige updates

De privésleutel staat uitsluitend op deze computer, buiten de bronbundel:

`../.tools/android-build/release-signing/bitcoin-pulse-upload.p12`

Het bijbehorende lokale bestand `upload-key.json` bevat het wachtwoord en de alias. Bewaar beide privé en maak zelf een beveiligde back-up. Ze zijn **niet** naar Dropbox gekopieerd en **niet** in de bron-ZIP opgenomen. `upload-certificate.pem` is alleen het openbare certificaat en mag wel gedeeld worden.

Voor een volgende upload: verhoog `versionCode`, bouw met dezelfde uploadsleutel en upload via dezelfde app in Play Console. De eerdere APK's waren debug-ondertekend; een door Play met de productie-appsleutel ondertekende installatie kan niet rechtstreeks als update over zo'n debug-installatie worden geplaatst. Test zo nodig op een ander toestel of verwijder de debug-installatie nadat je hebt vastgesteld dat je de lokale instellingen kunt missen.

## Technische controle en bouwen

`VERIFICATION.json` beschrijft de controles van de geleverde bundle. De bestaande 31 JVM-tests en release-lint zijn uitgevoerd; geen fysieke telefoon- of emulatorrun voor deze release. De privacyweergave is een native dialoog; on-device bediening moet in de interne test worden gecontroleerd.

Op deze computer, vanuit de projectmap:

```powershell
python tools/prepare-play-key.py
./tools/build-play-release.ps1 -Offline
python tools/verify-play-bundle.py
python tools/package-play-release.py
```

Op een andere computer kun je je eigen uploadsleutel gebruiken en vóór `gradlew.bat :app:bundleRelease` de omgevingsvariabelen `PULSE_UPLOAD_STORE_FILE`, `PULSE_UPLOAD_STORE_PASSWORD`, `PULSE_UPLOAD_KEY_ALIAS` en `PULSE_UPLOAD_KEY_PASSWORD` instellen. Bewaar geen wachtwoorden in broncode of versiebeheer. De bronbundel bevat geen machinegebonden SDK of uploadsleutel.

## Officiële referenties — gecontroleerd 16 oktober 2026

- [Vereist target API-niveau](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Android App Signing en uploadsleutels](https://developer.android.com/studio/publish/app-signing)
- [16 KB-paginagrootte](https://developer.android.com/guide/practices/page-sizes)
- [Gebruikersgegevens en privacybeleid](https://support.google.com/googleplay/android-developer/answer/10144311)
- [Testvereisten voor nieuwe persoonlijke accounts](https://support.google.com/googleplay/android-developer/answer/14151465)
