package eisenbahngesellschaft.strategie;

import eisenbahngesellschaft.model.Strecke;
import eisenbahngesellschaft.model.Bahnhof;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategie für eine Hin- und Rückfahrt mit einseitigem Warten.
 * <p>
 *
 *  Die Hinfahrt wird direkt berechnet, waehrend auf der Rückfahrt bei
 *  Kollisionen oder zu kurzer Wartezeit im Bahnhof die Abfahrt gezielt
 *  verschoben wird. Dadurch wird versucht, Konflikte nur auf einer Fahrtrichtung
 *  zu loesen.
 * </p>
 */
public class EinseitigesWarten implements FahrplanStrategie {

    /**
     * Ermittelt einen Fahrplan für eine durchgehende Hin- und Rückfahrt mit
     * Korrekturen auf der Rückfahrt.
     *
     * Die übergebenen Strecken werden als Kopien verarbeitet, um Seiteneffekte zu verhindern. Bei der
     * Rückfahrt werden bei Bedarf Abfahrtszeiten angepasst, um Kollisionen zu
     * vermeiden und die Sicherheitswartezeit einzuhalten.
     *
     * @param strecken geordnete Liste der Strecken zwischen den Bahnhoefen
     * @return berechnete Liste mit gesetzten Zeiten für Hin- und Rückfahrt
     */
    @Override
    public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
        List<Strecke> fahrplan = new ArrayList<>();

        for (Strecke s : strecken) {
            fahrplan.add(s.copy());
        }

        if (fahrplan.isEmpty()) {
            return fahrplan;
        }

        // Der erste Bahnhof hat keine Ankunftszeit auf der Hinfahrt und keine Abfahrtszeit auf der Rückfahrt
        strecken.get(0).getBahnhof1().setHinAnkunft(-1);
        strecken.get(0).getBahnhof1().setRueckAbfahrt(-1);

        // Der letzte Bahnhof hat keine Abfahrtszeit auf der Hinfahrt und keine Ankunftszeit auf der Rückfahrt
        strecken.get(strecken.size()-1).getBahnhof1().setRueckAnkunft(0);
        strecken.get(strecken.size()-1).getBahnhof1().setHinAbfahrt(0);

        // --- HINFAHRT (Identisch zur Einfachen Fahrt) ---
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

        // --- RÜCKFAHRT (Mit Verschiebung bei Kollision) ---
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

                // Berchnung der Rückfahrtsabfahrtzeit und der Hinfahrtsabfahrtzeit (Ankunftszeit + Einstiegszeit)
                int zielZeit = (startBhfRueck.getHinAnkunft() + Strecke.EINSTIEGSZEIT) % 60;
                int aktuelleZeit = startBhfRueck.getRueckAbfahrt() % 60;

                // Verschiebung ermitteln
                int verschiebung = (zielZeit - aktuelleZeit) % 60;
                if (verschiebung < 0) {
                    verschiebung += 60; // Verhindert negative Zeiten beim Stundenübergang
                }

                // Abfahrt verschieben
                startBhfRueck.setRueckAbfahrt(startBhfRueck.getRueckAbfahrt() + verschiebung);

                // Folgezeiten für diese Strecke neu berechnen
                ankunftRueck = startBhfRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
                zielBhfRueck.setRueckAnkunft(ankunftRueck);
                abfahrtRueck = ankunftRueck + Strecke.EINSTIEGSZEIT;
                zielBhfRueck.setRueckAbfahrt(abfahrtRueck);

                // Kollision wurde behoben, Flag muss zurück auf false
                aktuelleStrecke.setKollision(false);
            }


            // 3. Prüfung auf Einhaltung der Sicherheitswartezeit
            // Auch ohne Streckenkollision muss die Sicherheitswartezeit im Bahnhof gelten
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

            // 4. Zeiten in den Startbahnhof der nächsten Iteration schreiben
            if (i > 0) {
                Bahnhof vorherigerZielBhf = fahrplan.get(i - 1).getBahnhof2();
                vorherigerZielBhf.setRueckAbfahrt(zielBhfRueck.getRueckAbfahrt());
                vorherigerZielBhf.setRueckAnkunft(zielBhfRueck.getRueckAnkunft());
            }
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
        return "Einseitiges Warten";
    }
}