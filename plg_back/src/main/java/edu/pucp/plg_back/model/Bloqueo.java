package edu.pucp.plg_back.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class Bloqueo {
    // conjunto de nodos bloqueados
    // estos forman un conjunto de segmentos lo cual sera considerado como un tramo
    // este tramo será polígono abierto
    private int id;
    private List<Nodo> tramo;
    private String periodo;
    private String motivo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public Boolean isActual(LocalDateTime fechaActual){
        return (fechaActual.isAfter(fechaInicio) && fechaActual.isBefore(fechaFin));
    }

    public void mostrarBloqueo() {
        System.out.println("=== Información del Bloqueo ===");
        System.out.println("Motivo: " + motivo);
        System.out.println("Periodo: " + periodo);
        System.out.println("Fecha de inicio: " + fechaInicio);
        System.out.println("Fecha de fin: " + fechaFin);
        System.out.println("Tramo bloqueado:");
        for (Nodo nodo : tramo) {
            System.out.println("  - " + nodo);
        }
        System.out.println("===============================");
    }
}
