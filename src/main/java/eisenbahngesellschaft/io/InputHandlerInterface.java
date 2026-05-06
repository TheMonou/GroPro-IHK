package eisenbahngesellschaft.io;

import eisenbahngesellschaft.model.Strecke;

import java.util.List;

/**
 * Schnittstelle für das Einlesen von Eingabedateien.
 *
 * Implementierungen parsen eine Eingabedatei (z. B. aus dem Ordner
 * {@code input/}) und erzeugen daraus die interne Repräsentation der
 * Strecken als Liste von {@link Strecke}-Objekten. Fehler beim Einlesen
 * sollten durch {@link InputHandlerException} signalisiert werden.
 */
public interface InputHandlerInterface {

    /**
     * Liest die Eingabedatei am angegebenen Pfad und wandelt sie in eine
     * Liste von {@link Strecke}-Instanzen um.
     *
     * @param path Pfad zur Eingabedatei
     * @return Liste der Strecken (Hinfahrt-Reihenfolge)
     * @throws InputHandlerException bei Lesefehlern oder ungültigem Format
     */
    List<Strecke> handleInput(String path);


}
