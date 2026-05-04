package de.monou.strategie;

import de.monou.model.Strecke;
import de.monou.model.Bahnhof;

import java.util.ArrayList;
import java.util.List;

public class EinfacheFahrt implements FahrplanStrategie {

    private static final int HALTEZEIT = 1; // 1 Minute Sicherheitswartezeit

    @Override
    public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
        List<Strecke> fahrplan = new ArrayList<>();

        // Nutzt deine geplante Deep-Copy Methode aus der Strecke-Klasse
        for (Strecke s : strecken) {
            fahrplan.add(s.copy());
        }

        if (fahrplan.isEmpty()) {
            return fahrplan;
        }

        // --- HINFAHRT ---
        for (int i = 0; i < fahrplan.size(); i++) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            Bahnhof startBhf = aktuelleStrecke.getBahnhof1();
            Bahnhof zielBhf = aktuelleStrecke.getBahnhof2();

            // Ankunft = Abfahrt vorheriger Bahnhof + Dauer der Strecke
            int ankunft = startBhf.getHinAbfahrt() + aktuelleStrecke.getDauer();
            zielBhf.setHinAnkunft(ankunft);

            // Abfahrt = Ankunft + 1 Minute Haltezeit
            int abfahrt = ankunft + HALTEZEIT;
            zielBhf.setHinAbfahrt(abfahrt);

            // KONZEPT-UMSETZUNG: Zeiten in den Startbahnhof der nächsten Iteration kopieren
            if (i < fahrplan.size() - 1) {
                Bahnhof naechsterStartBhf = fahrplan.get(i + 1).getBahnhof1();
                naechsterStartBhf.setHinAbfahrt(abfahrt);
                naechsterStartBhf.setHinAnkunft(ankunft); // Optional, aber gut für Vollständigkeit
            }
        }

        // --- RÜCKFAHRT ---
        // Die Rückfahrt startet am letzten Bahnhof, 1 Minute nach Ankunft der Hinfahrt
        Bahnhof letzterBhf = fahrplan.get(fahrplan.size() - 1).getBahnhof2();
        letzterBhf.setRueckAbfahrt(letzterBhf.getHinAnkunft() + HALTEZEIT);

        // Iteration rückwärts durch den Fahrplan
        for (int i = fahrplan.size() - 1; i >= 0; i--) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            // Für die Rückfahrt vertauschen sich logisch Start und Ziel
            Bahnhof startBhfRueck = aktuelleStrecke.getBahnhof2();
            Bahnhof zielBhfRueck = aktuelleStrecke.getBahnhof1();

            // Ankunft = Abfahrt + Dauer
            int ankunftRueck = startBhfRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
            zielBhfRueck.setRueckAnkunft(ankunftRueck);

            // Abfahrt = Ankunft + 1 Minute Haltezeit
            int abfahrtRueck = ankunftRueck + HALTEZEIT;
            zielBhfRueck.setRueckAbfahrt(abfahrtRueck);

            // KONZEPT-UMSETZUNG: Zeiten in die nächste Iteration (rückwärts) kopieren
            if (i > 0) {
                Bahnhof vorherigerZielBhf = fahrplan.get(i - 1).getBahnhof2();
                vorherigerZielBhf.setRueckAbfahrt(abfahrtRueck);
                vorherigerZielBhf.setRueckAnkunft(ankunftRueck);
            }
        }

        return fahrplan;
    }
}