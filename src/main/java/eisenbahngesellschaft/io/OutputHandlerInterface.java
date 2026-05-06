package eisenbahngesellschaft.io;

import eisenbahngesellschaft.model.Strecke;

import java.util.List;

/**
 * Schnittstelle für die Erzeugung von Ausgabe-Dateien aus berechneten
 * Fahrplänen.
 *
 * Implementierungen sind verantwortlich für das Formatieren der
 * Ergebnisse und das Persistieren (z. B. in ein Textfile unter dem
 * Ordner {@code output/}).
 */
public interface OutputHandlerInterface {

    /**
     * Schreibt die berechneten Fahrpläne in eine Ausgabedatei.
     *
     * @param strecken Liste von Fahrplänen (jede Liste ist das Ergebnis einer Strategie)
     * @param namen   zugehörige Namen der Strategien, gleiche Reihenfolge wie {@code strecken}
     * @param filename Ursprünglicher Eingabedateiname (zur Bildung des Output-Namens)
     */
    void createOutput(List<List<Strecke>> strecken, List<String> namen, String filename);

    /**
     * Berechnet die Summe der Wartezeiten für Hinfahrt und Rückfahrt eines Fahrplans.
     *
     * @param strecken Fahrplan, für den die Wartezeiten berechnet werden sollen
     * @return int-Array der Länge 2: [0]=Wartezeit Hinfahrt, [1]=Wartezeit Rückfahrt
     */
    int[] berechneWartezeiten(List<Strecke> strecken);

    /**
     * Berechnet den Straf-Score (Quadratsumme der Wartezeiten) aus dem Wartezeiten-Array.
     *
     * @param wartezeiten Array wie von {@link #berechneWartezeiten(List)}
     * @return berechneter Score
     */
    int berechneScore(int[] wartezeiten);
}
