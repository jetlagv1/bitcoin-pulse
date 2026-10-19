# Horizontale zoom — Bitcoin Pulse 1.6.0

## App

Leg twee vingers op een candlegrafiek. Uit elkaar bewegen toont minder candles met meer detail; naar elkaar bewegen toont meer historie. Het bereik is 24–270 candles, met standaard 36. De nieuwste candle blijft rechts vóór de vooruitgeschoven cloud staan. Onder elke grafiek staat het werkelijk getoonde aantal candles. Een tik blijft een candle selecteren; scrollen met één vinger blijft beschikbaar. Schermlezers krijgen de acties Inzoomen en Uitzoomen.

Elke periode heeft een eigen lokaal opgeslagen zoomstand, ook in Combi. Wisselen van valuta behoudt de zoomstand. Een zoomgebaar slaat de nieuwe stand op bij het loslaten; er worden geen extra API-verzoeken gedaan.

## Standaard Android-widgets

De compacte −/+ knoppen staan naast elke grafiektitel, met het aantal candles ertussen. **−** toont meer candles, **+** minder: 24, 36, 60, 90, 120, 180 of 270. De achtergrond is 26 × 22 dp binnen een aanraakvlak van 36 × 32 dp in de compacte indeling; in de uitgebreide indeling 30 × 30 dp binnen 40 × 40 dp. Aan de grenzen wordt de betreffende knop uitgeschakeld. Elke widget en elke periode hebben onafhankelijke opgeslagen standen. Verwijderen van een widget verwijdert zijn zoominstellingen.

De widget begint op 4×5 en blijft schaalbaar naar 4×6 en groter, met een minimumhoogte van 320 dp. Eén grafiek gebruikt alle resterende hoogte met behoud van de netwerkgegevens. Widgets tonen maximaal twee grafieken. De compacte widget zonder grafiek verandert niet.

## Omalauncher

OWL-widget **4.5** heeft dezelfde compacte knoppen, 24/36/60/90/120/180/270-standen en afzonderlijk opgeslagen zoom voor 4 uur, 1 dag en 1 week. Hij toont naar keuze één of twee verschillende intervallen tegelijk; zie `WIDGET-GRAPHS.md`. Een eerder geïmporteerde kopie moet worden vervangen via **Bitcoin Pulse → Widgets → Toevoegen aan Omalauncher**; een app-update vervangt die kopie niet automatisch.

## Cloud en gegevens

De indicator blijft 20/60/120/30 en gebruikt de volledige bewaarde historie van maximaal 480 echte candles. Alleen het zichtbare venster verandert. De vooruitgeschoven cloud behoudt 29 indexposities; toekomstige prijscandles worden niet verzonnen. De verticale as volgt de zichtbare prijzen en cloudwaarden.

Voor volledige clouddekking zijn `148 + zichtbaar aantal candles` historische candles nodig: 172 bij 24, 184 bij 36, 208 bij 60, 238 bij 90, 268 bij 120, 328 bij 180 en 418 bij 270. Bij onvoldoende historie blijven echte candles zichtbaar en verschijnt een vernieuwmelding. Het getoonde aantal kan lager zijn als de bron minder candles bevat.

## Controle

Native Android-tests sturen echte meerpunts-MotionEvents door een scrollbaar Compose-paneel, controleren beide zoomrichtingen, afzonderlijke Combi-standen en toegankelijkheidsacties. Bitmaptests controleren cloudvulling op alle zeven zoomstanden en correcte afhandeling van te korte historie. RemoteViews-tests controleren compacte headers, tellers, grenzen, leesbare grafiekhoogte en afzonderlijke widgetinstellingen.

De officiële OWL-compiler en Compose-renderer controleren de echte asset op 320/360/390 dp breed en 840 dp hoog. Tests tikken door beide zoomgrenzen, controleren tekst en vergelijken de cloudwaarden numeriek met Kotlin. Reproduceer met `tools/verify-omalauncher.ps1 -RecordedData` om bestaande opgenomen bronantwoorden te gebruiken. Dit vervangt geen gebruikstest op een fysieke telefoon.
