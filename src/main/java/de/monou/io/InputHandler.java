package de.monou.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InputHandler {


    public String[] handleInput(String filepath){
        List<String> input;

        try{
            input = Files.readAllLines(Path.of(filepath))
                    .stream()
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("**"))
                    .toList();

        }catch(IOException e){
            throw new InputHandlerException("Could not read file: " + e.getMessage());
        }

        if(validate(input)){
            return input.toArray(String[]::new);
        }else{
            throw new InputHandlerException("Input validation failed");
        }
    }

    private boolean validate(List<String> input){
        //dummy
        return true;
    }
}
