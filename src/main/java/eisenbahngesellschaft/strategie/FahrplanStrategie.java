package eisenbahngesellschaft.strategie;

import eisenbahngesellschaft.model.Strecke;

import java.util.List;

/**
 * Allgemeines Interface für Fahrplan-Strategien.
 *
 * Implementierungen dieser Schnittstelle berechnen für eine gegebene
 * Folge von Strecken einen möglichen Fahrplan (mit Ankunfts- und
 * Abfahrtszeiten sowie markierten Kollisionen). Zusätzlich liefert jede
 * Strategie einen lesbaren Namen zurück, der z. B. in der Ausgabe verwendet
 * werden kann.
 */
public interface FahrplanStrategie {

    /**
     * Berechnet einen Fahrplan für die übergebenen Strecken.
     *
     * Die Implementierung ist frei, sollte jedoch deterministisch für die
     * gleiche Eingabe arbeiten und alle relevanten Zeitfelder in den
     * übergebenen {@link Strecke}-Objekten setzen (oder in einer Kopie
     * zurückgeben), damit nachfolgende Verarbeitungsschritte die Werte
     * verwenden können.
     *
     * @param strecken geordnete Liste der Strecken (Hinfahrt-Reihenfolge)
     * @return Liste der Strecken mit berechneten Zeitwerten bzw. eine leere
     * Liste, wenn keine gültige Lösung gefunden wurde
     */
    List<Strecke> ermittleFahrplan(List<Strecke> strecken);

    /**
     * Liefert den Anzeigenamen der Strategie.
     *
     * @return lesbarer Name der Strategie
     */
    String getName();
}
