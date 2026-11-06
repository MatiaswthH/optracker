
package edu.pucp.plg_back.dto;

import edu.pucp.plg_back.model.Nodo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodoDTO {

    int x;
    int y;
    int direccion; // 0: Arriba, 1: Derecha, 2: Abajo, 3: Izquierda. Para las rutas de los
                   // camiones.

    public void convertirNodoToDTO(Nodo nodo) {
        this.x = nodo.getX();
        this.y = nodo.getY();
    }

}