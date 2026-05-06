package eisenbahngesellschaft.io;

import eisenbahngesellschaft.model.Strecke;
import eisenbahngesellschaft.model.Bahnhof;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Konkrete Implementierung von {@link InputHandlerInterface} zum Einlesen
 * der Eingabedateien im Projektformat.
 *
 * Erwartetes Format (vereinfachte Beschreibung):
 * - Zeile 1: Kommentar/Metadaten (optional)
 * - Zeile 2: Bahnhofsnamen (z. B. "A B C")
 * - Zeile 3: Kommentar/Metadaten (optional)
 * - Zeile 4: Abstände zwischen Bahnhöfen (z. B. "5 7")
 * - Zeile 5: Kommentar/Metadaten (optional)
 * - Zeile 6: Startzeit der Hinfahrt in Minuten (z. B. "17")
 *
 * Diese Klasse filtert leere Zeilen und Zeilen, die mit "**" beginnen,
 * bevor die relevanten Zeilen geparst werden.
 */
public class ConcreteInputHandler implements InputHandlerInterface {

    /**
     * Liest die Datei unter {@code path} und gibt die daraus erzeugten
     * {@link Strecke}-Objekte zurück.
     *
     * @param path Pfad zur Eingabedatei
     * @return Liste von Strecken in Hinfahrt-Reihenfolge
     * @throws InputHandlerException bei I/O-Fehlern oder ungültigem Dateiformat
     */
    @Override
    public List<Strecke> handleInput(String path) throws InputHandlerException {
        List<String> input;

        try {
            input = Files.readAllLines(Path.of(path))
                    .stream()
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("**")) // Good idea to ignore potential comment lines
                    .toList();

        } catch (IOException e) {
            throw new InputHandlerException("Could not read file: " + e.getMessage());
        }

        List<Strecke> strecken = new ArrayList<>();

        if (input.isEmpty() || input.size() != 6) {
            throw new InputHandlerException("Input file is empty or does not contain the right amount of lines.");
        }

        try {
            // Zeile 1: Bahnhofsnamen (z.B. "A B C")
            String[] bahnhoefeNamen = input.get(1).trim().split("\\s+");

            // Zeile 3: Abstände (z.B. "5 7")
            String[] abstaende = input.get(3).trim().split("\\s+");

            // Zeile 5: Startzeit (z.B. "17")
            int startZeitHinfahrt = Integer.parseInt(input.get(5).trim());

            // Sicherheitscheck: Es muss immer genau einen Abstand weniger geben als Bahnhöfe
            if (bahnhoefeNamen.length - 1 != abstaende.length) {
                throw new InputHandlerException("Die Anzahl der Abstände passt nicht zur Anzahl der Bahnhöfe.");
            }

            // Strecken-Objekte aufbauen
            for (int i = 0; i < abstaende.length; i++) {
                // Konzept-treu: Wir erstellen für JEDE Strecke komplett neue Bahnhof-Instanzen.
                // Dadurch wird der Speicher nicht geteilt und deine Zuweisungs-Logik in den Strategien funktioniert.
                Bahnhof b1 = new Bahnhof(bahnhoefeNamen[i]);
                Bahnhof b2 = new Bahnhof(bahnhoefeNamen[i + 1]);

                // Die initiale Startzeit setzen wir NUR beim allerersten Bahnhof der Hinfahrt.
                if (i == 0) {
                    b1.setHinAbfahrt(startZeitHinfahrt);
                }

                int dauer = Integer.parseInt(abstaende[i]);

                // Setze voraus, dass du einen entsprechenden Konstruktor in 'Strecke' hast.
                Strecke strecke = new Strecke(b1, b2, dauer);
                strecken.add(strecke);
            }

        } catch (NumberFormatException e) {
            throw new InputHandlerException("Fehler beim Parsen der Zahlenwerte (Dauer oder Startzeit).");
        }

        return strecken;
    }
}