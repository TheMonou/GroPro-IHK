package de.monou.io;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OutputHandler {

    private static String OUTPUT_PREFIX = "output/output_";

    public void createOutput(String[] output, String filename, String errors[]){
        String filenameOnly = Paths.get(filename).getFileName().toString();
        String filepath = OUTPUT_PREFIX + filenameOnly;


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write("** Vom Programm ermittelte Lösung");
            writer.newLine();

            for (String line : output) {
                writer.write(line);
                writer.newLine();
            }

            //errors
            writer.write("**errors: ");
            writer.newLine();

            for (String line : errors) {
                writer.write(line);
                writer.newLine();
            }

        }catch(IOException e){
            throw new OutputHandlerException("Could not create output file: " + e.getMessage());
        }
    }
}
