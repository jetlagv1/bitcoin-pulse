# Eén of twee widgetgrafieken — Bitcoin Pulse 1.5.0

Dit geldt voor de grote standaard Android-widget en OWL-widget 4.0 in Omalauncher. De grafiekkeuzes in de app blijven intact: daar kun je nog drie Combi-grafieken gebruiken en met twee vingers zoomen. De compacte Android-widget zonder grafiek verandert niet.

## Bediening

- Kies bovenaan **1 grafiek** of **2 grafieken**. Meer dan twee is in een widget niet mogelijk.
- Tik op de titel met **▾** bij een grafiek en kies **4 uur**, **1 dag** of **1 week**. In de standaardwidget opent een klein native keuzemenu; in Omalauncher verschijnt een keuzerij onder de titel. Bij twee grafieken is het interval van de andere grafiek niet beschikbaar. Alle drie de verschillende paren en beide volgordes zijn mogelijk.
- De compacte **−/+** knoppen en het candleaantal blijven naast de titel staan. Zoom en Ichimoku-cloud werken zoals voorheen.
- Aantal en intervalkeuzes worden per widget bewaard, onafhankelijk van de app. Wisselen naar één grafiek bewaart de tweede keuze; als je in de tussentijd dezelfde periode voor de eerste kiest, wordt de tweede keuze zo nodig aangepast om dubbelen te voorkomen.

## Standaard Android-widget

Bestaande Combi-widgets nemen bij de eerste weergave na deze update maximaal twee van hun eerdere intervallen over. Daarna is hun keuze onafhankelijk van de app. Een eerdere keuze voor één interval blijft één grafiek. Je kunt met de nieuwe knoppen altijd wisselen tussen één en twee.

Minimale hoogte voor leesbare grafieken: **390 dp** voor één en **500 dp** voor twee. Bij minder ruimte toont de widget een verzoek om te vergroten. De intervalkiezer wijzigt uitsluitend de widget waarvan je op de titel tikt. Dubbelen, ongeldige widget-ID's en ongeldige intervallen worden geweigerd; verwijderen van de widget ruimt zijn instellingen op.

## Omalauncher

Verwijder de oude geïmporteerde kopie en voeg de nieuwe toe via **Bitcoin Pulse → Widgets → Toevoegen aan Omalauncher**. Alleen de app bijwerken vervangt de launcher-kopie niet. Dit is **OWL-widget 4.0**; hij begint met twee grafieken: 4 uur en 1 dag.

Begin voor twee grafieken met **6 kolommen × 13 rijen**. De native inhoud is gecontroleerd op 320, 360 en 390 dp breed bij 840 dp hoog, inclusief het open intervalmenu. Voor één grafiek kan de widget kleiner; scrollen blijft beschikbaar als de launcher minder ruimte geeft.

Alleen de één of twee gekozen candlebronnen voor de gekozen valuta zijn actief. Cache en tijdstempel zijn nu afzonderlijk opgeslagen voor elke valuta en elk interval. Een falende bron behoudt alleen zijn eigen eerdere waarden; een nog lege bron toont geen candles van een ander interval of een andere valuta. De cloud blijft berekend uit maximaal 240 echte historische candles per bron.

## Controle

Android-tests controleren migratie van drie widgetgrafieken naar twee, alle intervalparen, onafhankelijke opslag, bewaren van de tweede keuze, het native keuzemenu, uitsluiten van dubbelen en opruimen na verwijderen. De bestaande app-test blijft drie Combi-grafieken en tweevingerzoom controleren.

De officiële OWL-compiler en Compose-renderer testen de echte asset met opgenomen bronantwoorden: beide grafieken, echte menu-/zoomtikken, beide aantallen, cloudwaarden tegenover Kotlin en scheiding van lege/falende caches. De testhost tekent complete Android-frames na layoutwijzigingen zodat de volgende tik de bijgewerkte posities gebruikt. Dit vervangt geen fysieke telefoon- of volledige launchertest.
