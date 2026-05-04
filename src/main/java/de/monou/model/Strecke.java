package de.monou.model;

public class Strecke {
    private final Bahnhof bahnhof1;
    private final Bahnhof bahnhof2;
    private final int dauer;
    private boolean kollision;

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

    public boolean pruefeKollision() {
        // Dummy-Implementierung, hier könnte die Logik zur Kollisionsprüfung implementiert werden
        return false;
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
