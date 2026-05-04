package de.monou;

import de.monou.io.ConcreteInputHandler;
import de.monou.io.ConcreteOutputHandler;
import de.monou.io.InputHandlerInterface;
import de.monou.io.OutputHandlerInterface;
import de.monou.model.Strecke;
import de.monou.strategie.FahrplanStrategie;

import java.util.ArrayList;
import java.util.List;

public class FahrplanErmittler {
    private List<FahrplanStrategie> strategien;
    private InputHandlerInterface inputHandler;
    private OutputHandlerInterface outputHandler;

    public FahrplanErmittler(InputHandlerInterface inputHandler, OutputHandlerInterface outputHandler) {
        this.strategien = new ArrayList<>();
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    public void ermittleFahrplan(String path){
        List<Strecke> strecken = inputHandler.handleInput(path);
        List<List<Strecke>> fahrplaene = new ArrayList<>();

        for (FahrplanStrategie strategie : strategien) {
            List<Strecke> fahrplan = strategie.ermittleFahrplan(strecken);
            fahrplaene.add(fahrplan);
        }

        for (Strecke strecke: strecken) {
            System.out.println(strecke.toString());
        }


    }

    public InputHandlerInterface getInputHandler() {
        return inputHandler;
    }

    public void setInputHandler(InputHandlerInterface inputHandler) {
        this.inputHandler = inputHandler;
    }

    public OutputHandlerInterface getOutputHandler() {
        return outputHandler;
    }

    public void setOutputHandler(OutputHandlerInterface outputHandler) {
        this.outputHandler = outputHandler;
    }

    //Änderungen vs. Konzept
    public void addStrategie(FahrplanStrategie strategie){
        this.strategien.add(strategie);
    }

    public void clearStrategien(){
        this.strategien.clear();
    }


}
