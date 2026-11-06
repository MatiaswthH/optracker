
package edu.pucp.plg_back.dto;

import java.time.LocalDateTime;

import edu.pucp.plg_back.model.Averia;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AveriaDTO {

    private int tipo; // Tipo de incidente (1, 2 o 3)
    private String turno; // turno 1, 2 o 3
    private String codigoUnidad;
    private NodoDTO nodoAveria; // Nodo donde ocurre la averia
    private LocalDateTime fechaFinAveria;

    public void convertirAveriaToDTO(Averia averia) {
        this.tipo = averia.getTipo();
        this.turno = averia.getTurno();
        this.codigoUnidad = averia.getCodigoUnidad();
        if (averia.getNodoAveria() != null) {
            this.nodoAveria = new NodoDTO();
            this.nodoAveria.convertirNodoToDTO(averia.getNodoAveria());
        } else {
            this.nodoAveria = null;
        }
        this.fechaFinAveria = averia.getFechaDisponible();
    }

}