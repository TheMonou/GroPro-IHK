package de.monou.strategie;

import de.monou.model.Strecke;
import de.monou.model.Bahnhof;

import java.util.ArrayList;
import java.util.List;

public class EinseitigesWarten implements FahrplanStrategie {

    @Override
    public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
        List<Strecke> fahrplan = new ArrayList<>();

        for (Strecke s : strecken) {
            fahrplan.add(s.copy());
        }

        if (fahrplan.isEmpty()) {
            return fahrplan;
        }

        // --- HINFAHRT (Identisch zur Einfachen Fahrt) ---
        for (int i = 0; i < fahrplan.size(); i++) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            Bahnhof startBhf = aktuelleStrecke.getBahnhof1();
            Bahnhof zielBhf = aktuelleStrecke.getBahnhof2();

            int ankunft = startBhf.getHinAbfahrt() + aktuelleStrecke.getDauer();
            zielBhf.setHinAnkunft(ankunft);

            int abfahrt = ankunft + Strecke.EINSTIEGSZEIT;
            zielBhf.setHinAbfahrt(abfahrt);

            if (i < fahrplan.size() - 1) {
                Bahnhof naechsterStartBhf = fahrplan.get(i + 1).getBahnhof1();
                naechsterStartBhf.setHinAbfahrt(abfahrt);
                naechsterStartBhf.setHinAnkunft(ankunft);
            }
        }

        // --- RÜCKFAHRT (Mit direkter Shift-Logik nach Konzept) ---
        Bahnhof letzterBhf = fahrplan.get(fahrplan.size() - 1).getBahnhof2();
        letzterBhf.setRueckAbfahrt(letzterBhf.getHinAnkunft() + Strecke.EINSTIEGSZEIT);

        for (int i = fahrplan.size() - 1; i >= 0; i--) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            Bahnhof startBhfRueck = aktuelleStrecke.getBahnhof2();
            Bahnhof zielBhfRueck = aktuelleStrecke.getBahnhof1();

            // 1. Initialer Vorschlag für die Zeiten
            int ankunftRueck = startBhfRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
            zielBhfRueck.setRueckAnkunft(ankunftRueck);

            int abfahrtRueck = ankunftRueck + Strecke.EINSTIEGSZEIT;
            zielBhfRueck.setRueckAbfahrt(abfahrtRueck);

            // 2. Strecken-Kollision prüfen
            if (aktuelleStrecke.pruefeKollision()) {

                // BEHEBUNG DURCH DIREKTEN SPRUNG:
                int zielZeit = (startBhfRueck.getHinAnkunft() + Strecke.SICHERHEITSWARTEZEIT) % 60;
                int aktuelleZeit = startBhfRueck.getRueckAbfahrt() % 60;

                // Differenz (Shift) ermitteln
                int shift = (zielZeit - aktuelleZeit) % 60;
                if (shift < 0) {
                    shift += 60; // Verhindert negative Zeiten beim Stundenübergang
                }

                // Abfahrt verschieben
                startBhfRueck.setRueckAbfahrt(startBhfRueck.getRueckAbfahrt() + shift);

                // Folgezeiten für diese Strecke neu berechnen!
                ankunftRueck = startBhfRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
                zielBhfRueck.setRueckAnkunft(ankunftRueck);
                abfahrtRueck = ankunftRueck + Strecke.EINSTIEGSZEIT;
                zielBhfRueck.setRueckAbfahrt(abfahrtRueck);

                // FIX: Kollision wurde behoben, Flag muss zurück auf false!
                aktuelleStrecke.setKollision(false);
            }

            // 3. Prüfung der reinen Bahnhofs-Begegnung
            // Auch ohne Streckenkollision muss die Sicherheitswartezeit IM Bahnhof gelten
            int stationsDiff = (startBhfRueck.getRueckAbfahrt() - startBhfRueck.getHinAnkunft()) % 60;
            if (stationsDiff < 0) stationsDiff += 60;

            if (stationsDiff < Strecke.SICHERHEITSWARTEZEIT) {
                int shift = Strecke.SICHERHEITSWARTEZEIT - stationsDiff;
                startBhfRueck.setRueckAbfahrt(startBhfRueck.getRueckAbfahrt() + shift);

                // Folgezeiten erneut anpassen
                ankunftRueck = startBhfRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
                zielBhfRueck.setRueckAnkunft(ankunftRueck);
                abfahrtRueck = ankunftRueck + Strecke.EINSTIEGSZEIT;
                zielBhfRueck.setRueckAbfahrt(abfahrtRueck);
            }

            // 4. Zeiten für die nächste Iteration sichern
            if (i > 0) {
                Bahnhof vorherigerZielBhf = fahrplan.get(i - 1).getBahnhof2();
                vorherigerZielBhf.setRueckAbfahrt(zielBhfRueck.getRueckAbfahrt());
                vorherigerZielBhf.setRueckAnkunft(zielBhfRueck.getRueckAnkunft());
            }
        }

        return fahrplan;
    }

    @Override
    public String getName() {
        return "Einseitiges Warten";
    }
}