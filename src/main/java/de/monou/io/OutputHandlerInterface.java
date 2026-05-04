package de.monou.io;

import de.monou.model.Strecke;

import java.util.List;

public interface OutputHandlerInterface {

    void createOutput(List<List<Strecke>> strecken, List<String> namen, String filename);
    int[] berechneWartezeiten(List<Strecke> strecken);
    int berechneScore(int[] wartezeiten);
}
