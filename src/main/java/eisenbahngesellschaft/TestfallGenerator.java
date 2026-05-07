package eisenbahngesellschaft;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Random;

public class TestfallGenerator {

    public static void main(String[] args) {
        // 26 Bahnhöfe * 500 = 13.000
        int anzahlBahnhoefe = 13000;
        String dateiName = "input/Beispiel_13000_Bahnhoefe.txt";

        try (PrintWriter out = new PrintWriter(dateiName)) {

            out.println("// Stresstest-Datei mit " + anzahlBahnhoefe + " Bahnhoefen");
            out.println("Strecke:");

            // Generiert Bahnhofsnamen: B1, B2, B3 ... B13000
            for (int i = 1; i <= anzahlBahnhoefe; i++) {
                out.print("B" + i + (i == anzahlBahnhoefe ? "" : " "));
            }

            out.println("\n");
            out.println("Abstaende:");

            Random rand = new Random();
            // Generiert 12.999 zufällige Abstände (z.B. zwischen 3 und 25 Minuten)
            for (int i = 1; i < anzahlBahnhoefe; i++) {
                int zufallsDauer = rand.nextInt(23) + 3;
                out.print(zufallsDauer + (i == anzahlBahnhoefe - 1 ? "" : " "));
            }

            out.println("\n");
            out.println("Start Hinfahrt:");
            out.println("12");

            System.out.println("Erfolg! Riesige Testdatei wurde erstellt: " + dateiName);

        } catch (IOException e) {
            System.err.println("Fehler beim Erstellen der Datei: " + e.getMessage());
        }
    }
}
