# Omalauncher v2 — ontwerpvoorstel

Dit is een ontwerpvoorstel met fictieve voorbeeldgegevens. De productie-app en OWL-widget zijn hiervoor niet aangepast; de app blijft versie 1.2.4.

- [Interactief voorstel](https://p.superdesign.dev/draft/3151a48b-17ba-48a2-a05a-7be317a26936).
- `omalauncher-v2-proposal.html`: zelfstandig HTML-ontwerp met voorbeeldknoppen voor EUR/USD, 4 uur / 1 dag / 1 week en expliciet niet-functionele live verversing. Tailwind en Iconify worden van hun publieke CDN geladen.
- `omalauncher-v2-proposal.png`: gerenderd voorbeeld bij een viewport van 390 CSS-pixels breed, met pixelschaal 2.

## Voorstel

Een dekkende donkerblauwe kaart, dezelfde kleuren als de standaardwidget, systeem-sans-serif, bredere candlegrafiek met gereserveerde aslabels, groene holle en rode gevulde candles, blokgegevens als volledige horizontale velden, een aparte rij met fee-adviezen en een compacte bron-/ververstijdregel. De koers is de belangrijkste waarde. Het blok toont hoogte, leeftijd, transactieaantal, pool en mediane gerealiseerde fee.

## Controle en grenzen

Op 360 en 390 CSS-pixels breed gecontroleerd: geen horizontale overflow of afgekapte koers-, blok- of feevelden. Kaarthoogte respectievelijk circa 567 en 572 pixels. EUR/USD-conversie, alle drie de candlekeuzes en de melding bij voorbeeldverversing werken. De laatste voorbeeldcandle sluit aan op de getoonde koers. Alle getallen zijn fictief en als voorbeelddata gemarkeerd.

Dit bewijst de weergave van het HTML-voorstel. De daadwerkelijke OWL-uitwerking volgt pas na beoordeling; daarbij moeten de maten, lettertypen, kleuren en tekstvelden met Omalaunchers eigen renderer worden gecontroleerd, ook op een telefoon. De huidige rijproblemen hangen samen met OWL-tekstvelden die standaard de volledige rijbreedte innemen; in de uitwerking moeten beide velden expliciet ruimte krijgen.

De persoonlijke testfoto is niet naar Superdesign of GitHub geüpload. Het voorstel bevat geen afbeelding of kopie van die foto.
