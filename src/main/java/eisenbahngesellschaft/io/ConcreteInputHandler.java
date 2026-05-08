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
 * Diese Klasse filtert leere Zeilen und Zeilen, die mit "//" beginnen,
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
            // Die Startzeit muss zwischen im Intervall [0; 59] liegen
            if(startZeitHinfahrt < 0 || startZeitHinfahrt > 59) {
                throw new InputHandlerException("Die Startzeit der Hinfahrt muss zwischen 0 und 59 Minuten liegen.");
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
                        + "Bitte die Strecke: "  + strecke.toString() + " überprüfen."
                );
            }
            if(strecke.getDauer() < 1){
                throw new InputHandlerException(
                        "Die Fahrtdauer einer Strecke muss mindest 1 Minute betragen. "
                        + "Bitte die Strecke: "  + strecke.toString() + " überprüfen."
                );
            }
        }
        return strecken;
    }
}