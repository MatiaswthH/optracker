// GAParams.java
package edu.pucp.plg_back.service.impl;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GAParams {
    @Builder.Default
    private int popSize = 80; // tamaño población
    @Builder.Default
    private int nGenerations = 400; // ciclos evolutivos
    @Builder.Default
    private double crossoverProb = 0.9; // probabilidad cruce
    @Builder.Default
    private double mutationProb = 0.1; // probabilidad mutación
    @Builder.Default
    private int tournamentK = 3; // torneo selección
}
