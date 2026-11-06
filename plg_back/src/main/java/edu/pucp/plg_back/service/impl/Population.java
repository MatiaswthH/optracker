// Population.java
package edu.pucp.plg_back.service.impl;

import java.util.*;

public class Population {
    public List<Chromosome> individuals;

    public Population(int size) {
        individuals = new ArrayList<>(size);
    }

    public void add(Chromosome c) {
        individuals.add(c);
    }

    public void sort() {
        Collections.sort(individuals);
    }

    public Chromosome best() {
        return individuals.get(0);
    }
    
}
