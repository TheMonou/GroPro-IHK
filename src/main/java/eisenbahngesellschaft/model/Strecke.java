package eisenbahngesellschaft.model;

/**
 * Repräsentiert eine Strecke zwischen zwei Bahnhöfen mit einer Fahrtdauer in Minuten
 * und einem boolean {@code kollision} als möglichem Kollisionsindikator.
 */
public class Strecke {
    private final Bahnhof bahnhof1;
    private final Bahnhof bahnhof2;
    private final int dauer;
    private boolean kollision;

    // "ohne Mühe angepasst werden können" -> public static macht es global änderbar.
    /**
     * Zeit (in Minuten), die beim Einsteigen verwendet werden kann (konfigurierbar).
     */
    public static int EINSTIEGSZEIT = 1;
    /**
     * Sicherheitswartezeit (in Minuten), die bei Kollisionsprüfungen berücksichtigt
     * werden könnte (konfigurierbar).
     */
    public static int SICHERHEITSWARTEZEIT = 1;

    /**
     * Erzeugt eine Strecke zwischen den beiden angegebenen Bahnhöfen mit der
     * gegebenen Fahrtdauer.
     *
     * @param bahnhof1 erster Bahnhof (Hinrichtung)
     * @param bahnhof2 zweiter Bahnhof (Rückrichtung)
     * @param dauer Fahrtdauer in Minuten
     */
    public Strecke(Bahnhof bahnhof1, Bahnhof bahnhof2, int dauer) {
        this.bahnhof1 = bahnhof1;
        this.bahnhof2 = bahnhof2;
        this.dauer = dauer;
        this.kollision = false;
    }

    /**
     * Liefert den ersten Bahnhof (Hinrichtung).
     *
     * @return Bahnhof am Anfang der Strecke
     */
    public Bahnhof getBahnhof1() {
        return bahnhof1;
    }

    /**
     * Liefert den zweiten Bahnhof (Rückrichtung).
     *
     * @return Bahnhof am Ende der Strecke
     */
    public Bahnhof getBahnhof2() {
        return bahnhof2;
    }

    /**
     * Liefert die Fahrtdauer dieser Strecke in Minuten.
     *
     * @return Dauer in Minuten
     */
    public int getDauer() {
        return dauer;
    }

    /**
     * Gibt an, ob für diese Strecke aktuell eine Kollision festgestellt wurde.
     *
     * @return {@code true}, falls eine Kollision vorliegt, sonst {@code false}
     */
    public boolean isKollision() {
        return kollision;
    }

    /**
     * Setzt den Kollision-Status für diese Strecke.
     *
     * @param kollision {@code true}, falls eine Kollision vorhanden ist
     */
    public void setKollision(boolean kollision) {
        this.kollision = kollision;
    }

    /**
     * Prüft, ob Züge der Hin- und Rückfahrt diese Strecke in sich überschneidenden
     * Zeiträumen belegen.
     *
     * <p> Da die Fahrten im 60-Minuten-Takt wiederkehren, wird geprüft, ob sich
     * die Intervalle in der gleichen Stunde, in der vorherigen Stunde oder in der
     * nächsten Stunde überschneiden. Konkret werden drei Überlappungsfälle ermittelt:
     * - Rückfahrt eine Stunde vorher
     * - Rückfahrt in der gleichen Stunde
     * - Rückfahrt eine Stunde später
     * </p>
     * Die Methode setzt das Feld {@code kollision} auf {@code true}, falls eine
     * Überschneidung gefunden wird, und gibt den Wert zurück.
     *
     * @return {@code true} wenn eine Kollision festgestellt wurde, sonst {@code false}
     */
    public boolean pruefeKollision() {
        // 1. Reduzieren der absoluten Startzeit auf die aktuelle Stunde (Modulo 60)
        int hinAb = bahnhof1.getHinAbfahrt() % 60;
        // WICHTIG: Die Ankunft wird addiert, damit das Intervall nicht zerrissen wird (z.B. [58, 62])
        int hinAn = hinAb + dauer;

        int rAb = bahnhof2.getRueckAbfahrt() % 60;
        int rAn = rAb + dauer;

        // 2. Prüfen auf Überschneidung: (A <= D) && (C <= B)
        // Das "<=" erzwingt die Sicherheitsminute, da exakt gleiche Minuten als Kollision gelten!

        // Prüft die Rückfahrt, die eine Stunde vorher abgefahren ist
        boolean overlapVorher = (hinAb <= (rAn - 60)) && ((rAb - 60) <= hinAn);

        // Prüft die Rückfahrt in der gleichen Stunde
        boolean overlapGleich = (hinAb <= rAn) && (rAb <= hinAn);

        // Prüft die Rückfahrt, die eine Stunde später abfährt
        boolean overlapSpaeter = (hinAb <= (rAn + 60)) && ((rAb + 60) <= hinAn);

        // Wenn in irgendeinem dieser 60-Minuten-Takte eine Überschneidung vorliegt -> Kollision!
        this.kollision = overlapVorher || overlapGleich || overlapSpaeter;

        return this.kollision;
    }

    /**
     * Liefert eine lesbare Darstellung der Strecke ("A -> B (12 min)").
     *
     * @return String-Repräsentation der Strecke
     */
    public String toString(){
        return bahnhof1.getName() + " -> " + bahnhof2.getName() + " (" + dauer + " min)";
    }

    /**
     * Erstellt eine Kopie dieser Strecke. Die enthaltenen Bahnhof-Objekte
     * werden ebenfalls kopiert (tiefe Kopie der Strecke mit Kopien der Bahnhöfe).
     *
     * @return neue Instanz von {@code Strecke} mit identischen Feldwerten
     */
    public Strecke copy(){
        Strecke copy = new Strecke(this.bahnhof1.copy(), this.bahnhof2.copy(), this.dauer);
        copy.setKollision(this.kollision);
        return copy;
    }
}
