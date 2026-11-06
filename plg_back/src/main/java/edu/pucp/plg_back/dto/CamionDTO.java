package edu.pucp.plg_back.dto;

import java.util.ArrayList;
import java.util.List;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Nodo;
import edu.pucp.plg_back.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CamionDTO {
    private String codigo;
    private double cargaGLP;
    // private double pesoTara;
    // private double pesoCargaGLP;
    // private double pesoTotal;
    // private double combustible;
    private double capacidad;

    // private MantenimientoDTO mantenimiento; //No hay modelo mantenimiento,
    // arreglar luego

    private AveriaDTO averia = null; // averia asignada al camion

    private boolean inmovilizado = false; // Indica si el camión está inmovilizado por una avería

    private boolean enRuta = false; // true si el camion esta en ruta

    // private boolean empezandoRuta = false; // true si el camion esta en ruta

    // private int tiempoDespacho = 0; // tiempo de despacho

    private boolean despachando = false; // despachando

    // private int tiempoTrasvase = 0; // tiempo de trasvase

    private boolean trasvasando = false; // despachando

    // private int tiempoSalida = 0; // tiempo de salida de la planta

    private boolean saliendo = true; // saliendo de la planta

    private List<String> listaPedidos = new ArrayList<>();

    List<NodoDTO> rutaVehiculo = new ArrayList<>();

    List<NodoDTO> paradas = new ArrayList<>();

    private NodoDTO nodoActual = null; // Nodo actual del camión

    private int cantidadPedidos = 0;

    private boolean enMantenimiento = false;

    // public void convertirCamionToDTO(Camion camion) {
    // this.codigo = camion.getCodigo();
    // this.cargaGLP = camion.getCargaGLP();
    // this.pesoTara = camion.getPesoTara();
    // this.pesoCargaGLP = camion.getPesoCargaGLP();
    // this.pesoTotal = camion.getPesoTotal();
    // this.combustible = camion.getCombustible();
    // this.capacidad = camion.getCapacidad();

    // if (camion.getAveria() != null) {
    // this.averia = new AveriaDTO();
    // this.averia.convertirAveriaToDTO(camion.getAveria());
    // this.inmovilizado = true;
    // }

    // this.enRuta = camion.isEnRuta();
    // this.empezandoRuta = camion.isEmpezandoRuta();
    // this.tiempoDespacho = camion.getTiempoDespacho();
    // this.despachando = camion.isDespachando();
    // this.tiempoTrasvase = camion.getTiempoTrasvase();
    // this.trasvasando = camion.isTrasvasando();
    // this.tiempoSalida = camion.getTiempoSalida();
    // this.saliendo = camion.isSaliendo();

    // for (Pedido pedido : camion.getListaPedidos()) {
    // PedidoDTO pedidoDTO = new PedidoDTO();
    // pedidoDTO.convertirPedidoToDTO(pedido);
    // listaPedidos.add(pedidoDTO);
    // cantidadPedidos++;
    // }

    // for (Nodo nodo : camion.getRutaVehiculo()) {
    // NodoDTO nodoDTO = new NodoDTO();
    // nodoDTO.convertirNodoToDTO(nodo);
    // rutaVehiculo.add(nodoDTO);
    // }
    // }
    public void convertirCamionToDTO(Camion camion) {
        this.codigo = camion.getCodigo();
        this.cargaGLP = camion.getCargaGLP();
        this.capacidad = camion.getCapacidad();
        this.nodoActual = new NodoDTO();
        this.nodoActual.convertirNodoToDTO(camion.getNodoActual());
        this.inmovilizado = camion.isInmovilizado();
        this.trasvasando = camion.isTrasvasando();
        this.saliendo = camion.isSaliendo();
        this.despachando = camion.isDespachando();
        this.enRuta = camion.isEnRuta();
        this.enMantenimiento = camion.isEn_mantenimiento();
        if (camion.getAveria() != null) {
            this.averia = new AveriaDTO();
            this.averia.convertirAveriaToDTO(camion.getAveria());
        }
        for (Nodo nodo : camion.getRutaVehiculo()) {
            NodoDTO nodoDTO = new NodoDTO();
            nodoDTO.convertirNodoToDTO(nodo);
            this.rutaVehiculo.add(nodoDTO);
        }
        for (Pedido p : camion.getListaPedidos()) {
            this.listaPedidos.add(p.getId() + "-" + p.getChildId());
            NodoDTO nodoDTO = new NodoDTO();
            nodoDTO.setX(p.getX());
            nodoDTO.setY(p.getY());
            this.paradas.add(nodoDTO);
        }
        if (!rutaVehiculo.isEmpty()) {
            NodoDTO nodoAlmacen = new NodoDTO();
            nodoAlmacen = this.rutaVehiculo.getLast();
            this.paradas.add(nodoAlmacen);
        }
        this.cantidadPedidos = camion.getListaPedidos().size();
        // Añadir direcciones a la ruta del camión para mostrar en el front
        this.addDireccionRutaDTO();
        this.generateNodosIntermedios();
    }

    private void generateNodosIntermedios() {
        List<NodoDTO> edgeNodes = new ArrayList<>();
        int n = this.rutaVehiculo.size();
        if (n == 0)
            return;

        // Always include the starting node (nodoActual)
        edgeNodes.add(this.nodoActual);

        for (int i = 0; i < n - 1; i++) {
            NodoDTO prev;
            if (i == 0) {
                prev = this.nodoActual;
            } else {
                prev = this.rutaVehiculo.get(i - 1);
            }
            NodoDTO curr = this.rutaVehiculo.get(i);
            NodoDTO next = this.rutaVehiculo.get(i + 1);

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
        edgeNodes.add(this.rutaVehiculo.get(n - 1));

        this.rutaVehiculo = edgeNodes;
    }

    private void addDireccionRutaDTO() {
        if (rutaVehiculo.isEmpty()) {
            return; // No hay nodos en la ruta
        }
        NodoDTO nodoSiguiente = this.rutaVehiculo.get(0);
        if (nodoSiguiente.getX() == nodoActual.getX() && nodoSiguiente.getY() > nodoActual.getY()) {
            nodoActual.setDireccion(0); // Arriba
        } else if (nodoSiguiente.getX() > nodoActual.getX() && nodoSiguiente.getY() == nodoActual.getY()) {
            nodoActual.setDireccion(1); // Derecha
        } else if (nodoSiguiente.getX() == nodoActual.getX() && nodoSiguiente.getY() < nodoActual.getY()) {
            nodoActual.setDireccion(2); // Abajo
        } else if (nodoSiguiente.getX() < nodoActual.getX() && nodoSiguiente.getY() == nodoActual.getY()) {
            nodoActual.setDireccion(3); // Izquierda
        }
    }
}
