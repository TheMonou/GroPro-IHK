package de.monou.io;

import de.monou.model.Strecke;
import de.monou.model.Bahnhof;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ConcreteOutputHandler implements OutputHandlerInterface {

    private static final String OUTPUT_PREFIX = "output/output_";

    @Override
    public void createOutput(List<List<Strecke>> strecken, List<String> namen, String filename) {

        try {
            Files.createDirectories(Paths.get("output"));
        } catch (IOException e) {
            throw new OutputHandlerException("Konnte Output-Verzeichnis nicht erstellen: " + e.getMessage());
        }

        String filenameOnly = Paths.get(filename).getFileName().toString();
        if(filenameOnly.endsWith(".txt")) {
            filenameOnly = filenameOnly.substring(0, filenameOnly.length() - 4);
        }
        String filepath = OUTPUT_PREFIX + filenameOnly + ".out";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, false))) {

            // --- HEADER BLOCK: Strecke, Abstände, Start, Anzahl, Mindestdauer ---
            if (!strecken.isEmpty() && strecken.get(0) != null && !strecken.get(0).isEmpty()) {
                List<Strecke> basePlan = strecken.get(0);

                // 1. Strecke: A B C ...
                writer.write("Strecke:\n");
                writer.write(basePlan.get(0).getBahnhof1().getName());
                for (Strecke s : basePlan) {
                    writer.write(" " + s.getBahnhof2().getName());
                }
                writer.write("\n");

                // 2. Abstaende: 5 8 6 ...
                writer.write("Abstaende:\n");
                int sumDauer = 0;
                for (int j = 0; j < basePlan.size(); j++) {
                    writer.write(basePlan.get(j).getDauer() + (j < basePlan.size() - 1 ? " " : ""));
                    sumDauer += basePlan.get(j).getDauer();
                }
                writer.write("\n");

                // 3. Start Hinfahrt:
                writer.write("Start Hinfahrt:\n");
                writer.write(String.format("%02d\n", basePlan.get(0).getBahnhof1().getHinAbfahrt() % 60));

                // 4. Anzahl Bahnhöfe
                int anzahlBahnhoefe = basePlan.size() + 1;
                writer.write("Anzahl Bahnhöfe: " + anzahlBahnhoefe + "\n");

                // 5. Mindestdauer = Summe(Dauer) + Haltezeiten an den Zwischenbahnhöfen
                int mindestDauer = sumDauer + ((anzahlBahnhoefe - 2) * Strecke.EINSTIEGSZEIT);
                writer.write("Mindestdauer : " + mindestDauer + "\n\n");
            }


            // --- STRATEGIEN AUSGABE ---
            for (int i = 0; i < strecken.size(); i++) {
                List<Strecke> fahrplan = strecken.get(i);
                String strategieName = namen.get(i);

                writer.write(strategieName + ":\n");

                if (fahrplan == null || fahrplan.isEmpty()) {
                    writer.write("Kein Fahrplan ermittelt.\n\n");
                    continue;
                }

                // 1. Zeile: Ankunft Hinfahrt (Überspringt Startbahnhof)
                writer.write("An \t\t");
                for (Strecke s : fahrplan) {
                    writer.write(String.format("%02d\t", s.getBahnhof2().getHinAnkunft() % 60));
                }
                writer.write("\n");

                // 2. Zeile: Wartezeiten Hinfahrt
                writer.write("Wa \t\t");
                for (int j = 0; j < fahrplan.size() - 1; j++) {
                    int ankunft = fahrplan.get(j).getBahnhof2().getHinAnkunft();
                    int abfahrt = fahrplan.get(j+1).getBahnhof1().getHinAbfahrt();
                    int pause = (abfahrt - ankunft) - Strecke.EINSTIEGSZEIT;
                    if (pause > 0) {
                        writer.write(String.format("(%02d)\t", pause));
                    } else {
                        writer.write("\t");
                    }
                }
                writer.write("\n");

                // 3. Zeile: Abfahrt Hinfahrt (NUR Bahnhof1 der jeweiligen Strecke = überspringt den letzten Bahnhof)
                writer.write("Ab \t");
                for (Strecke s : fahrplan) {
                    writer.write(String.format("%02d\t", s.getBahnhof1().getHinAbfahrt() % 60));
                }
                writer.write("\n");

                // 4. Zeile: Bahnhofsnamen (MIT KOLLISIONS-CHECK)
                writer.write("\t" + fahrplan.get(0).getBahnhof1().getName());
                for (Strecke s : fahrplan) {
                    if (s.isKollision()) {
                        writer.write(" x " + s.getBahnhof2().getName());
                    } else {
                        writer.write("\t" + s.getBahnhof2().getName());
                    }
                }
                writer.write("\n");

                // 5. Zeile: Abfahrt Rückfahrt (NUR Bahnhof2 der jeweiligen Strecke = überspringt den ersten Bahnhof)
                writer.write("Ab \t\t");
                for (Strecke s : fahrplan) {
                    writer.write(String.format("%02d\t", s.getBahnhof2().getRueckAbfahrt() % 60));
                }
                writer.write("\n");

                // 6. Zeile: Wartezeiten Rückfahrt
                writer.write("Wa \t\t");
                for (int j = 0; j < fahrplan.size() - 1; j++) {
                    int ankunft = fahrplan.get(j+1).getBahnhof1().getRueckAnkunft();
                    int abfahrt = fahrplan.get(j).getBahnhof2().getRueckAbfahrt();
                    int pause = (abfahrt - ankunft) - Strecke.EINSTIEGSZEIT;
                    if (pause > 0) {
                        writer.write(String.format("(%02d)\t", pause));
                    } else {
                        writer.write("\t");
                    }
                }
                writer.write("\n");

                // 7. Zeile: Ankunft Rückfahrt (Überspringt den letzten Bahnhof)
                writer.write("An \t");
                for (Strecke s : fahrplan) {
                    writer.write(String.format("%02d\t", s.getBahnhof1().getRueckAnkunft() % 60));
                }
                writer.write("\n");

                // --- Statistiken berechnen ---
                int[] wartezeiten = berechneWartezeiten(fahrplan);
                int warteHin = wartezeiten[0];
                int warteRueck = wartezeiten[1];

                // Gesamtdauer = Ankunft letzter Bahnhof - Abfahrt erster Bahnhof
                int dauerHin = fahrplan.get(fahrplan.size() - 1).getBahnhof2().getHinAnkunft() - fahrplan.get(0).getBahnhof1().getHinAbfahrt();
                int dauerRueck = fahrplan.get(0).getBahnhof1().getRueckAnkunft() - fahrplan.get(fahrplan.size() - 1).getBahnhof2().getRueckAbfahrt();

                writer.write(String.format("Gesamtdauer Hinfahrt, Rückfahrt : %d, %d\n", dauerHin, dauerRueck));
                writer.write(String.format("Summe Wartezeiten Hinfahrt, Rückfahrt : %d, %d\n", warteHin, warteRueck));
                writer.write("Summe Strafen : " + berechneScore(wartezeiten) + "\n\n");
            }

        } catch (IOException e) {
            throw new OutputHandlerException("Fehler beim Schreiben der Output-Datei: " + e.getMessage());
        }
    }

    @Override
    public int[] berechneWartezeiten(List<Strecke> strecken) {
        int warteHin = 0;
        int warteRueck = 0;

        for (int i = 0; i < strecken.size() - 1; i++) {
            int ankunft = strecken.get(i).getBahnhof2().getHinAnkunft();
            int abfahrt = strecken.get(i + 1).getBahnhof1().getHinAbfahrt();
            int pause = (abfahrt - ankunft) - Strecke.EINSTIEGSZEIT;
            if (pause > 0) warteHin += pause;
        }

        for (int i = 0; i < strecken.size() - 1; i++) {
            int ankunft = strecken.get(i + 1).getBahnhof1().getRueckAnkunft();
            int abfahrt = strecken.get(i).getBahnhof2().getRueckAbfahrt();
            int pause = (abfahrt - ankunft) - Strecke.EINSTIEGSZEIT;
            if (pause > 0) warteRueck += pause;
        }

        return new int[]{warteHin, warteRueck};
    }

    @Override
    public int berechneScore(int[] wartezeiten) {
        int score = 0;
        for (int wartezeit : wartezeiten) {
            score += (int) Math.pow(wartezeit, 2);
        }
        return score;
    }
}