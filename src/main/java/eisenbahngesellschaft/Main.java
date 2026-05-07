package eisenbahngesellschaft;


import eisenbahngesellschaft.io.*;
import eisenbahngesellschaft.model.Strecke;
import eisenbahngesellschaft.strategie.BeidseitigesWarten;
import eisenbahngesellschaft.strategie.EinfacheFahrt;
import eisenbahngesellschaft.strategie.EinseitigesWarten;
import eisenbahngesellschaft.strategie.FahrplanStrategie;


import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        String inputPath = args[0];
        List<String> errors = new ArrayList<>();
        if (args.length == 3) {
            try{
                int einstiegszeit = Integer.parseInt(args[1]);
                int sicherheitsabstand = Integer.parseInt(args[2]);

                Strecke.EINSTIEGSZEIT = einstiegszeit;
                Strecke.SICHERHEITSWARTEZEIT = sicherheitsabstand;
            }catch(Exception e){
                errors.add("Einstiegszeit und Sicherheitszeit konnten nicht gesetzt werden: "
                        + e.getMessage()
                );
            }
        }



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
            errors.add(e.getMessage());
        } catch (OutputHandlerException e){
            errors.add(e.getMessage());
        }

        //Fehlermeldungen werden in die Konsole ausgegeben
        if (errors.size() > 0) {
            System.out.println("Es sind Fehler aufgetreten:");
            for (String error : errors) {
                System.out.println(error);
            }
        }

    }
}
