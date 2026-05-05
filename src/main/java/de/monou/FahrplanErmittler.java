package de.monou;

import de.monou.io.ConcreteInputHandler;
import de.monou.io.ConcreteOutputHandler;
import de.monou.io.InputHandlerInterface;
import de.monou.io.OutputHandlerInterface;
import de.monou.model.Strecke;
import de.monou.strategie.FahrplanStrategie;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Koordiniert die Fahrplanermittlung fuer alle registrierten Strategien.
 *
 * <p>Die Klasse liest Eingabedaten ueber einen {@link InputHandlerInterface},
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
     * Ermittelt fuer alle registrierten Strategien einen Fahrplan und schreibt die Ausgabe.
     *
     * Es wird eine Eingabedatei mit der Endung {@code .txt} gelesen. Fuer jede
     * hinterlegte Strategie wird ein Fahrplan berechnet und gesammelt an den
     * Ausgabehandler uebergeben.
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
     * Gibt den aktuell gesetzten Eingabehandler zurueck.
     *
     * @return verwendeter Eingabehandler
     */
    public InputHandlerInterface getInputHandler() {
        return inputHandler;
    }

    /**
     * Setzt den Eingabehandler fuer kuenftige Fahrplanermittlungen.
     *
     * @param inputHandler neuer Eingabehandler
     */
    public void setInputHandler(InputHandlerInterface inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * Gibt den aktuell gesetzten Ausgabehandler zurueck.
     *
     * @return verwendeter Ausgabehandler
     */
    public OutputHandlerInterface getOutputHandler() {
        return outputHandler;
    }

    /**
     * Setzt den Ausgabehandler fuer kuenftige Fahrplanermittlungen.
     *
     * @param outputHandler neuer Ausgabehandler
     */
    public void setOutputHandler(OutputHandlerInterface outputHandler) {
        this.outputHandler = outputHandler;
    }

    //ToDo: Änderungen vs. Konzept markieren
    /**
     * Fuegt eine Fahrplanstrategie zur Ausfuehrungsliste hinzu.
     *
     * @param strategie hinzuzufuegende Strategie
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
