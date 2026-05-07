package eisenbahngesellschaft;


import eisenbahngesellschaft.io.*;
import eisenbahngesellschaft.model.Strecke;
import eisenbahngesellschaft.strategie.BeidseitigesWarten;
import eisenbahngesellschaft.strategie.EinfacheFahrt;
import eisenbahngesellschaft.strategie.EinseitigesWarten;
import eisenbahngesellschaft.strategie.FahrplanStrategie;


import java.util.ArrayList;
import java.util.List;


/**
 * Einstiegspunkt der Anwendung.
 *
 * <p>Initialisiert Ein-/Ausgabe, registriert die Fahrplanstrategien und startet
 * die Fahrplanermittlung auf Basis der Eingabedatei.</p>
 */
public class Main {

    /**
     * Startet die Fahrplanermittlung über Kommandozeilenargumente.
     *
     * <p>Erwartete Argumente:</p>
     * <ul>
     *   <li>{@code args[0]}: Pfad zur Eingabedatei </li>
     *   <li>{@code args[1]}: Sicherheitswartezeit in Minuten (optional) {@code args[2]}</li>
     *   <li>{@code args[2]}: Einstiegszeit in Minuten (optional, nur zusammen mit {@code args[1]}).</li>
     * </ul>
     *
     * <p>Ungültige optionale Werte werden als Fehler gesammelt und am Ende in der Konsole ausgegeben.</p>
     *
     * @param args Kommandozeilenargumente zur Steuerung der Fahrplanberechnung.
     */
    public static void main(String[] args) {
        String inputPath = args[0];
        List<String> errors = new ArrayList<>();
        if (args.length == 3) {
            try{
                int sicherheitsabstand = Integer.parseInt(args[1]);
                int einstiegszeit = Integer.parseInt(args[2]);

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
                System.out.println("[FEHLER]: " + error);
            }
        }

    }
}
