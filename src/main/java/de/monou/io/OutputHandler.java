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
        String filepath = OUTPUT_PREFIX + filenameOnly + ".out";



    }
}
