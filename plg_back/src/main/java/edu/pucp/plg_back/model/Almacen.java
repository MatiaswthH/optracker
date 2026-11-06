package edu.pucp.plg_back.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Almacen {
    private int id;
    private boolean principal;
    private int capacidad;
    private int cargaGLP;
    private int x;
    private int y;
    private List<Pedido> pedidosAlmacen; // Pedido asignados al almacen
    private List<Pedido> pedidosAsignados; // Pedidos asignados a la flota del almacen
    private List<Camion> camionesEnAlmacen;

    public void actualizarCamionesEnAlmacen(List<Camion> camiones) {
        List<Camion> camionesEnAlmacen = new ArrayList<>();
        for (Camion camion : camiones) {
            if (camion.getNodoActual().getX() == this.x && camion.getNodoActual().getY() == this.y
                    && !camion.isEnRuta()) {
                camionesEnAlmacen.add(camion);
            }
        }
        // System.out.println("Camiones en almacen " + this.id + ": " +
        // camionesEnAlmacen.size());
        this.camionesEnAlmacen = camionesEnAlmacen;
    }

    public void actualizarPedidosAsignados() {
        for (Camion camion : this.camionesEnAlmacen) {
            for (Pedido pedido : camion.getListaPedidos()) {
                if (!this.pedidosAsignados.contains(pedido)) {
                    this.pedidosAsignados.add(pedido);
                }
            }
        }
    }

    public static List<Almacen> getListaAlmacenesEstatica() {
        List<Almacen> almacenes = new ArrayList<>();
        almacenes.add(Almacen.builder()
                .id(1)
                .principal(true)
                .capacidad(Integer.MAX_VALUE)
                .cargaGLP(Integer.MAX_VALUE)
                .x(12)
                .y(8)
                .camionesEnAlmacen(new ArrayList<>())
                .pedidosAlmacen(new ArrayList<>())
                .pedidosAsignados(new ArrayList<>())
                .build());
        almacenes.add(Almacen.builder()
                .id(2).principal(false)
                .capacidad(160).cargaGLP(160)
                .x(42).y(42)
                .camionesEnAlmacen(new ArrayList<>()).pedidosAlmacen(new ArrayList<>())
                .pedidosAsignados(new ArrayList<>()).build());
        almacenes.add(Almacen.builder()
                .id(3).principal(false)
                .capacidad(160).cargaGLP(160)
                .x(63).y(3)
                .camionesEnAlmacen(new ArrayList<>()).pedidosAlmacen(new ArrayList<>())
                .pedidosAsignados(new ArrayList<>()).build());
        return almacenes;
    }

    public List<Camion> verificarCamionesEnAlmacen(List<Camion> camiones) {
        List<Camion> camionesEnAlmacen = new ArrayList<>();
        for (Camion camion : camiones) {
            for (Camion almacenCamion : this.camionesEnAlmacen) {
                if (camion.getCodigo() == almacenCamion.getCodigo()) {
                    camionesEnAlmacen.add(camion);
                }
            }
        }
        return camionesEnAlmacen;
    }

    // La capacidad de toda la flota en el almacen
    public int getCapacidadFlota() {
        int capacidadFlota = 0;
        for (Camion camion : this.camionesEnAlmacen) {
            capacidadFlota += (camion.getCapacidad() - camion.getCargaGLP());
        }
        return capacidadFlota;
    }

    // Verifica si un camion esta en el almacen
    public boolean camionEstaEnAlmacen(Camion camion) {
        for (Camion almacenCamion : this.camionesEnAlmacen) {
            if (camion.getCodigo() == almacenCamion.getCodigo()) {
                return true;
            }
        }
        return false;
    }

    // Quitar un camion del almacen
    public void quitarCamionDelAlmacen(Camion camion) {
        List<Camion> camionesActualizados = new ArrayList<>();
        for (Camion almacenCamion : this.camionesEnAlmacen) {
            if (camion.getCodigo() != almacenCamion.getCodigo()) {
                camionesActualizados.add(almacenCamion);
            }
        }
        this.camionesEnAlmacen = camionesActualizados;
    }

    public void sacarCamionesDeAlmacen(List<Almacen> almacenes, Mapa mapa, List<Bloqueo> bloqueos,
            LocalDateTime fechaHoraActual) {
        for (Camion c : this.camionesEnAlmacen) {
            if ((this.getCargaGLP() < c.getCapacidad()
                    || (this.getCargaGLP() <= 5
                            && c.getCapacidad() == 5 && this.getCamionesEnAlmacen().size() > 1
                            && this.getCargaGLP() > 0))
                    || (this.getCargaGLP() == 0)
                            && c.getListaPedidos().isEmpty() && !c.isEnRuta()) {
                System.out.println(
                        "Camión " + c.getCodigo() + " esta en un depósito vacio, buscando otro deposito con carga.");
                List<Nodo> rutaVehiculo = c.getRutaVehiculo();
                rutaVehiculo.clear();
                rutaVehiculo.add(c.getNodoActual());
                c.setRutaVehiculo(rutaVehiculo); // Agregar el nodo actual a la ruta
                c.agregarDepositoFinal(almacenes); // Agregar el depósito más cercano
                c.generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
                c.setEnRuta(true); // El camión sale en ruta
            }
        }
    }
}
