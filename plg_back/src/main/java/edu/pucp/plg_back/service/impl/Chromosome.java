// Chromosome.java
package edu.pucp.plg_back.service.impl;

import lombok.*;
import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Chromosome implements Comparable<Chromosome> {
    /** Secuencia de índices de destinos (0 = depósito) que visita el camión */
    private List<Integer> genes;

    private double fitness; // distancia total (fitness = 1/dist)

    @Override
    public int compareTo(Chromosome o) {
        return Double.compare(o.fitness, this.fitness); // orden descendente
    }
}
