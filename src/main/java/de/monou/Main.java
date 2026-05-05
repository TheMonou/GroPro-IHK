package de.monou;

import de.monou.io.*;
import de.monou.strategie.BeidseitigesWarten;
import de.monou.strategie.EinfacheFahrt;
import de.monou.strategie.EinseitigesWarten;
import de.monou.strategie.FahrplanStrategie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Logger logger = LoggerFactory.getLogger(Main.class);
            logger.error("Fehler beim Einlesen der Datei: " + e.getMessage());
        } catch (OutputHandlerException e){
            Logger logger = LoggerFactory.getLogger(Main.class);
            logger.error("Fehler beim Schreiben der Datei: " + e.getMessage());
        }


    }
}
