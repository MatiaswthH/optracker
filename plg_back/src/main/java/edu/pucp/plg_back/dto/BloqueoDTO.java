
package edu.pucp.plg_back.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.cglib.core.Local;

import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Nodo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BloqueoDTO {

    private int id;
    private List<NodoDTO> tramo;
    private List<NodoDTO> nodosIntermedios;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Duration tiempoRestante;

    public void convertirBloqueoToDTO(Bloqueo bloqueo, LocalDateTime fechaHoraActual) {
        this.id = bloqueo.getId();
        this.tramo = new ArrayList<>();
        this.fechaInicio = bloqueo.getFechaInicio();
        this.fechaFin = bloqueo.getFechaFin();
        for (Nodo nodo : bloqueo.getTramo()) {
            NodoDTO nodoDTO = new NodoDTO();
            nodoDTO.convertirNodoToDTO(nodo);
            this.tramo.add(nodoDTO);
        }
        this.generateNodosIntermedios();
        this.tiempoRestante = Duration.between(fechaHoraActual, this.fechaFin);
    }

    public void generarTramosCompletos() {
        List<NodoDTO> tramosCompletos = new ArrayList<>();
        List<NodoDTO> tramoActual = new ArrayList<>();
        tramoActual = this.tramo;
        // Generar los puntos intermedios de los tramos para enviar al front
        NodoDTO nodoInicio = tramoActual.removeFirst(); // sacar el primer nodo de la ruta
        Iterator<NodoDTO> iterator = tramoActual.iterator();
        while (iterator.hasNext()) {
            NodoDTO nodoDestino = iterator.next(); // sacar el siguiente nodo de la ruta
            if (nodoDestino.getX() == nodoInicio.getX()) {
                for (int y = Math.min(nodoDestino.getY(), nodoInicio.getY()) + 1; y < Math.max(nodoDestino.getY(),
                        nodoInicio.getY()); y++) {
                    NodoDTO nodoIntermedio = new NodoDTO();
                    nodoIntermedio.setX(nodoInicio.getX());
                    nodoIntermedio.setY(y);
                    tramosCompletos.add(nodoIntermedio);
                }
            } else if (nodoDestino.getY() == nodoInicio.getY()) {
                for (int x = Math.min(nodoDestino.getX(), nodoInicio.getX()) + 1; x < Math.max(nodoDestino.getX(),
                        nodoInicio.getX()); x++) {
                    NodoDTO nodoIntermedio = new NodoDTO();
                    nodoIntermedio.setX(x);
                    nodoIntermedio.setY(nodoInicio.getY());
                    tramosCompletos.add(nodoIntermedio);
                }
            }
            tramosCompletos.add(nodoDestino);
            nodoInicio = nodoDestino; // actualizar el nodo de inicio
        }
        this.tramo = tramosCompletos;
    }

    private void generateNodosIntermedios() {
        List<NodoDTO> edgeNodes = new ArrayList<>();
        int n = this.tramo.size();
        if (n == 0)
            return;

        // Always include the starting node (nodoActual)
        edgeNodes.add(this.tramo.get(0));

        for (int i = 0; i < n - 1; i++) {
            NodoDTO prev;
            if (i == 0) {
                prev = this.tramo.get(0);
            } else {
                prev = this.tramo.get(i - 1);
            }
            NodoDTO curr = this.tramo.get(i);
            NodoDTO next = this.tramo.get(i + 1);

            // Calculate direction from prev->curr and curr->next
            int dx1 = curr.getX() - prev.getX();
            int dy1 = curr.getY() - prev.getY();
            int dx2 = next.getX() - curr.getX();
            int dy2 = next.getY() - curr.getY();

            // If direction changes, it's a turn (edge node)
            if (dx1 != dx2 || dy1 != dy2) {
                edgeNodes.add(curr);
            }
        }

        // Always include the last node
        edgeNodes.add(this.tramo.get(n - 1));

        this.nodosIntermedios = edgeNodes;
    }
}