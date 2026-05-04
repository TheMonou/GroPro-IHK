package de.monou.strategie;

import de.monou.model.Strecke;

import java.util.ArrayList;
import java.util.List;

public class BeidseitigesWarten implements FahrplanStrategie {
    @Override
    public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
        ArrayList<Strecke> fahrplan = new ArrayList<>();
        return strecken;
    }

    @Override
    public String getName() {
        return "Beidseitiges Warten";
    }
}
