package de.monou.io;

/**
 * Laufzeit-Ausnahme für Fehler beim Erzeugen der Ausgabedatei.
 *
 * Wird von {@link ConcreteOutputHandler} oder anderen Implementierungen
 * von {@link OutputHandlerInterface} geworfen, wenn Schreiben oder
 * Verzeichnisoperationen fehlschlagen.
 */
public class OutputHandlerException extends RuntimeException {
    public OutputHandlerException(String message) {
        super(message);
    }
}
