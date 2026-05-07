package eisenbahngesellschaft.strategie;

import eisenbahngesellschaft.model.Bahnhof;
import eisenbahngesellschaft.model.Strecke;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategie für die Fahrplanermittlung mit beidseitigem Warten.
 *
 * <p>
 * Die Strategie prüft stufenweise mehrere Varianten: zuerst eine einfache
 * Fahrt ohne Zusatzwartezeiten, danach eine reine Startverschiebung der
 * Rückfahrt und schließlich eine Greedy-Relaxation mit Wartezeiten auf
 * Hin- und Rückfahrt. Ziel ist ein kollisionsfreier Fahrplan mit möglichst
 * niedrigem Score.
 * </p>
 */
public class BeidseitigesWarten implements FahrplanStrategie {

    @Override
    public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
        if (strecken == null || strecken.isEmpty()) return new ArrayList<>();

        int startzeitHinfahrt = strecken.get(0).getBahnhof1().getHinAbfahrt();

        // ---  Einfache Fahrt  ---
        List<Strecke> einfacheFahrtPlan = kopiereFahrplan(strecken);
        berechneZeiten(einfacheFahrtPlan, -1, startzeitHinfahrt, new int[strecken.size()], new int[strecken.size()]);
        if (!enthaeltKollisionen(einfacheFahrtPlan)) {
            return einfacheFahrtPlan;  // Keine Kollisionen.
        }

        // --- Rückfahrt verschieben (Startverzögerung, aber keine Wartezeiten unterwegs) ---
        for (int verschiebung = 0; verschiebung < 60; verschiebung++) {
            List<Strecke> verschobenerPlan = kopiereFahrplan(strecken);
            berechneZeiten(verschobenerPlan, verschiebung, startzeitHinfahrt, new int[strecken.size()], new int[strecken.size()]);
            if (!enthaeltKollisionen(verschobenerPlan)) {
                return verschobenerPlan; // Kollisionen konnten allein durch den verschobenen Start gelöst werden.
            }
        }

        // Baseline:(Einseitiges Warten als Mindestlösung)
        FahrplanStrategie einseitig = new EinseitigesWarten();
        List<Strecke> baselinePlan = einseitig.ermittleFahrplan(kopiereFahrplan(strecken));

        int niedrigsteStrafpunkte = berechneScoreAusFahrplan(baselinePlan);
        List<Strecke> besterFahrplan = baselinePlan; // Fallback

        // ---  Greedy Relaxation (Echte Wartezeiten an den Bahnhöfen einfügen) ---
        for (int verschiebung = 0; verschiebung < 60; verschiebung++) {
            int[] wartezeitenHin = new int[strecken.size()];
            int[] wartezeitenRueck = new int[strecken.size()];

            List<Strecke> aktuellerFahrplan = kopiereFahrplan(strecken);
            boolean fahrplanStabil = false;
            int durchlaeufe = 0;

            // Die Relaxations-Schleife
            while (!fahrplanStabil && durchlaeufe < 100000) {
                berechneZeiten(aktuellerFahrplan, verschiebung, startzeitHinfahrt, wartezeitenHin, wartezeitenRueck);

                int kollisionsIndex = -1;
                for (int i = 0; i < aktuellerFahrplan.size(); i++) {
                    if (aktuellerFahrplan.get(i).isKollision()) {
                        kollisionsIndex = i;
                        break; // Chronologisch erste Kollision finden
                    }
                }

                if (kollisionsIndex == -1) {
                    fahrplanStabil = true; // Fixpunkt erreicht!
                } else {
                    Strecke streckeMitKollision = aktuellerFahrplan.get(kollisionsIndex);

                    // Option A: Hinfahrt wartet
                    int abfahrtHin = streckeMitKollision.getBahnhof1().getHinAbfahrt();
                    int ankunftRueck = streckeMitKollision.getBahnhof1().getRueckAnkunft();
                    int zusaetzlicheWartezeitHin = (ankunftRueck + Strecke.EINSTIEGSZEIT - abfahrtHin) % 60;
                    if (zusaetzlicheWartezeitHin < 0) zusaetzlicheWartezeitHin += 60;

                    // Option B: Rückfahrt wartet
                    int abfahrtRueck = streckeMitKollision.getBahnhof2().getRueckAbfahrt();
                    int ankunftHin = streckeMitKollision.getBahnhof2().getHinAnkunft();
                    int zusaetzlicheWartezeitRueck = (ankunftHin + Strecke.EINSTIEGSZEIT - abfahrtRueck) % 60;
                    if (zusaetzlicheWartezeitRueck < 0) zusaetzlicheWartezeitRueck += 60;

                    // Globale Summen berechnen, um Balance zu erzwingen
                    int gesamteWartezeitHin = 0;
                    int gesamteWartezeitRueck = 0;
                    for (int wartezeit : wartezeitenHin) gesamteWartezeitHin += wartezeit;
                    for (int wartezeit : wartezeitenRueck) gesamteWartezeitRueck += wartezeit;

                    // Lokale Kosten (Strafpunkt-Erhöhung für diese spezifische Station)
                    int strafpunkteLokalHin = (int) Math.pow(wartezeitenHin[kollisionsIndex] + zusaetzlicheWartezeitHin, 2) - (int) Math.pow(wartezeitenHin[kollisionsIndex], 2);
                    int strafpunkteLokalRueck = (int) Math.pow(wartezeitenRueck[kollisionsIndex] + zusaetzlicheWartezeitRueck, 2) - (int) Math.pow(wartezeitenRueck[kollisionsIndex], 2);

                    // Ungleichgewichtsstrafe: Quadrierte Differenz der globalen Summen
                    int strafpunkteUngleichgewichtHin = (int) Math.pow(Math.abs((gesamteWartezeitHin + zusaetzlicheWartezeitHin) - gesamteWartezeitRueck), 2);
                    int strafpunkteUngleichgewichtRueck = (int) Math.pow(Math.abs(gesamteWartezeitHin - (gesamteWartezeitRueck + zusaetzlicheWartezeitRueck)), 2);

                    // Gesamtkosten
                    long strafpunkteGesamtHin = (kollisionsIndex == 0) ? Long.MAX_VALUE : (long) strafpunkteLokalHin + strafpunkteUngleichgewichtHin;
                    long strafpunkteGesamtRueck = (kollisionsIndex == aktuellerFahrplan.size() - 1) ? Long.MAX_VALUE : (long) strafpunkteLokalRueck + strafpunkteUngleichgewichtRueck;

                    // Greedy Entscheidung: Wähle den günstigeren Weg
                    if (strafpunkteGesamtHin <= strafpunkteGesamtRueck && strafpunkteGesamtHin != Long.MAX_VALUE) {
                        wartezeitenHin[kollisionsIndex] += zusaetzlicheWartezeitHin;
                    } else if (strafpunkteGesamtRueck != Long.MAX_VALUE) {
                        wartezeitenRueck[kollisionsIndex] += zusaetzlicheWartezeitRueck;
                    } else {
                        break; // Notausstieg bei invaliden Strecken
                    }
                }
                durchlaeufe++;
            }

            // Wenn der Fahrplan stabil ist, echte Bewertung durchführen
            if (fahrplanStabil) {
                int aktuelleStrafpunkte = 0;
                for (int wartezeit : wartezeitenHin) aktuelleStrafpunkte += (int) Math.pow(wartezeit, 2);
                for (int wartezeit : wartezeitenRueck) aktuelleStrafpunkte += (int) Math.pow(wartezeit, 2);

                if (aktuelleStrafpunkte < niedrigsteStrafpunkte) {
                    niedrigsteStrafpunkte = aktuelleStrafpunkte;
                    besterFahrplan = kopiereFahrplan(aktuellerFahrplan);
                }
            }
        }

        return besterFahrplan;
    }

    private boolean enthaeltKollisionen(List<Strecke> fahrplan) {
        for (Strecke strecke : fahrplan) {
            if (strecke.isKollision()) return true;
        }
        return false;
    }

    private void berechneZeiten(List<Strecke> fahrplan, int verschiebung, int startzeitHinfahrt, int[] wartezeitenHin, int[] wartezeitenRueck) {

        fahrplan.get(0).getBahnhof1().setHinAnkunft(-1);
        fahrplan.get(0).getBahnhof1().setRueckAbfahrt(-1);
        fahrplan.get(fahrplan.size() - 1).getBahnhof1().setRueckAnkunft(-1);

        fahrplan.get(0).getBahnhof1().setHinAbfahrt(startzeitHinfahrt);

        // --- HINFAHRT ---
        for (int i = 0; i < fahrplan.size(); i++) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            Bahnhof startBahnhof = aktuelleStrecke.getBahnhof1();
            Bahnhof zielBahnhof = aktuelleStrecke.getBahnhof2();

            if (i > 0) {
                startBahnhof.setHinAbfahrt(startBahnhof.getHinAbfahrt() + wartezeitenHin[i]);
            }

            int ankunftszeit = startBahnhof.getHinAbfahrt() + aktuelleStrecke.getDauer();
            zielBahnhof.setHinAnkunft(ankunftszeit);

            int abfahrtszeit = ankunftszeit + Strecke.EINSTIEGSZEIT;
            zielBahnhof.setHinAbfahrt(abfahrtszeit);

            if (i < fahrplan.size() - 1) {
                Bahnhof naechsterStartBahnhof = fahrplan.get(i + 1).getBahnhof1();
                naechsterStartBahnhof.setHinAbfahrt(abfahrtszeit);
                naechsterStartBahnhof.setHinAnkunft(ankunftszeit);
            }
        }

        // --- RÜCKFAHRT ---
        Bahnhof letzterBahnhof = fahrplan.get(fahrplan.size() - 1).getBahnhof2();

        int fruehesterStartRueckfahrt = letzterBahnhof.getHinAnkunft() + Strecke.EINSTIEGSZEIT;
        int absoluterStartRueckfahrt = fruehesterStartRueckfahrt;

        if (verschiebung != -1) {
            while (absoluterStartRueckfahrt % 60 != verschiebung) {
                absoluterStartRueckfahrt++;
            }
        }

        letzterBahnhof.setRueckAbfahrt(absoluterStartRueckfahrt);

        for (int i = fahrplan.size() - 1; i >= 0; i--) {
            Strecke aktuelleStrecke = fahrplan.get(i);
            Bahnhof startBahnhofRueck = aktuelleStrecke.getBahnhof2();
            Bahnhof zielBahnhofRueck = aktuelleStrecke.getBahnhof1();

            if (i < fahrplan.size() - 1) {
                startBahnhofRueck.setRueckAbfahrt(startBahnhofRueck.getRueckAbfahrt() + wartezeitenRueck[i]);
            }

            int ankunftszeitRueck = startBahnhofRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
            zielBahnhofRueck.setRueckAnkunft(ankunftszeitRueck);

            int abfahrtszeitRueck = ankunftszeitRueck + Strecke.EINSTIEGSZEIT;
            zielBahnhofRueck.setRueckAbfahrt(abfahrtszeitRueck);

            if (i > 0) {
                Bahnhof vorherigerZielBahnhof = fahrplan.get(i - 1).getBahnhof2();
                vorherigerZielBahnhof.setRueckAbfahrt(abfahrtszeitRueck);
                vorherigerZielBahnhof.setRueckAnkunft(ankunftszeitRueck);
            }

            aktuelleStrecke.pruefeKollision();
        }
    }

    /**
     * NEU: Hilfsmethode, um die Strafpunkte eines fertigen Fahrplans (die Baseline)
     * aus den Ankunfts- und Abfahrtszeiten zu rekonstruieren.
     */
    private int berechneScoreAusFahrplan(List<Strecke> fahrplan) {
        int score = 0;

        // Wartezeiten der Hinfahrt addieren (außer erster Bahnhof)
        for (int i = 1; i < fahrplan.size(); i++) {
            Bahnhof bhf = fahrplan.get(i).getBahnhof1();
            int ankunftVorher = fahrplan.get(i - 1).getBahnhof2().getHinAnkunft();
            int fruehesteAbfahrt = ankunftVorher + Strecke.EINSTIEGSZEIT;

            int wartezeit = (bhf.getHinAbfahrt() - fruehesteAbfahrt) % 60;
            if (wartezeit < 0) wartezeit += 60;

            score += (wartezeit * wartezeit);
        }

        // Wartezeiten der Rückfahrt addieren (außer letzter Bahnhof der Gesamtstrecke)
        for (int i = fahrplan.size() - 2; i >= 0; i--) {
            Bahnhof bhf = fahrplan.get(i).getBahnhof2();
            int ankunftVorher = fahrplan.get(i + 1).getBahnhof1().getRueckAnkunft();
            int fruehesteAbfahrt = ankunftVorher + Strecke.EINSTIEGSZEIT;

            int wartezeit = (bhf.getRueckAbfahrt() - fruehesteAbfahrt) % 60;
            if (wartezeit < 0) wartezeit += 60;

            score += (wartezeit * wartezeit);
        }

        return score;
    }

    private List<Strecke> kopiereFahrplan(List<Strecke> original) {
        List<Strecke> kopie = new ArrayList<>();
        for (Strecke strecke : original) {
            kopie.add(strecke.copy());
        }
        return kopie;
    }

    @Override
    public String getName() {
        return "Beidseitiges Warten";
    }
}