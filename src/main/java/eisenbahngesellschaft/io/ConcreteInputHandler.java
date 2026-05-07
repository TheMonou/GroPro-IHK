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
                    .filter(line -> !line.startsWith("//"))
                    .toList();

        } catch (IOException e) {
            throw new InputHandlerException(
                    "Eingabedatei konnte nicht gelesen werden: "
                    + e.getMessage()
            );
        }

        List<Strecke> strecken = new ArrayList<>();

        if (input.isEmpty() || input.size() != 6) {
            throw new InputHandlerException(
                    "Eingabedatei ist entweder leer oder hat nicht das erwartete Format (6 relevante Zeilen)."
            );
        }

        try {
            // Zeile 1: Bahnhofsnamen (z.B. "A B C")
            String[] bahnhoefeNamen = input.get(1).trim().split("\\s+");

            // Zeile 3: Abstände (z.B. "5 7")
            String[] abstaende = input.get(3).trim().split("\\s+");

            // Zeile 5: Startzeit (z.B. "17")
            int startZeitHinfahrt = Integer.parseInt(input.get(5).trim());

            if(bahnhoefeNamen.length < 2) {
                throw new InputHandlerException("Es müssen mindestens zwei Bahnhöfe angegeben werden.");
            }
            // Es muss immer genau einen Abstand weniger geben als Bahnhöfe
            if (bahnhoefeNamen.length - 1 != abstaende.length) {
                throw new InputHandlerException("Die Anzahl der Abstände passt nicht zur Anzahl der Bahnhöfe.");
            }


            // Strecken-Objekte aufbauen
            for (int i = 0; i < abstaende.length; i++) {

                Bahnhof b1 = new Bahnhof(bahnhoefeNamen[i]);
                Bahnhof b2 = new Bahnhof(bahnhoefeNamen[i + 1]);

                // Die initiale Startzeit
                if (i == 0) {
                    b1.setHinAbfahrt(startZeitHinfahrt);
                }

                int dauer = Integer.parseInt(abstaende[i]);

                Strecke strecke = new Strecke(b1, b2, dauer);
                strecken.add(strecke);
            }

        } catch (NumberFormatException e) {
            throw new InputHandlerException("Fehler beim Parsen der Zahlenwerte (Dauer oder Startzeit).");
        }

        for(Strecke strecke:  strecken) {
            if(strecke.getDauer() + Strecke.SICHERHEITSWARTEZEIT > 30) {
                throw new InputHandlerException(
                        "Die Summe aus Fahrtdauer und Sicherheitswartezeit darf nicht mehr als 30 Minuten sein. "
                        + "Bite die Strecke: "  + strecke.toString() + " überprüfen."
                );
            }
            if(strecke.getDauer() < 1){
                throw new InputHandlerException(
                        "Die Fahrtdauer einer Strecker muss mindest 1 Minute betragen."
                );
            }
        }
        return strecken;
    }
}