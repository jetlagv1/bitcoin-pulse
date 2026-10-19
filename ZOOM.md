# Horizontale zoom — Bitcoin Pulse 1.4.0

## App

Leg twee vingers op een candlegrafiek. Uit elkaar bewegen toont minder candles met meer detail; naar elkaar bewegen toont meer historie. Het bereik is 24–90 candles, met standaard 36. De nieuwste candle blijft rechts vóór de vooruitgeschoven cloud staan. Onder elke grafiek staat het werkelijk getoonde aantal candles. Een tik blijft een candle selecteren; scrollen met één vinger blijft beschikbaar. Schermlezers krijgen de acties Inzoomen en Uitzoomen.

Elke periode heeft een eigen lokaal opgeslagen zoomstand, ook in Combi. Wisselen van valuta behoudt de zoomstand. Een zoomgebaar slaat de nieuwe stand op bij het loslaten; er worden geen extra API-verzoeken gedaan.

## Standaard Android-widgets

De compacte −/+ knoppen staan naast elke grafiektitel, met het aantal candles ertussen. **−** toont meer candles, **+** minder: 24, 36, 60 of 90. De zichtbare knop is 30 dp, binnen een aanraakvlak van 40 dp. Aan de grenzen wordt de betreffende knop uitgeschakeld. Elke widget en elke periode hebben onafhankelijke opgeslagen standen. Verwijderen van een widget verwijdert zijn zoominstellingen.

Voor leesbare grafieken zijn minimaal 620 dp hoogte voor één grafiek, 800 dp voor twee nodig. Widgets tonen maximaal twee grafieken. Vergroot een bestaande widget als hij daarom vraagt. De compacte widget zonder grafiek verandert niet.

## Omalauncher

OWL-widget **4.1** heeft dezelfde compacte knoppen, 24/36/60/90-standen en afzonderlijk opgeslagen zoom voor 4 uur, 1 dag en 1 week. Hij toont naar keuze één of twee verschillende intervallen tegelijk; zie `WIDGET-GRAPHS.md`. Een eerder geïmporteerde kopie moet worden vervangen via **Bitcoin Pulse → Widgets → Toevoegen aan Omalauncher**; een app-update vervangt die kopie niet automatisch.

## Cloud en gegevens

De indicator blijft 20/60/120/30 en gebruikt de volledige bewaarde historie van maximaal 240 echte candles. Alleen het zichtbare venster verandert. De vooruitgeschoven cloud behoudt 29 indexposities; toekomstige prijscandles worden niet verzonnen. De verticale as volgt de zichtbare prijzen en cloudwaarden.

Voor volledige clouddekking zijn `148 + zichtbaar aantal candles` historische candles nodig: 172 bij 24, 184 bij 36, 208 bij 60 en 238 bij 90. Bij onvoldoende historie blijven echte candles zichtbaar en verschijnt een vernieuwmelding. Het getoonde aantal kan lager zijn als de bron minder candles bevat.

## Controle

Native Android-tests sturen echte meerpunts-MotionEvents door een scrollbaar Compose-paneel, controleren beide zoomrichtingen, afzonderlijke Combi-standen en toegankelijkheidsacties. Bitmaptests controleren cloudvulling op alle vier zoomstanden en correcte afhandeling van te korte historie. RemoteViews-tests controleren compacte headers, tellers, grenzen, leesbare grafiekhoogte en afzonderlijke widgetinstellingen.

De officiële OWL-compiler en Compose-renderer controleren de echte asset op 320/360/390 dp breed en 840 dp hoog. Tests tikken door beide zoomgrenzen, controleren tekst en vergelijken de cloudwaarden numeriek met Kotlin. Reproduceer met `tools/verify-omalauncher.ps1 -RecordedData` om bestaande opgenomen bronantwoorden te gebruiken. Dit vervangt geen gebruikstest op een fysieke telefoon.
