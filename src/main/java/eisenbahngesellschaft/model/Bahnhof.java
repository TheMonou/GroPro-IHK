package eisenbahngesellschaft.model;

/**
 * Repräsentiert einen Bahnhof mit den Ankunfts- und Abfahrtzeit jeweils für Hin- und Rückweg
 * und einem String als namen.
 *
 */
public class Bahnhof {
    private final String name;
    private int hinAnkunft;
    private int hinAbfahrt;
    private int rueckAnkunft;
    private int rueckAbfahrt;

    /**
     * Erzeugt einen Bahnhof mit dem gegebenen Namen. Die Zeitfelder sind
     * initial 0 und müssen separat gesetzt werden.
     *
     * @param name lesbarer Name des Bahnhofs
     */
    public Bahnhof(String name) {
        this.name = name;
    }

    /**
     * Liefert die Ankunftszeit des Zuges auf der Hinfahrt in Minuten.
     *
     * @return Ankunftszeit (Minuten)
     */
    public int getHinAnkunft() {
        return hinAnkunft;
    }

    /**
     * Setzt die Ankunftszeit des Zuges auf der Hinfahrt in Minuten.
     *
     * @param hinAnkunft Ankunftszeit (Minuten)
     */
    public void setHinAnkunft(int hinAnkunft) {
        this.hinAnkunft = hinAnkunft;
    }

    /**
     * Liefert die Abfahrtszeit des Zuges auf der Hinfahrt in Minuten.
     *
     * @return Abfahrtszeit (Minuten)
     */
    public int getHinAbfahrt() {
        return hinAbfahrt;
    }

    /**
     * Setzt die Abfahrtszeit des Zuges auf der Hinfahrt in Minuten.
     *
     * @param hinAbfahrt Abfahrtszeit (Minuten)
     */
    public void setHinAbfahrt(int hinAbfahrt) {
        this.hinAbfahrt = hinAbfahrt;
    }

    /**
     * Liefert die Ankunftszeit des Zuges auf der Rückfahrt in Minuten.
     *
     * @return Ankunftszeit (Minuten)
     */
    public int getRueckAnkunft() {
        return rueckAnkunft;
    }

    /**
     * Setzt die Ankunftszeit des Zuges auf der Rückfahrt in Minuten.
     *
     * @param rueckAnkunft Ankunftszeit (Minuten)
     */
    public void setRueckAnkunft(int rueckAnkunft) {
        this.rueckAnkunft = rueckAnkunft;
    }

    /**
     * Liefert die Abfahrtszeit des Zuges auf der Rückfahrt in Minuten.
     *
     * @return Abfahrtszeit (Minuten)
     */
    public int getRueckAbfahrt() {
        return rueckAbfahrt;
    }

    /**
     * Setzt die Abfahrtszeit des Zuges auf der Rückfahrt in Minuten.
     *
     * @param rueckAbfahrt Abfahrtszeit (Minuten)
     */
    public void setRueckAbfahrt(int rueckAbfahrt) {
        this.rueckAbfahrt = rueckAbfahrt;
    }

    /**
     * Liefert den Namen des Bahnhofs.
     *
     * @return Bahnhofname
     */
    public String getName() {
        return name;
    }

    /**
     * Erzeugt eine flache Kopie des Bahnhofsobjekts. Die kopierten Zeitwerte
     * werden übernommen, der Name bleibt gleich.
     *
     * @return neue Instanz von {@code Bahnhof} mit identischen Feldwerten
     */
    public Bahnhof copy(){
        Bahnhof copy = new Bahnhof(this.name);
        copy.setHinAnkunft(this.hinAnkunft);
        copy.setHinAbfahrt(this.hinAbfahrt);
        copy.setRueckAnkunft(this.rueckAnkunft);
        copy.setRueckAbfahrt(this.rueckAbfahrt);
        return copy;
    }
}
