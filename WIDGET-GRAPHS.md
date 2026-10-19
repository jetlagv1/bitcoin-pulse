# Widgetgrafieken — Bitcoin Pulse 1.5.4

Open **Bitcoin Pulse → Widgets → Aantal grafieken in de widget**. Kies één of twee. Dit geldt direct voor bestaande standaardwidgets en voor nieuwe widgets; de gekozen intervallen blijven bewaard. De widget heeft geen aparte rij met knoppen voor het aantal grafieken meer. Het dashboard behoudt drie Combi-grafieken en tweevingerzoom.

De widget begint op **4×5** en blijft schaalbaar naar **4×6** en groter. De launcher bepaalt de werkelijke celmaten. Ondersteunde minimumhoogte voor de standaardwidget is 320 dp. De compacte indeling geeft de beschikbare hoogte aan de grafieken. Bij één grafiek blijven alle gegevens zichtbaar, inclusief mediane blokfee, mempool, bronvermelding en candle-status; de grafiek krijgt op elk formaat alle resterende hoogte. Alleen bij de bestaande compacte tweegrafiekweergave worden extra netwerkregels en, onder 400 dp, de feevelden verborgen.

Tik op de grafiektitel met **▾** om de keuzerij **4 uur / 1 dag / 1 week** binnen de widget te openen. Na je keuze sluit de rij; de app en het oude activiteitenscherm worden niet geopend. Dubbelen zijn bij twee grafieken uitgesloten. De **−/+** knoppen onthouden de zoom per interval; Cloud Aan/Uit geldt voor de getoonde grafieken. Wisselen naar één grafiek bewaart het tweede interval.

## Omalauncher

Kies eerst het aantal in de app en gebruik daarna **Toevoegen aan Omalauncher**. Verwijder de oude launcher-kopie en bevestig de nieuwe import. OWL 4.4 neemt het gekozen aantal mee in de importlink. Een wijziging in de app verandert een al geïmporteerde, zelfstandige widget niet automatisch. Losse imports van de OWL-bron gebruiken standaard twee grafieken.

De widget begint op 4×5 en kan naar 4×6 worden vergroot. Bij één grafiek schaalt de grafiek binnen de widget mee, ook tijdens het openen van de intervalkeuze. De tweegrafiekweergave behoudt scrollen bij weinig ruimte. EUR/USD en intervallen worden in de widget gekozen. Elke valuta en elk interval heeft een afzonderlijke cache; alleen geselecteerde candlebronnen zijn actief.

## Controle

Android-tests tekenen de echte RemoteViews voor één en twee grafieken bij meerdere hoogtes, controleren grafiek- en footergrenzen, appinstellingen voor bestaande en nieuwe widgets, intervalkeuze, cloud en zoom. De officiële OWL-compiler en renderer controleren de echte asset met opgenomen bronantwoorden en bediening via Android-aanraakgebeurtenissen. Dit vervangt geen fysieke launchertest.

## Wisselen in de widget (1.5.4)

Tik op **1 ↔** of **2 ↔** naast de verversknop om direct tussen één en twee grafieken te wisselen. Het getal is het huidige aantal. De knop gebruikt de bestaande footer; er komt geen extra rij bij. De app blijft gesloten. Intervalkeuzes en zoom worden bewaard. Een open intervalmenu sluit bij het wisselen. De instelling geldt alleen voor deze widget; de keuze in de app blijft ook beschikbaar.

In Omalauncher is hiervoor OWL 4.4 nodig. Importeer die eenmalig opnieuw via de app. Daarna kun je het aantal rechtstreeks in de widget wijzigen zonder opnieuw te importeren. Als de eerste periode tijdens de weergave met één grafiek gelijk is geworden aan de verborgen tweede, krijgt de tweede bij het inschakelen een ander interval.
