package de.monou.model;

public class Bahnhof {
    private final String name;
    private int hinAnkunft;
    private int hinAbfahrt;
    private int rueckAnkunft;
    private int rueckAbfahrt;
    public Bahnhof(String name) {
        this.name = name;
    }

    public int getHinAnkunft() {
        return hinAnkunft;
    }

    public void setHinAnkunft(int hinAnkunft) {
        this.hinAnkunft = hinAnkunft;
    }

    public int getHinAbfahrt() {
        return hinAbfahrt;
    }

    public void setHinAbfahrt(int hinAbfahrt) {
        this.hinAbfahrt = hinAbfahrt;
    }

    public int getRueckAnkunft() {
        return rueckAnkunft;
    }

    public void setRueckAnkunft(int rueckAnkunft) {
        this.rueckAnkunft = rueckAnkunft;
    }

    public int getRueckAbfahrt() {
        return rueckAbfahrt;
    }

    public void setRueckAbfahrt(int rueckAbfahrt) {
        this.rueckAbfahrt = rueckAbfahrt;
    }

    public String getName() {
        return name;
    }

    public Bahnhof copy(){
        Bahnhof copy = new Bahnhof(this.name);
        copy.setHinAnkunft(this.hinAnkunft);
        copy.setHinAbfahrt(this.hinAbfahrt);
        copy.setRueckAnkunft(this.rueckAnkunft);
        copy.setRueckAbfahrt(this.rueckAbfahrt);
        return copy;
    }
}
