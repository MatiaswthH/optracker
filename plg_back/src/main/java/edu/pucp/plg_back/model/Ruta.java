package edu.pucp.plg_back.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Ruta implements Comparable<Ruta> {
    private List<Nodo> ruta;
    private String startTime;
    private String endTime;

    @Override
    public int compareTo(Ruta ruta) {
        return this.getStartTime().compareTo(ruta.getStartTime());
    }

    public void mostrarRuta() {
        System.out.println("Ruta: ");
        for (Nodo nodo : ruta) {
            System.out.print(nodo.toString() + " -> ");
        }
        System.out.println("Fin de la ruta");
    }
}
