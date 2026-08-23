package eisenbahngesellschaft.strategie;

import eisenbahngesellschaft.model.Strecke;
import eisenbahngesellschaft.model.Bahnhof;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategie für eine einfache Hin- und Rückfahrt ohne explizite Warteoptimierung.
 *
 * Die Methode berechnet zunaechst die Zeiten für die Hinfahrt entlang aller
 * Strecken und anschliessend die Rückfahrt in umgekehrter Reihenfolge. Dabei
 * werden Kollisionsprüfungen pro Strecke durchgeführt.
 */
public class EinfacheFahrt implements FahrplanStrategie {

    /**
     * Ermittelt einen Fahrplan für eine durchgehende Hin- und Rückfahrt.
     *
     * Die übergebenen Strecken werden als Kopien verarbeitet, sodass die
     * Fahrplanberechnung keine Seiteneffekte verursacht
     *
     * @param strecken geordnete Liste der Strecken zwischen den Bahnhoefen
     * @return berechnete Liste mit gesetzten Zeiten für Hin- und Rückfahrt
     */
    @Override
    public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
        List<Strecke> fahrplan = new ArrayList<>();

        // echte Kopie zum Verhindern von Seiteneffekten
        for (Strecke s : strecken) {
            fahrplan.add(s.copy());
        }

        if (fahrplan.isEmpty()) {
            return fahrplan;
        }

        // Der erste Bahnhof hat keine Ankunftszeit auf der Hinfahrt und keine Abfahrtszeit auf der Rückfahrt
        fahrplan.get(0).getBahnhof1().setHinAnkunft(-1);
        fahrplan.get(0).getBahnhof1().setRueckAbfahrt(-1);

        // Der letzte Bahnhof hat keine Abfahrtszeit auf der Hinfahrt und keine Ankunftszeit auf der Rückfahrt
        fahrplan.get(strecken.size()-1).getBahnhof2().setRueckAnkunft(0);
        fahrplan.get(strecken.size()-1).getBahnhof2().setHinAbfahrt(0);

        // --- HINFAHRT ---
        for (int i = 0; i < fahrplan.size(); i++) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            Bahnhof startBhf = aktuelleStrecke.getBahnhof1();
            Bahnhof zielBhf = aktuelleStrecke.getBahnhof2();

            // Ankunftszeit = Abfahrtszeit des vorherigen Bahnhofs + Fahrtdauer der Strecke
            int ankunft = startBhf.getHinAbfahrt() + aktuelleStrecke.getDauer();
            zielBhf.setHinAnkunft(ankunft);

            // Abfahrtszeit = Ankunftszeit + Einstiegszeit
            int abfahrt = ankunft + Strecke.EINSTIEGSZEIT;
            zielBhf.setHinAbfahrt(abfahrt);

            // Zeiten des aktuellen Zielbahnhofs in den Startbahnhof der nächsten Iteration kopieren
            if (i < fahrplan.size() - 1) {
                Bahnhof naechsterStartBhf = fahrplan.get(i + 1).getBahnhof1();
                naechsterStartBhf.setHinAbfahrt(abfahrt);
                naechsterStartBhf.setHinAnkunft(ankunft);
            }
        }

        // --- RÜCKFAHRT ---
        // Die Rückfahrt startet am letzten Bahnhof, nach Ankunft der Hinfahrt + Einstiegszeit
        Bahnhof letzterBhf = fahrplan.get(fahrplan.size() - 1).getBahnhof2();
        letzterBhf.setRueckAbfahrt(letzterBhf.getHinAnkunft() + Strecke.EINSTIEGSZEIT);

        // Iteration rückwärts durch den Fahrplan
        for (int i = fahrplan.size() - 1; i >= 0; i--) {
            Strecke aktuelleStrecke = fahrplan.get(i);

            // Für die Rückfahrt vertauschen sich logisch Start und Ziel
            Bahnhof startBhfRueck = aktuelleStrecke.getBahnhof2();
            Bahnhof zielBhfRueck = aktuelleStrecke.getBahnhof1();

            // Ankunftszeit = Abfahrtszeit + Dauer
            int ankunftRueck = startBhfRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
            zielBhfRueck.setRueckAnkunft(ankunftRueck);

            // Abfahrtszeit = Ankunftszeit + Einstiegszeit
            int abfahrtRueck = ankunftRueck + Strecke.EINSTIEGSZEIT;
            zielBhfRueck.setRueckAbfahrt(abfahrtRueck);

            // Zeiten in die nächste Iteration (rückwärts) kopieren
            if (i > 0) {
                Bahnhof vorherigerZielBhf = fahrplan.get(i - 1).getBahnhof2();
                vorherigerZielBhf.setRueckAbfahrt(abfahrtRueck);
                vorherigerZielBhf.setRueckAnkunft(ankunftRueck);
            }

            // Kollsionsprüfung für die aktuelle Strecke -> setzt ggfs. eine Flag
            aktuelleStrecke.pruefeKollision();
        }

        return fahrplan;
    }

    /**
     * Liefert den Anzeigenamen dieser Fahrplanstrategie.
     *
     * @return Name der Strategie
     */
    @Override
    public String getName() {
        return "Einfache Fahrt";
    }
}