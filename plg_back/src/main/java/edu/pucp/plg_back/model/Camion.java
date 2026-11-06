package edu.pucp.plg_back.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Iterator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.pucp.plg_back.service.impl.Pathfinder;
import edu.pucp.plg_back.simulation.SimulationParams;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Camion {

    /* TODO CAMION AL INICIO EMPIEZA EN EL DEPOSITO CENTRAL (12,8) */

    private String codigo;
    private double cargaGLP;
    private double pesoTara;
    private double pesoCargaGLP;
    private double pesoTotal;
    private Calendar fechaInicio;
    private double combustible;
    private double capacidad;

    @Builder.Default
    private Nodo nodoTrasvase = null; // Nodo donde hara un trasvase

    @Builder.Default
    private LocalDateTime fechaMantenimiento = null;

    @Builder.Default
    private boolean en_mantenimiento = false; // true si el camion esta en mantenimiento

    @Builder.Default
    private Averia averia = null; // averia asignada al camion

    @Builder.Default
    private boolean inmovilizado = false; // Indica si el camión está inmovilizado por una avería

    @Builder.Default
    private boolean enRuta = false; // true si el camion esta en ruta

    @Builder.Default
    private boolean empezandoRuta = false; // true si el camion esta en ruta

    @Builder.Default
    private int tiempoDespacho = 0; // tiempo de despacho

    @Builder.Default
    private boolean despachando = false; // despachando

    @Builder.Default
    private int tiempoTrasvase = 0; // tiempo de trasvase

    @Builder.Default
    private boolean trasvasando = false; // despachando

    @Builder.Default
    private int tiempoSalida = 0; // tiempo de salida de la planta

    @Builder.Default
    private boolean saliendo = false; // saliendo de la planta

    @Builder.Default
    private List<Pedido> listaPedidos = new ArrayList<>();

    @Builder.Default
    List<Nodo> rutaVehiculo = new ArrayList<>();

    @Builder.Default
    Nodo nodoActual = new Nodo(12, 8); // Nodo actual del camión (inicialmente en el depósito central)

    @Builder.Default
    private double cantidadPedidos = 0;

    @Builder.Default
    private double velocidad = 50;

    @Builder.Default
    private boolean averiado = false; // Indica si el camión está disponible para la planificacion

    @Builder.Default
    private double tiempo_averiado = 0; // Establece el tiempo en que el camión no está disponible para la planificación

    @Builder.Default
    private int indiceNodoActual = 0; // Indica en qué nodo de la ruta está el camión

    @Builder.Default
    private ResultadoReplanificacion replanificacion = null; // Resultado de la replanificación del camión

    @Builder.Default
    private boolean replanificar = false; // Indica si el camión debe replanificar su ruta por pedido grande

    private double cargaGLPReplanificar; // Carga GLP para replanificacion

    public void clearCamion() {
        cantidadPedidos = 0;
        listaPedidos.clear();
    }

    public float calculateTimeToDispatch() {
        return ((float) this.rutaVehiculo.size() / (float) this.velocidad) * 60;
    }

    public List<Posicion> getRutaVehiculoPositions(List<Pedido> pedidos) {
        List<Posicion> ruta = new ArrayList<>();
        this.getRutaVehiculo().forEach(nodo -> {
            int esDestino = 0;
            for (Pedido pedido : pedidos) {
                if (nodo.getX() == pedido.getX() && nodo.getY() == pedido.getY()) {
                    esDestino = 1;
                    break;
                }
            }
            ruta.add(Posicion.builder().X(nodo.getX()).Y(nodo.getY()).destino(Integer.valueOf(esDestino)).build());
        });
        return ruta;
    }

    public void regresarAlDeposito(Mapa mapa, List<Bloqueo> bloqueos, LocalDateTime fechaHoraActual,
            List<Almacen> almacenes) {

        this.rutaVehiculo.clear(); // Limpiar la ruta actual del camión
        this.rutaVehiculo.add(nodoActual); // Agregar el nodo actual a la ruta

        if (averia != null) {
            if (averia.getTipo() == 1) {
                System.out.println("Camión " + this.codigo + " sigue su ruta original");
                this.agregarDepositoFinal(almacenes);
                generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
            } else if (averia.getTipo() == 2 || averia.getTipo() == 3) {

                System.out.println("Camión " + this.codigo + " regresando al depósito por avería");
                this.agregarDepositoFinal(almacenes);
                generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
            }
        } else {

            System.out.println("Camión " + this.codigo + " regresando al depósito");
            this.agregarDepositoFinal(almacenes);
            generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
        }

        this.averia = null; // Resetea la avería del camión al regresar al depósito
        // System.out.println("Ruta del camión al depósito: ");
        // for (Nodo nodo : this.rutaVehiculo) {
        // System.out.println("Nodo: " + nodo.getX() + "," + nodo.getY());
        // }
    }

    public void setRutaVehiculoNodos(Ruta ruta) {
        this.rutaVehiculo.clear(); // Limpiar la ruta actual
        for (Nodo nodo : ruta.getRuta()) {
            this.rutaVehiculo.add(nodo);
        }
    }

    public void ubicarInicioRuta() {
        Nodo inicio = rutaVehiculo.getFirst();
        rutaVehiculo.removeFirst();
        setNodoActual(inicio); // Actualizar la posición del camión al primer nodo de la ruta
    }

    public void avanzarNodo() {
        /*
         * if (this.indiceNodoActual < rutaVehiculo.size() - 1) {
         * this.indiceNodoActual++;
         * //setNodoActual(rutaVehiculo.get(this.indiceNodoActual));
         * }
         */

        Nodo siguientePosicion = rutaVehiculo.getFirst(); // siguiente posición de la ruta
        rutaVehiculo.removeFirst(); // eliminar la posición de la ruta
        // System.out.println("Ruta luego de quitar uno:");
        // camion.mostrarRuta();
        setNodoActual(siguientePosicion); // actualizar posición del camión
    }

    public double getPorcentajeAvanceRuta() {
        if (rutaVehiculo.isEmpty())
            return 0.0;
        return (double) this.indiceNodoActual / (double) (rutaVehiculo.size() - 1);
    }

    public void verificarTrasvasePedido(Camion camionInmovilizado) {

        if (this.replanificacion == null)
            return;

        List<Pedido> pedidosARecoger = this.replanificacion.getCamionPedidosTrasvase()
                .getOrDefault(camionInmovilizado, new ArrayList<>());

        if (pedidosARecoger.isEmpty()) {
            return;
        } else {

            // Si hay pedidos a recoger, el camión va a trasvasar los pedidos
            // Por ejemplo si el camion tiene 10 pedidos el camion inmovilizado, el camion
            // que esta avanzando puede
            // recoger solo 2 o dependiendo del valor que tenga el hash map

            Iterator<Pedido> it = camionInmovilizado.getListaPedidos().iterator(); // 10 pedidos
            while (it.hasNext()) {
                Pedido pedido = it.next(); // 1 de los 10 pedidos
                if (pedidosARecoger.contains(pedido)) { // pedidos a recoger (los pedidos que estan en el hash map
                                                        // <camion, List<Pedido> (pedidos a recoger)>)
                    this.listaPedidos.add(pedido); // El camión recoge el pedido
                    this.cargaGLP += pedido.getVolumen(); // Suma el volumen si corresponde
                    camionInmovilizado.setCargaGLP(camionInmovilizado.getCargaGLP() - pedido.getVolumen());
                    it.remove(); // El camión inmovilizado pierde el pedido
                    this.trasvasando = true; // Indica que el camión está trasvasando
                    this.tiempoTrasvase += 15; // Tiempo de trasvase (simulación)
                }
            }
            // Limpia la lista de pedidos trasvasados en la replanificación para este camión
            // inmovilizado
            this.replanificacion.getCamionPedidosTrasvase().get(camionInmovilizado).clear();
        }
    }

    public List<Pedido> verificarEntregaPedido() {
        List<Pedido> pedidosEntregados = new ArrayList<>();
        if (this.nodoActual.getDestino() == 1) { // si el nodo actual es un pedido
            pedidosEntregados = this.entregarPedidos(); // entregar el pedido
        }
        return pedidosEntregados; // retornar la lista de pedidos entregados
    }

    public List<Pedido> entregarPedidos() {
        List<Pedido> pedidosEntregados = new ArrayList<>();
        Iterator<Pedido> iterator = this.listaPedidos.iterator();
        while (iterator.hasNext()) {
            Pedido pedido = iterator.next();
            if (pedido.getX() == nodoActual.getX() && pedido.getY() == nodoActual.getY()) {
                this.cargaGLP -= pedido.getVolumen(); // Restar el volumen del pedido al camión
                if (this.cargaGLP < 0) {
                    this.cargaGLP = 0; // Evitar que la carga sea negativa
                }
                System.out.println("Pedido entregado: " + pedido.getId() + " en la posicion: " + pedido.getX() + ","
                        + pedido.getY() + " por el camion: " + this.codigo);
                iterator.remove(); // Eliminar el pedido de la lista de manera segura
                this.tiempoDespacho += 15; // Vendría a ser 15 minutos en nuestra escala
                                           // de tiempo (simulación)
                this.despachando = true; // camion despachando
                pedidosEntregados.add(pedido); // Agregar el pedido a la lista de pedidos entregados
            }
        }
        return pedidosEntregados; // retornar la lista de pedidos entregados
    }

    public void entregarPedido(Pedido pedido) {
        // Entregar un pedido específico
        this.cargaGLP -= pedido.getVolumen(); // Restar el volumen del pedido al camión
        listaPedidos.remove(pedido); // Eliminar el pedido de la lista
        System.out.println("Pedido entregado: " + pedido.getId() + " en la posicion: " + pedido.getX() + ","
                + pedido.getY() + " por el camion: " + this.codigo);
        this.tiempoDespacho += 15; // Vendría a ser 15 minutos en nuestra escala
                                   // de tiempo (simulación)
        this.despachando = true; // camion despachando
    }

    public List<Pedido> procesarPedidos() {
        List<Pedido> pedidosEntregados = new ArrayList<>(); // Lista de pedidos a entregados
        if (this.isEnRuta()) {
            pedidosEntregados = this.verificarEntregaPedido(); // verificar si el camión ha llegado a un
                                                               // pedido. Despacha si llego
        }
        return pedidosEntregados; // retornar la lista de pedidos entregados
    }

    public void salidaDePlanta() {
        SimulationParams cfg = SimulationParams.builder().build();
        if (nodoActual.getX() == 12 && nodoActual.getY() == 8 && this.enRuta && this.empezandoRuta) { // si el camion
                                                                                                      // esta en el
                                                                                                      // deposito
            this.saliendo = true; // camion saliendo de la planta
            this.tiempoSalida += 10; // tiempo de salida de la planta (simulacion)
        }
    }

    public void mostrarRuta() {
        System.out.println("Ruta del camion " + this.codigo + ": ");
        for (Nodo nodo : this.rutaVehiculo) {
            System.out.print(nodo.toString() + " -> ");
        }
        System.out.println("Fin de la ruta");
    }

    public void generarCaminosIntermedios(Mapa mapa, List<Bloqueo> bloqueos, LocalDateTime fechaHoraActual) {

        List<Nodo> rutaActual = new ArrayList<>();
        List<Nodo> rutaCompleta = new ArrayList<>();

        rutaActual = this.rutaVehiculo;

        if (rutaActual.isEmpty()) {
            System.out.println("Ruta vacia, no se puede generar caminos intermedios para el camion " + this.codigo
                    + " - en el nodo: " + this.nodoActual.toString());
            return; // no hay ruta
        }

        Nodo nodoInicio = rutaActual.removeFirst(); // sacar el primer nodo de la ruta
        Iterator<Nodo> iterator = rutaActual.iterator();
        Pathfinder pathfinder = new Pathfinder();
        while (iterator.hasNext()) {
            Nodo nodoDestino = iterator.next(); // sacar el siguiente nodo de la ruta
            Pathfinder.PathResult result = pathfinder.findShortestPath(nodoInicio, nodoDestino, mapa, bloqueos,
                    fechaHoraActual);

            if (result.isFound() == false) {
                rutaVehiculo.clear();
                ; // si no se encuentra un camino, se elimina la ruta
                return;
            }
            rutaCompleta.addAll(result.getPath());

            nodoInicio = nodoDestino; // actualizar el nodo de inicio
        }
        // System.out.println("Ruta generada actual del camion");
        // for (Nodo nodo : rutaVehiculo) {
        // System.out.println("Nodo: " + nodo.getX() + "," + nodo.getY());
        // }
        // System.out.println("Ruta generada por los caminos intermedios");
        // for (Nodo nodo : rutaCompleta) {
        // System.out.println("Nodo: " + nodo.getX() + "," + nodo.getY());
        // }
        this.rutaVehiculo = rutaCompleta; // actualizar la ruta del camion
    }

    public boolean sufreAveria(LocalDateTime fecha) {
        double hora = fecha.getHour();
        String turno = "";
        if (hora < 8) { // Turno 1
            turno = "T1"; // Disponible en el turno 3 del mismo día
        } else if (hora < 16) { // Turno 2
            turno = "T2"; // Disponible en el turno 1 del día siguiente
        } else { // Turno 3
            turno = "T3"; // Disponible en el turno 2 del día siguiente
        }
        if (turno.equals(averia.getTurno())) {
            return true;
        } else {
            return false; // El camión no está en el turno de la avería
        }
    }

    public boolean estaInamovible(LocalDateTime fechaActual) {
        if (averia == null) {
            return false; // No hay incidente, el camión está disponible
        }
        if (fechaActual.isAfter(averia.getFechaDisponible())) {
            liberarCamion(); // Liberar el camión si ya está disponible
            return false;
        }
        return true; // El camión sigue inmovilizado
    }

    public void liberarCamion() {
        // System.out.println("Camión " + codigo + " ha sido liberado del incidente.");
        this.averia = null; // Eliminar el incidente
        this.enRuta = false; // El camión regresa al depósito
    }

    public static List<Camion> getListaFlotaEstatica() { // Funcion que te da la flota definida en el archivo de
                                                         // configuracion
        List<Camion> flota = new ArrayList<>();
        // Set a start time for trucks
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.YEAR, 2025);
        startCal.set(Calendar.MONTH, Calendar.JANUARY); // Enero es 0 en Calendar
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        // Velocidad promedio 50 km/h según QA.md
        double velocidadPromedio = 100.0;

        // Añadir camiones según tipos y unidades en QA.md
        // 2 x TA (25 m³)
        flota.add(Camion.builder().codigo("TA01").capacidad(25).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TA02").capacidad(25).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        // 4 x TB (15 m³)
        flota.add(Camion.builder().codigo("TB01").capacidad(15).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TB02").capacidad(15).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TB03").capacidad(15).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TB04").capacidad(15).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        // 4 x TC (10 m³)
        flota.add(Camion.builder().codigo("TC01").capacidad(10).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TC02").capacidad(10).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TC03").capacidad(10).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TC04").capacidad(10).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        // 10 x TD (5 m³)
        flota.add(Camion.builder().codigo("TD01").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD02").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD03").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD04").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD05").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD06").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD07").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD08").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD09").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());
        flota.add(Camion.builder().codigo("TD10").capacidad(5).velocidad(velocidadPromedio)
                .fechaInicio((Calendar) startCal.clone()).build());

        return flota; // retornar la flota de camiones
    }

    public void reducirTiempos() {
        SimulationParams cfg = SimulationParams.builder().build();
        int unMinuto = cfg.getUnMinutoSimulacion();
        if (this.tiempoSalida > 0)
            this.tiempoSalida -= unMinuto;
        if (this.tiempoDespacho > 0)
            this.tiempoDespacho -= unMinuto;
        if (this.tiempoSalida < 0)
            this.tiempoSalida = 0;
        if (this.tiempoDespacho < 0)
            this.tiempoDespacho = 0;
        if (this.tiempoSalida == 0) {
            this.saliendo = false;
            this.empezandoRuta = false;
        }
        if (this.tiempoDespacho == 0)
            this.despachando = false;
    }

    public void agregarDepositoFinal(List<Almacen> almacenes) {

        // Agregar el nodo del depósito central al final de la ruta
        Nodo ultimoNodo = rutaVehiculo.get(rutaVehiculo.size() - 1);
        Nodo nodoFinal = ultimoNodo; // Por defecto, el último nodo es el mismo último nodo de la ruta
        double distanciaMasCorata = Double.MAX_VALUE;

        // Encontrar el depósito más cercano
        for (Almacen almacen : almacenes) {
            Nodo deposito = new Nodo(almacen.getX(), almacen.getY());
            double distancia = ultimoNodo.getDistancia(deposito);
            // Si es la dist mas corta y (la carga del almacen supera la capacidad del
            // camion o si es un camion chico de 5m3, solo considerar si el almacen tiene
            // carga)
            if (distancia < distanciaMasCorata
                    && (almacen.getCargaGLP() > this.capacidad || (this.capacidad == 5 && almacen.getCargaGLP() > 0)
                            || almacen.isPrincipal())) {
                distanciaMasCorata = distancia;
                nodoFinal = deposito; // Actualizar el último nodo al depósito más cercano
            }
        }

        // Agregar el depósito más cercano al final de la ruta
        rutaVehiculo.add(nodoFinal);
    }

    public boolean estaEnDeposito(Mapa mapa) {

        // Verifica si el camión está en el depósito central
        Nodo depositoCentral = mapa.getDepositoCentral();
        Nodo depositoIntNorte = mapa.getDepositoIntNorte();
        Nodo depositoIntEste = mapa.getDepositoIntEste();

        boolean enDepositoCentral = nodoActual.equals(depositoCentral);
        boolean enDepositoNorte = nodoActual.equals(depositoIntNorte);
        boolean enDepositoEste = nodoActual.equals(depositoIntEste);

        if (!saliendo) {
            if (enDepositoCentral || enDepositoNorte || enDepositoEste) {
                this.nodoActual = enDepositoCentral ? depositoCentral
                        : enDepositoEste ? depositoIntEste : depositoIntNorte; // Reiniciar el nodo actual
                return true; // El camión está en el depósito
            }
        }

        return false; // El camión no está en el depósito final
    }

    public void salirDeDepositoVacio(List<Almacen> almacenes, Mapa mapa, List<Bloqueo> bloqueos,
            LocalDateTime fechaHoraActual) {
        // Verifica si el camión está en el depósito y no tiene pedidos
        for (Almacen a : almacenes) {
            if (a.getX() == this.nodoActual.getX() && a.getY() == this.nodoActual.getY()) {
                if ((a.getCargaGLP() < this.capacidad
                        || (a.getCargaGLP() <= 5
                                && this.capacidad == 5 && a.getCamionesEnAlmacen().size() > 1 && a.getCargaGLP() > 0))
                        || (a.getCargaGLP() == 0)
                                && this.listaPedidos.isEmpty() && !this.enRuta) {
                    System.out.println(
                            "Camión " + this.codigo + " esta en un depósito vacio, buscando otro deposito con carga.");
                    rutaVehiculo.add(this.nodoActual); // Agregar el nodo actual a la ruta
                    agregarDepositoFinal(almacenes); // Agregar el depósito más cercano
                    generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
                    this.enRuta = true; // El camión sale en ruta
                    break;
                }
            }
        }
    }

    public List<Pedido> replanificarRuta(Mapa mapa, List<Bloqueo> bloqueos,
            List<Almacen> almacenes, LocalDateTime fechaHoraActual) {
        List<Pedido> pedidosReplanificar = new ArrayList<>();
        if (this.enRuta && !this.listaPedidos.isEmpty()) {
            for (Pedido p : this.listaPedidos) {
                // Si el camion tiene un pedido critico, se cancela la replanificacion
                if (p.isCritico(fechaHoraActual)) {
                    pedidosReplanificar.clear(); // Limpiar la lista de pedidos a replanificar
                    return pedidosReplanificar; // Retorna lista vacía.
                }
                pedidosReplanificar.add(p); // Agregar el pedido a la lista de pedidos a replanificar
            }
            // Si llega acá, no hubo pedidos críticos. Replanificar.
            this.replanificar = true; // Indica que el camión debe replanificar su ruta
            this.cargaGLPReplanificar = this.cargaGLP; // Guardar la carga GLP actual para replanificación
            this.rutaVehiculo.clear(); // Limpiar la ruta del camión
            System.out.println("Camión " + this.codigo + " replanificando ruta por pedidos no críticos.");
            for (Pedido p : this.listaPedidos) {
                p.setCamionAsignado(null); // Desasignar el camión de los pedidos
                p.setAlmacenAsignado(null); // Desasignar el almacén de los pedidos
            }
            // Regresa el camion a un deposito. Pero si el camion se le planifica un pedido,
            // esta ruta se deberia sobreescribir
            this.listaPedidos.clear(); // Limpiar la lista de pedidos del camión
            rutaVehiculo.add(this.nodoActual); // Agregar el nodo actual a la ruta
            agregarDepositoFinal(almacenes); // Agregar el depósito más cercano
            generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
        }
        return pedidosReplanificar; // Retorna la lista de pedidos a replanificar
    }

    public void procesarAveria() {
        // Procesar la avería del camión según el tipo de incidente
        this.rutaVehiculo.clear();
        averia.calcularTiempoDisponible();
        tiempo_averiado = averia.getTiempoIndisponibilidad(); // Actualizar el tiempo de indisponibilidad del camión
    }

    public boolean verificarTrasvase() {
        if (nodoTrasvase == null)
            return false;
        if (nodoActual.getX() == nodoTrasvase.getX() && nodoActual.getY() == nodoTrasvase.getY()) {
            this.tiempoTrasvase += 15;
            setTrasvasando(true);
            this.nodoTrasvase = null;
            return true;
        }
        return false;
    }

}