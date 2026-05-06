package eisenbahngesellschaft;

import eisenbahngesellschaft.io.InputHandlerInterface;
import eisenbahngesellschaft.io.OutputHandlerInterface;
import eisenbahngesellschaft.model.Strecke;
import eisenbahngesellschaft.strategie.FahrplanStrategie;

import java.util.ArrayList;
import java.util.List;

/**
 * Koordiniert die Fahrplanermittlung für alle registrierten Strategien.
 *
 * <p>Die Klasse liest Eingabedaten über einen {@link InputHandlerInterface},
 * berechnet je Strategie einen Fahrplan und delegiert die Ausgabe an einen
 * {@link OutputHandlerInterface}.</p>
 */
public class FahrplanErmittler {
    private List<FahrplanStrategie> strategien;
    private InputHandlerInterface inputHandler;
    private OutputHandlerInterface outputHandler;

    /**
     * Erstellt einen neuen Fahrplanermittler mit konfigurierbaren Ein- und Ausgabehandlern.
     *
     * @param inputHandler Handler zum Einlesen der Eingabedaten
     * @param outputHandler Handler zum Erzeugen der Ausgabedaten
     */
    public FahrplanErmittler(InputHandlerInterface inputHandler, OutputHandlerInterface outputHandler) {
        this.strategien = new ArrayList<>();
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    /**
     * Ermittelt für alle registrierten Strategien einen Fahrplan und schreibt die Ausgabe.
     * <p>
     *  Es wird eine Eingabedatei mit der Endung {@code .txt} gelesen. Für jede
     *  hinterlegte Strategie wird ein Fahrplan berechnet und gesammelt an den
     *  Ausgabehandler übergeben.
     * </p>
     *
     * @param path Basispfad der Ein- und Ausgabedatei ohne Dateiendung
     */
    public void ermittleFahrplan(String path){
        List<Strecke> strecken = inputHandler.handleInput(path+".txt");
        List<List<Strecke>> fahrplaene = new ArrayList<>();

        for (FahrplanStrategie strategie : strategien) {
            List<Strecke> fahrplan = strategie.ermittleFahrplan(strecken);
            fahrplaene.add(fahrplan);
        }

        outputHandler.createOutput(fahrplaene, getStrategieNamen(), path);

    }

    /**
     * Liefert die Namen aller aktuell registrierten Strategien in Reihenfolge.
     *
     * @return Liste der Strategienamen
     */
    private List<String> getStrategieNamen(){
        return strategien.stream().map(FahrplanStrategie::getName).toList();
    }

    /**
     * Gibt den aktuell gesetzten Eingabehandler zurück.
     *
     * @return verwendeter Eingabehandler
     */
    public InputHandlerInterface getInputHandler() {
        return inputHandler;
    }

    /**
     * Setzt den Eingabehandler für künftige Fahrplanermittlungen.
     *
     * @param inputHandler neuer Eingabehandler
     */
    public void setInputHandler(InputHandlerInterface inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * Gibt den aktuell gesetzten Ausgabehandler zurück.
     *
     * @return verwendeter Ausgabehandler
     */
    public OutputHandlerInterface getOutputHandler() {
        return outputHandler;
    }

    /**
     * Setzt den Ausgabehandler für künftige Fahrplanermittlungen.
     *
     * @param outputHandler neuer Ausgabehandler
     */
    public void setOutputHandler(OutputHandlerInterface outputHandler) {
        this.outputHandler = outputHandler;
    }

    //ToDo: Änderungen vs. Konzept markieren
    /**
     * Fügt eine Fahrplanstrategie zur Ausführungsliste hinzu.
     *
     * @param strategie hinzuzufügende Strategie
     */
    public void addStrategie(FahrplanStrategie strategie){
        this.strategien.add(strategie);
    }

    /**
     * Entfernt alle aktuell registrierten Fahrplanstrategien.
     */
    public void clearStrategien(){
        this.strategien.clear();
    }


}
