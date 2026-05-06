package eisenbahngesellschaft.io;

/**
 * Laufzeit-Ausnahme, die beim Einlesen oder Parsen der Eingabedatei geworfen wird.
 *
 * Wird von Implementierungen von {@link InputHandlerInterface} verwendet, um
 * Fehlerbedingungen (I/O-Probleme, ungültiges Format, fehlende Werte) an den
 * Aufrufer weiterzugeben.
 */
public class InputHandlerException extends RuntimeException {
    public InputHandlerException(String message) {
        super(message);
    }
}
