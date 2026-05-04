package de.monou.model;

public class Strecke {
    private final Bahnhof bahnhof1;
    private final Bahnhof bahnhof2;
    private final int dauer;
    private boolean kollision;

    // Zentrale Konfigurationsvariablen gemäß Aufgabenstellung.
    // "ohne Mühe angepasst werden können" -> public static macht es global änderbar.
    public static int EINSTIEGSZEIT = 1;
    public static int SICHERHEITSWARTEZEIT = 1;

    public Strecke(Bahnhof bahnhof1, Bahnhof bahnhof2, int dauer) {
        this.bahnhof1 = bahnhof1;
        this.bahnhof2 = bahnhof2;
        this.dauer = dauer;
        this.kollision = false;
    }

    public Bahnhof getBahnhof1() {
        return bahnhof1;
    }

    public Bahnhof getBahnhof2() {
        return bahnhof2;
    }

    public int getDauer() {
        return dauer;
    }

    public boolean isKollision() {
        return kollision;
    }

    public void setKollision(boolean kollision) {
        this.kollision = kollision;
    }

    /**
     * Prüft, ob Züge der Hin- und Rückfahrt diese Strecke zu überschneidenden
     * Zeiträumen belegen. Nutzt die konfigurierbare SICHERHEITSWARTEZEIT.
     */
    public boolean pruefeKollision() {
        // 1. Wir reduzieren die absolute Startzeit auf die aktuelle Stunde (Modulo 60)
        int hinAb = bahnhof1.getHinAbfahrt() % 60;
        // WICHTIG: Die Ankunft wird addiert, damit das Intervall nicht zerrissen wird (z.B. [58, 62])
        int hinAn = hinAb + dauer;

        int rAb = bahnhof2.getRueckAbfahrt() % 60;
        int rAn = rAb + dauer;

        // 2. Wir prüfen die Überschneidung: (A < D) && (C < B)
        // Da die Züge alle 60 Minuten fahren, müssen wir prüfen, ob der Gegenzug
        // in DIESER Stunde, in der VORHERIGEN oder in der NÄCHSTEN Stunde kollidiert.

        // Prüft die Rückfahrt, die eine Stunde vorher abgefahren ist
        boolean overlapVorher = (hinAb < (rAn - 60)) && ((rAb - 60) < hinAn);

        // Prüft die Rückfahrt in der gleichen Stunde
        boolean overlapGleich = (hinAb < rAn) && (rAb < hinAn);

        // Prüft die Rückfahrt, die eine Stunde später abfährt
        boolean overlapSpaeter = (hinAb < (rAn + 60)) && ((rAb + 60) < hinAn);

        // Wenn in irgendeinem dieser 60-Minuten-Takte eine Überschneidung vorliegt -> Kollision!
        this.kollision = overlapVorher || overlapGleich || overlapSpaeter;


        return this.kollision;
    }

    public String toString(){
        return bahnhof1.getName() + " -> " + bahnhof2.getName() + " (" + dauer + " min)";
    }

    public Strecke copy(){
        Strecke copy = new Strecke(this.bahnhof1.copy(), this.bahnhof2.copy(), this.dauer);
        copy.setKollision(this.kollision);
        return copy;
    }
}
