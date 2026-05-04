package de.monou.strategie;

import de.monou.model.Strecke;

import java.util.List;

public interface FahrplanStrategie {
    List<Strecke> ermittleFahrplan(List<Strecke> strecken);
    String getName();
}
