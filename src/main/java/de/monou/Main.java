package de.monou;

import de.monou.io.InputHandler;
import de.monou.io.InputHandlerException;
import de.monou.io.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    final static InputHandler inputHandler = new InputHandler();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static OutputHandler outputHandler = new OutputHandler();

    static void main(String[] args) {
        String[] input = new String[0];
        String filename = args[0];
        List<String> errors = new ArrayList<>();
        try{
            input = inputHandler.handleInput(filename);
        }catch (InputHandlerException e){
            logger.error("Error reading file: " + e.getMessage());
            errors.add("Error reading file: " + e.getMessage());
        }

        String[] output = new String[]{"Dummy output 1", "Dummy output 2"};
        outputHandler.createOutput(output, filename, errors.toArray(String[]::new));




    }
}
