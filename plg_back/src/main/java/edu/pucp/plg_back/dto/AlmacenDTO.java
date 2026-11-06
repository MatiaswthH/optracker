package edu.pucp.plg_back.dto;

import java.util.ArrayList;
import java.util.List;

import edu.pucp.plg_back.model.Almacen;
import edu.pucp.plg_back.model.Camion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlmacenDTO {
    private int id;
    private boolean principal;
    private int capacidad;
    private int cargaGLP;
    private int x;
    private int y;
    private int cantCamionesEnAlmacen; // Cantidad de camiones en el almacen
    private int cantPedidosAsignados; // Pedidos asignados a la flota del almacen
    private List<String> camionesEnAlmacen; // Lista de IDs de camiones en el almacen

    public void convertirAlmacenToDTO(Almacen almacen) {
        this.id = almacen.getId();
        this.principal = almacen.isPrincipal();
        this.capacidad = almacen.getCapacidad();
        this.cargaGLP = almacen.getCargaGLP();
        this.x = almacen.getX();
        this.y = almacen.getY();
        this.cantCamionesEnAlmacen = almacen.getCamionesEnAlmacen().size();
        this.cantPedidosAsignados = almacen.getPedidosAsignados().size();
        this.camionesEnAlmacen = new ArrayList<>();
        for (Camion camion : almacen.getCamionesEnAlmacen()) {
            this.camionesEnAlmacen.add(camion.getCodigo());
        }
    }
}
