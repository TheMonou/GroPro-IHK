package eisenbahngesellschaft;

import de.monou.io.*;
import eisenbahngesellschaft.io.*;
import eisenbahngesellschaft.strategie.BeidseitigesWarten;
import eisenbahngesellschaft.strategie.EinfacheFahrt;
import eisenbahngesellschaft.strategie.EinseitigesWarten;
import eisenbahngesellschaft.strategie.FahrplanStrategie;


import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        String inputPath = args[0];

        List<FahrplanStrategie> strategien = new ArrayList<>();
        strategien.add(new EinfacheFahrt());
        strategien.add(new EinseitigesWarten());
        strategien.add(new BeidseitigesWarten());

        InputHandlerInterface inputHandler = new ConcreteInputHandler();
        OutputHandlerInterface outputHandler = new ConcreteOutputHandler();

        FahrplanErmittler fahrplanErmittler = new FahrplanErmittler(inputHandler, outputHandler);
        for (FahrplanStrategie strategie : strategien) {
            fahrplanErmittler.addStrategie(strategie);
        }
        try {
            fahrplanErmittler.ermittleFahrplan(inputPath);
        } catch (InputHandlerException e) {
            // Logs
        } catch (OutputHandlerException e){
            // Error
        }


    }
}
