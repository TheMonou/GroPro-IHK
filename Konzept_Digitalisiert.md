# Konzept: Entwicklung eines Softwaresystems (Fahrplanermittler)
*Author: Alassane Mohammed*

## 1. Aufgabenanalyse
### 1.1 Problemstellung
Die MATSE-Eisenbahngesellschaft benötigt ein Softwaresystem zur Ermittlung von Fahrplänen für eingleisige Eisenbahnlinien. Das Problem bei eingleisigen Strecken ist das Risiko für Kollisionen, da es dazu kommen kann, dass Züge dieselbe Teilstrecke zur selben Zeit befahren wollen. Im Zuge dessen soll ein Softwaresystem mögliche Fahrpläne für gegebene Strecken bestimmen.

### 1.2 Anforderungsanalyse
Das Softwaresystem zur Fahrplanermittlung soll Eingabedaten bezüglich der Bahnhöfe, der Fahrzeiten zwischen Strecken und der initialen Startzeit einlesen, unter verschiedenen Strategien erstellte Lösungen ermitteln und diese nach spezifizierten Formaten zusammen mit bestimmten Kennzahlen ausgeben. 

Es sind drei verschiedene Lösungsstrategien vorgegeben:
* **Einfache Fahrt**: Die Züge fahren so schnell wie möglich weiter, ohne dass auf Kollisionen geachtet wird.
* **Einseitiges Warten**: Die Hinfahrt wählt die schnellstmögliche Verbindung und die Rückfahrt wartet bei Kollisionen auf entgegenkommende Züge.
* **Beidseitiges Warten**: Die Wartezeiten werden sinnvoll auf die Züge der Hin- und Rückfahrt verteilt.

---

## 2. Verfahrensbeschreibung
### 2.1 Eingabe und Dateninitialisierung
Die Verarbeitung der Eingabedaten erfolgt über ein Strategy Pattern. Das Interface `InputHandlerInterface` stellt die abstrakte Schnittstelle dar. Die Klasse `ConcreteInputHandler` implementiert das Interface, liest die Daten aus der Datei aus und generiert eine Liste von `Strecke`-Objekten.

### 2.2 Kollisionserkennung
Die Kollisionserkennung findet statt, indem geprüft wird, ob Züge der Hin- und Rückfahrt dieselbe Strecke zu überschneidenden Zeiträumen belegen. Dabei muss die obligatorische Sicherheitswartezeit berücksichtigt werden.

### 2.3 Wartezeitermittlung
Die Wartezeitermittlung berechnet die auszugebenden Wartezeiten. Es muss beachtet werden, dass sowohl die Mindesthaltezeit eines Zuges an einem Bahnhof als auch die Sicherheitswartezeit bei der Begegnung von Zügen eingehalten werden.

### 2.4 Strategien
#### 2.4.1 Entwurfsmuster (Strategy Pattern)
Da die Fahrplanermittlung mit allen drei Strategien anhand derselben Parameter erfolgt und Ausgaben derselben Form erzeugt, bietet es sich an, das Strategy-Pattern zu verwenden. Das Interface `FahrplanStrategie` definiert den Methodenaufruf zur Problemlösung. An der aufrufenden Stelle kann dann zur Laufzeit die Strategie gewechselt werden, ohne den Code zu verändern.

#### 2.4.2 Einfache Fahrt
Implementiert das Interface `FahrplanStrategie`. Der Algorithmus geht die Liste der Strecken durch und berechnet für jeden Bahnhof die Ankunfts- und Abfahrtszeiten für die Hin- und Rückfahrt. Dies geschieht in der schnellstmöglichen Zeit ohne Rücksicht auf potenzielle Kollisionen.

#### 2.4.3 Einseitiges Warten
Berechnet die Hinfahrt exakt wie bei der "Einfachen Fahrt". Für die Rückfahrt wird nach jeder Streckenberechnung eine Kollisionsüberprüfung durchgeführt. Sollte es zu einer Kollision kommen, wird die Abfahrtszeit des Zuges der Rückfahrt so weit nach hinten gesetzt, bis keine Kollision mehr vorliegt und die Sicherheitswartezeit eingehalten ist.

#### 2.4.4 Beidseitiges Warten
Führt zunächst eine Fahrplanerstellung der Strategie des einseitigen Wartens durch. Danach werden die Indizes der von Wartezeit betroffenen Strecken abgerufen. Die Wartezeiten werden schrittweise sinnvoll auf Hin- und Rückfahrt aufgeteilt. Anschließend wird erneut auf Kollisionen geprüft und der Vorgang wiederholt, bis der Score minimiert ist.

### 2.5 Fahrplanermittler
Der `FahrplanErmittler` ist die zentrale Steuerungskomponente des Programms. Sie initialisiert den Eingabe-Handler, leitet die eingelesenen Daten an die Strategien weiter, lässt sich jeweils eine Lösung zurückgeben und leitet diese an den Output-Handler weiter.

### 2.6 Ausgabe
Die Ausgabe findet ebenfalls mithilfe des Interfaces `OutputHandlerInterface` statt. Die Klasse `ConcreteOutputHandler` formatiert die Lösungen der Strategien in eine tabellarische Form. Sie berechnet die Gesamtdauer, die Summe der Wartezeiten sowie den finalen Score (Strafen) und gibt diese in der Konsole bzw. Datei aus.

---

## 3. UML-Entwurf / Objektorientierte Konzeption

### 3.1 Schnittstellen (Interfaces)
* `<<Interface>> InputHandlerInterface`
    * `+ handleInput(path: String): List<Strecke>`
* `<<Interface>> OutputHandlerInterface`
    * `+ createOutput(strecken: List<Strecke>, name: String): void`
    * `+ berechneWartezeiten(strecken: List<Strecke>): int`
    * `+ berechneScore(wartezeiten: int): int`
* `<<Interface>> FahrplanStrategie`
    * `+ ermittleFahrplan(strecken: List<Strecke>): List<Strecke>`

### 3.2 Strategie-Implementierungen
* `EinfacheFahrt` (implementiert `FahrplanStrategie`)
* `EinseitigesWarten` (implementiert `FahrplanStrategie`)
* `BeidseitigesWarten` (implementiert `FahrplanStrategie`)

### 3.3 Datenmodell (Model)
* **Bahnhof**
    * `- hinAnkunft: int`
    * `- hinAbfahrt: int`
    * `- rueckAnkunft: int`
    * `- rueckAbfahrt: int`
    * `- name: String`
* **Strecke**
    * `- bahnhof1: Bahnhof`
    * `- bahnhof2: Bahnhof`
    * `- kollision: boolean`
    * `- dauer: int`

### 3.4 Controller / Main
* **FahrplanErmittler**
    * `- strategien: List<FahrplanStrategie>`
    * `- inputHandler: InputHandlerInterface`
    * `- outputHandler: OutputHandlerInterface`
    * `+ ermittleFahrplan(path: String): void`
* **Main**
    * `+ main(args: String[]): void`

![[UML(grob).png]]

