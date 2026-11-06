package edu.pucp.plg_back.simulation;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
// import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import edu.pucp.plg_back.util.FileLogger;
import edu.pucp.plg_back.dto.AveriaRequest;
import edu.pucp.plg_back.dto.LogEventoDTO;
import edu.pucp.plg_back.dto.PedidoDTO;
import edu.pucp.plg_back.dto.ResultadoSimulacionDTO;
import edu.pucp.plg_back.dto.SnapshotDTO;
import edu.pucp.plg_back.model.Almacen;
import edu.pucp.plg_back.model.Averia;
import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Mapa;
import edu.pucp.plg_back.model.Nodo;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.ResultadoPlanificacion;
import edu.pucp.plg_back.service.FileDataService;
import edu.pucp.plg_back.service.fileReader.ArchivosReader;
import edu.pucp.plg_back.service.fileReader.AveriaService;
import edu.pucp.plg_back.service.fileReader.BloqueoService;
import edu.pucp.plg_back.service.fileReader.MantenimientoService;
import edu.pucp.plg_back.service.impl.GAPlanificador;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class DiarioService {

    @Autowired
    private FileDataService fileDataService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final SimulationParams cfg = SimulationParams.builder().build();
    private final Mapa mapa = new Mapa(); // Mapa de la simulacion
    /*
     * private static List<Camion> camionesInmovilizados = new ArrayList<>();
     * private static BloqueoService bloqueoService = new BloqueoService();
     * private static AveriaService averiaService = new AveriaService();
     * private static List<Bloqueo> bloqueos = new ArrayList<>();
     * private static Map<String, List<LocalDate>> camionesEnMantenimiento = new
     * HashMap<>();
     * private static List<Averia> averias = new ArrayList<>();
     * private static List<Camion> flota = new ArrayList<>();
     * private static SimulationService simulation = new SimulationService();
     * private static GAPlanificador ag = new GAPlanificador();
     */
    private List<Camion> camionesXRecogerPedidos = new ArrayList<>();
    private BloqueoService bloqueoService = new BloqueoService();
    private AveriaService averiaService = new AveriaService();
    private List<Bloqueo> bloqueos = new ArrayList<>();
    private Map<String, List<LocalDate>> camionesEnMantenimiento = new HashMap<>();
    private List<Averia> averias = new ArrayList<>();
    private List<Camion> flota = new ArrayList<>();
    private List<Almacen> almacenes = new ArrayList<>();
    private GAPlanificador ag = new GAPlanificador();
    private List<Bloqueo> bloqueosPrevios = new ArrayList<>(); // Track previous blockages
    private MantenimientoService mantenimientoService = new MantenimientoService();

    // Variables para simulacion diaria
    private final Semaphore simulationLock = new Semaphore(0);
    private final BlockingQueue<Pedido> nuevosPedidosQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<AveriaRequest> averiasSolicitadasQueue = new LinkedBlockingQueue<>();

    private volatile boolean running = false;
    private Thread simulationThread;

    private volatile LocalDateTime fechaActualSimulacion = null;

    public LocalDateTime getFechaActualSimulacion() {
        return fechaActualSimulacion;
    }

    private int nextPedidoId = 1;

    // Metodos para simulacion diaria
    public void setFechaActualSimulacion(LocalDateTime fecha) {
        this.fechaActualSimulacion = fecha;
    }

    public void SimulacionService() {
    }

    public void signalToContinue() {
        simulationLock.release();
    }

    public synchronized void stopSimulacion() {
        running = false;
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
            try {
                simulationThread.join(); // Espera a que termine el hilo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        simulationThread = null;
    }

    public void addPedido(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(nextPedidoId++);
        }
        nuevosPedidosQueue.add(pedido);
    }

    public void registrarAveriaSolicitada(AveriaRequest request) {
        averiasSolicitadasQueue.add(request);
    }

    // Metodos de simulacion normal (igual para todas las simulaciones)
    public void iniciarSimulacion(String fechaInicio) throws Exception {
        stopSimulacion();

        running = true;
        simulationThread = new Thread(() -> {
            try {

                while (running) {
                    // Transformar la fecha a LocalDateTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                    LocalDate date = LocalDate.parse(fechaInicio, formatter);
                    LocalDateTime fechaInicioSimulacion = date.atStartOfDay();

                    // Determinar los meses para cargar archivos
                    List<YearMonth> meses = new ArrayList<>();
                    YearMonth inicio = YearMonth.from(fechaInicioSimulacion);
                    YearMonth fin = YearMonth.from(fechaInicioSimulacion.plusDays(7));
                    YearMonth actual = inicio;
                    while (!actual.isAfter(fin)) {
                        meses.add(actual);
                        actual = actual.plusMonths(1);
                    }

                    // System.out.println(archivoBloqueo);

                    // bloqueos = bloqueoService.leerBloqueos(fechaInicioSimulacion,
                    // fechaInicioSimulacion.plusDays(7));
                    // Cargar bloqueos - usar datos subidos si están disponibles, sino usar archivos
                    // estáticos
                    Map<String, String> bloqueoFiles = fileDataService.getBloqueoFilesForMonths(meses);
                    if (!bloqueoFiles.isEmpty()) {
                        bloqueos = bloqueoService.leerBloqueosFromContent(bloqueoFiles, fechaInicioSimulacion,
                                fechaInicioSimulacion.plusDays(1));
                        FileLogger.log("---->Total bloqueos cargados desde archivos subidos: " + bloqueos.size());
                    } else {
                        bloqueos = bloqueoService.leerBloqueos(fechaInicioSimulacion,
                                fechaInicioSimulacion.plusDays(1));
                    }
                    System.out.println("Total bloqueos: " + bloqueos.size());
                    for (Bloqueo bloqueo : bloqueos) {
                        bloqueo.mostrarBloqueo(); // Imprimir información de cada bloqueo
                    }
                    mapa.setBloqueos(bloqueos);

                    // Cargar plan de mantenimiento - usar datos subidos si están disponibles, sino
                    // usar archivos estáticos
                    String mantenimientoContent = fileDataService.getMantenimientoFileContent();
                    if (mantenimientoContent != null && !mantenimientoContent.trim().isEmpty()) {
                        camionesEnMantenimiento = mantenimientoService
                                .cargarMantenimientoFromContent(mantenimientoContent);
                    } else {
                        String archivoMantenimiento = Paths.get("..", "data", "PlanMantenimiento", "planM.txt")
                                .toString();
                        camionesEnMantenimiento = mantenimientoService.cargarMantenimiento(archivoMantenimiento);
                    }
                    System.out.println("Total camiones en mantenimiento: " + camionesEnMantenimiento.size());
                    camionesEnMantenimiento.forEach((camion, fechas) -> {
                        System.out.printf("Camion: %s\n", camion);
                        System.out.println("Fechas de mantenimiento: " + fechas);
                    });

                    // Cargar averias - siempre usar archivo estático (no hay controlador para subir
                    // averias)
                    /* */
                    String archivoAverias = new File("").getAbsolutePath() +
                            "/../data/averias/averias.txt";
                    averias = averiaService.leerAverias(archivoAverias);
                    System.out.println("Total averias: " + averias.size());
                    for (Averia averia : averias) {
                        System.out.printf("Avería: Turno=%s, Código=%s, Tipo=%d\n",
                                averia.getTurno(), averia.getCodigoUnidad(), averia.getTipo());
                    }

                    // Crear flota de camiones
                    flota = Camion.getListaFlotaEstatica();

                    // Crear lista de almacenes
                    almacenes = Almacen.getListaAlmacenesEstatica();
                    // Almacenes secundarios en 0 para simulación diaria
                    for (Almacen almacen : almacenes) {
                        if (!almacen.isPrincipal()) {
                            almacen.setCargaGLP(0);
                            almacen.setCargaGLP(0);
                        }
                    }

                    // Simulacion
                    this.simularDiariaWS(flota, bloqueos, camionesEnMantenimiento, ag, averias, fechaInicioSimulacion);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        simulationThread.start();
    }

    // Metodo de la simulacion
    public int simularDiariaWS(List<Camion> camiones, List<Bloqueo> bloqueos,
            Map<String, List<LocalDate>> camionesEnMantenimiento,
            GAPlanificador ag, List<Averia> averias, LocalDateTime fechaInicioSimulacion) {

        int horasHastaColapso = 0; // Variable para contar las horas hasta el colapso logístico

        // Determinar los meses para cargar pedidos
        List<YearMonth> meses = new ArrayList<>();
        YearMonth inicio = YearMonth.from(fechaInicioSimulacion);
        YearMonth fin = YearMonth.from(fechaInicioSimulacion.plusDays(7));
        YearMonth actual = inicio;
        while (!actual.isAfter(fin)) {
            meses.add(actual);
            actual = actual.plusMonths(1);
        }

        // Cargar pedidos - usar datos subidos si están disponibles, sino usar archivos
        // estáticos
        ArchivosReader archivosReader = new ArchivosReader();
        List<Pedido> pedidosTotales = new ArrayList<>();

        Map<String, String> pedidoFiles = fileDataService.getPedidoFilesForMonths(meses);
        if (!pedidoFiles.isEmpty()) {
            pedidosTotales = archivosReader.leerPedidosFromContent(pedidoFiles, fechaInicioSimulacion,
                    fechaInicioSimulacion.plusDays(1));
        }
        List<Pedido> pedidosCumplidos = new ArrayList<>(); // Lista de pedidos entregados

        long startTime = System.currentTimeMillis(); // Tiempo de inicio de simulacion

        // Resultado para enviar al cliente
        ResultadoSimulacionDTO resultadoSimulacion = new ResultadoSimulacionDTO();
        resultadoSimulacion.setTipoFin(true);
        resultadoSimulacion.setFechaFin(fechaInicioSimulacion.plusDays(1));
        resultadoSimulacion
                .setTiempoSimulado(Duration.between(fechaInicioSimulacion, fechaInicioSimulacion.plusDays(1)));

        // -----------------Simulacion--------------------
        for (LocalDateTime fecha = fechaInicioSimulacion; fecha
                .isBefore(fechaInicioSimulacion.plusDays(1)); fecha = fecha.plusMinutes(cfg.getNMinutosSimulacion())) { // Simular
                                                                                                                        // por
            // 60 minutos
            setFechaActualSimulacion(fecha);

            List<Pedido> pedidosPorHora = new ArrayList<>(); // Lista de pedidos de la ultima iteracions
            Iterator<Pedido> iterator = pedidosTotales.iterator();
            System.out.println("------------------");
            System.out.println("Fecha y hora: " + fecha.toLocalDate() + " " + fecha.getHour());

            // Drenar la cola de nuevos pedidos y agregarlos a la lista principal
            List<Pedido> nuevosPedidosEncolados = new ArrayList<>();
            nuevosPedidosQueue.drainTo(nuevosPedidosEncolados);
            if (!nuevosPedidosEncolados.isEmpty()) {
                pedidosPorHora.addAll(nuevosPedidosEncolados);
                System.out
                        .println("Se agregaron " + nuevosPedidosEncolados.size() + " nuevos pedidos a la simulación.");
                for (Pedido p : nuevosPedidosEncolados) {
                    System.out.println("Nuevo pedido: " + p.getId() + " - Volumen: " + p.getVolumen() + "m3");
                }
            }

            while (iterator.hasNext()) {
                Pedido pedido = iterator.next();
                if (pedido.getFechaPedido().isBefore(fecha)) {
                    pedidosPorHora.add(pedido); // Agregar pedido a la lista de pedidos por hora
                    // iterator.remove(); // Eliminar pedido de la lista total de manera segura
                }
            }
            for (Pedido p : pedidosPorHora) {
                System.out.println("Pedido procesado: " + p.getId() + " - Volumen: " + p.getVolumen() + "m3");
            }

            if (hayColapsoLogistico(pedidosPorHora, fecha)) {
                System.out.println("Colapso logístico detectado. Se detiene la simulación.");
                resultadoSimulacion.setFechaFin(fecha);
                resultadoSimulacion.setTipoFin(false);
                resultadoSimulacion.setTiempoSimulado(Duration.between(fechaInicioSimulacion, fecha));
                break; // Si hay colapso logístico, se detiene la simulación
            }
            horasHastaColapso++; // Incrementar contador de horas hasta el colapso logístico

            pedidosPorHora = replanificarSiPedidosGrandes(pedidosPorHora, camiones, bloqueos, mapa, fecha);

            ResultadoPlanificacion resultadoPlanificacion = new ResultadoPlanificacion();

            resultadoPlanificacion = ag.planificar(camiones, pedidosPorHora, bloqueos,
                    camionesEnMantenimiento, averias, almacenes, fecha, true); // PLANIFICACION

            // Eliminar pedidos procesados de la lista total
            final List<Pedido> pedidosPorHoraFinal = new ArrayList<>(pedidosPorHora);
            pedidosTotales.removeIf(
                    pedidoTotal -> pedidosPorHoraFinal.stream().anyMatch(
                            p -> p.getId().equals(pedidoTotal.getId()) &&
                                    p.getChildId() == pedidoTotal.getChildId()));

            // Agregar pedidos no asignados a la lista total
            System.out.println("Pedidos procesados: " + pedidosPorHora.size());
            if (!resultadoPlanificacion.getPedidosNoAtendidos().isEmpty()) {
                System.out.println("Cantidad de pedidos no asignados siendo reañadidos a la cola de pedidos: "
                        + resultadoPlanificacion.getPedidosNoAtendidos().size());
                for (Pedido p : resultadoPlanificacion.getPedidosNoAtendidos()) {
                    System.out.println("Pedido no asignado: " + p.getId() + " - " + p.getVolumen() + "m3");
                }
                pedidosTotales.addAll(new ArrayList<>(resultadoPlanificacion.getPedidosNoAtendidos()));
            }

            // Se mueven los camiones
            List<Pedido> pedidosEntregados = moverCamiones(camiones, fecha, ag, bloqueos, almacenes,
                    resultadoPlanificacion.getPedidosNoAtendidos()); // Mover camiones y
            // Si el camión esta en un depósito vacío, sale de él a otro que tenga capacidad
            for (Almacen a : almacenes) {
                a.sacarCamionesDeAlmacen(almacenes, mapa, bloqueos,
                        fecha);
            }
            // obtener
            // pedidos entregados
            if (!pedidosEntregados.isEmpty())
                pedidosCumplidos.addAll(pedidosEntregados); // Agregar pedidos entregados a la lista de pedidos
                                                            // cumplidos

            System.out.println("------------------");
        }

        long endTime = System.currentTimeMillis(); // Tiempo de fin de simulacion

        System.out.println("-------------------");
        System.out.println("Simulación finalizada.");
        System.out.println("-------------------");
        System.out.println("Tiempo de simulación: " + (endTime - startTime) + " ms");
        pedidosCumplidos = filtrarOrdenarPedidosCumplidos(pedidosCumplidos); // Filtrar y ordenar pedidos cumplidos
        for (Pedido pedido : pedidosCumplidos) {
            System.out.println("Pedido cumplido: " + pedido.getId());
        }
        int debugCumplidos = 1;
        for (Pedido pedido : pedidosCumplidos) {
            if (debugCumplidos != pedido.getId()) {
                System.out.println("Falta cumplir el pedido: " + debugCumplidos);
                debugCumplidos = pedido.getId() + 1;
            } else {
                debugCumplidos++;
            }
        }
        System.out.println("Pedidos cumplidos: " + pedidosCumplidos.size());

        // Enviar el resultado de simulacion al cliente
        resultadoSimulacion.setCantidadPedidosEntregados(pedidosCumplidos.size());
        messagingTemplate.convertAndSend("/topic/diaria", resultadoSimulacion);

        return horasHastaColapso; // Retornar horas hasta el colapso logístico
    }
    // ------------------Fin Simulacion--------------------

    public List<Pedido> moverCamiones(List<Camion> camiones, LocalDateTime fechaActual, GAPlanificador ag,
            List<Bloqueo> bloqueos, List<Almacen> almacenes, List<Pedido> noAsignados) {

        System.out.println("Moviendo camiones...");
        List<Pedido> pedidosEntregadosTotal = new ArrayList<>(); // Lista de pedidos a entregados
        List<Pedido> pedidosTrasvasadosTotal = new ArrayList<>(); // Lista de pedidos trasvasados

        // Guardamos en variables para evitar calcular a cada rato
        int iteracionesTotales = cfg.getNIteracionesMovimientoCamiones(); // Total de iteraciones de movimiento de
                                                                          // camiones
        // int unMinutoSimulacion = cfg.getUnMinutoSimulacion(); // Iteraciones para
        // simular un minuto
        int unKilometroMovimientoSimulacion = cfg.getUnKilometroMovimientoSimulacion(); // Iteraciones para simular un
                                                                                        // kilómetro de movimiento
        int unMinutoSimulacion = cfg.getUnMinutoSimulacion(); // Iteraciones para simular un minuto

        // Eventos para enviar
        List<LogEventoDTO> eventosPorEnviar = new ArrayList<>();

        // durante 60 minutos de simulación, cada iteración representa un minuto
        // en este minuto todos los camiones se mueven
        for (int i = 1; i <= iteracionesTotales; i++) {
            Iterator<Camion> iterator = camiones.iterator();
            while (iterator.hasNext()) {

                Camion camion = iterator.next();
                /*
                 * if (!camion.getListaPedidos().isEmpty()) {
                 * System.out.println("Camion esta en ruta ? " + camion.isEnRuta()+
                 * camion.getTiempoSalida());
                 * }
                 */

                // PRIMICIA
                // los camiones empiezan a moverse
                // recordamos que hay averias definidas para ciertos camiones
                // entonces cuando el camion llegue a ese nodo averiado
                // este va a ser inmovilizado

                if (i % unMinutoSimulacion == 0) {

                    // Logica de averias para simulación diaria
                    AveriaRequest request;
                    while ((request = averiasSolicitadasQueue.poll()) != null) {
                        for (Camion c : camiones) {
                            if (c.getCodigo().equals(request.getCodigo()) && c.isEnRuta()) {
                                // Create and assign the Averia object
                                Averia averia = new Averia("0", c.getCodigo(),
                                        Integer.parseInt(request.getTipoAveria()));
                                averia.setTurnoDiario(fechaActual.plusMinutes(i / unMinutoSimulacion));
                                averia.setTipo(Integer.parseInt(request.getTipoAveria()));
                                averia.setNodoAveria(c.getRutaVehiculo().getFirst());
                                c.setAveria(averia);
                                System.out.println("Camión " + c.getCodigo() + " ha solicitado una avería de tipo "
                                        + request.getTipoAveria());
                            }
                        }
                    }

                    // ha pasado 1 minuto de simulacion
                    if (camion.isInmovilizado()) {
                        // Si el camión está inmovilizado, se verifica si ya está disponible (despues de
                        // 1 minuto)
                        double tiempoRestante = camion.getAveria().getTiempoInamovilidad() - 1;
                        camion.getAveria().setTiempoInamovilidad(tiempoRestante);
                        System.out.println("Camión " + camion.getCodigo() + " está inmovilizado. Tiempo restante: "
                                + tiempoRestante + " minutos.");
                        if (tiempoRestante <= 0) {
                            camion.setInmovilizado(false); // El camión ya no está inmovilizado
                            camion.regresarAlDeposito(mapa, bloqueos, fechaActual, almacenes);
                            // No eliminamos la avería, porque el camion aun tiene un tiempo de
                            // indisponibilidad por esta
                        } else {
                            continue; // Si el camión sigue inmovilizado, no se mueve
                        }
                    }

                    if (camion.isAveriado()) {
                        // Si el camión está averiado, se verifica si ya está disponible (despues de 1
                        // minuto)
                        double tiempoAveriadoRestante = camion.getTiempo_averiado() - 1;
                        System.out.println("Camión " + camion.getCodigo() + " está averiado. Tiempo restante: "
                                + camion.getTiempo_averiado() + " minutos.");
                        if (tiempoAveriadoRestante <= 0) {
                            camion.setAveriado(false); // El camión ya no está averiado
                            camion.setTiempo_averiado(0); // Reiniciar el tiempo de avería
                            camion.setAveria(null); // Eliminar la avería asociada
                        } else {
                            continue; // Si el camión sigue averiado, no se mueve
                        }
                    }

                    if (camion.isDespachando()) {
                        // Si el camión está despachando, se verifica si ya terminó el tiempo de
                        // despacho (1 minuto)
                        if (camion.getTiempoDespacho() > 0) {
                            // En caso se siga despachando, se disminuye el tiempo de despacho
                            camion.setTiempoDespacho(camion.getTiempoDespacho() - 1);
                        } else {
                            if (camion.getTiempoDespacho() == 0) {
                                // Si el tiempo de despacho es 0, se considera que el camión ya no está
                                // despachando
                                camion.setDespachando(false);
                            }
                        }
                    }

                    if (camion.isSaliendo()) {
                        // Si el camión está saliendo, se verifica si ya terminó el tiempo de salida
                        if (camion.getTiempoSalida() > 0) {
                            // En caso se siga saliendo, se disminuye el tiempo de salida
                            camion.setTiempoSalida(camion.getTiempoSalida() - 1);
                        } else {
                            // Si el tiempo de salida es 0, se considera que el camión ya no está saliendo
                            camion.setSaliendo(false);
                            camion.setEnRuta(true); // El camión ya está en ruta
                            camion.ubicarInicioRuta(); // Ubicar el camión en el inicio de su ruta
                            // System.out.println(camion.getTiempoSalida());
                            System.out.println("Camión " + camion.getCodigo() + " ha salido del depósito.");
                        }
                    }

                    if (camion.isTrasvasando()) { // recogiendo el pedido de otro camion
                        // Si el camión está trasvasando, se verifica si ya terminó el tiempo de
                        // trasvase
                        if (camion.getTiempoTrasvase() > 0) {
                            // En caso se siga trasvasando, se disminuye el tiempo de trasvase
                            camion.setTiempoTrasvase(camion.getTiempoTrasvase() - 1);
                        } else {
                            // Si el tiempo de trasvase es 0, se considera que el camión ya no está
                            // trasvasando
                            camion.setTrasvasando(false);
                        }
                    }
                }

                // movimiento
                if (i % unKilometroMovimientoSimulacion == 0) { // Un kilómetro recorrido

                    if (camion.isEnRuta()) {
                        if (camion.getNodoActual().getDestino() == 1) { // el camion esta en una ubicacion de entrega o
                                                                        // de despacho

                            // 1. Entrega o trasvase de pedidos propios (no deberia entrar nunca a trasvase
                            // ya que esos estan definidos en el atributo replanificacion)
                            // Filtrar los pedidos que están en la ubicación actual del camión
                            List<Pedido> pedidosUbic = camion.getListaPedidos().stream()
                                    .filter(p -> p.getX() == camion.getNodoActual().getX()
                                            && p.getY() == camion.getNodoActual().getY())
                                    .collect(Collectors.toList());

                            for (Pedido pedido : pedidosUbic) {
                                // Verificar si el pedido se puede entregar o trasvasar
                                if (pedido.isSeTrasvasa()) {

                                    // Como se tiene un pedido que se va a trasvasar, buscamos que camion
                                    // inmovilizado lo tiene
                                    Camion camionInmovilizado = camionesXRecogerPedidos.stream()
                                            .filter(c -> c.getNodoActual().equals(camion.getNodoActual()))
                                            .findFirst()
                                            .orElse(null);
                                    camion.verificarTrasvasePedido(camionInmovilizado);

                                    // Si el camion inmovilizado ya no tiene pedidos, se elimina de la lista
                                    if (camionInmovilizado.getListaPedidos().size() > 0)
                                        camionesXRecogerPedidos.remove(camionInmovilizado);

                                } else {

                                    camion.entregarPedido(pedido);
                                    LogEventoDTO evento = new LogEventoDTO();
                                    evento.convertirPedidoEntregadoEnEvento(pedido, camion.getCodigo());
                                    eventosPorEnviar.add(evento);
                                    pedidosEntregadosTotal.add(pedido); // Agregar pedido entregado a la lista
                                }

                            }

                            // 2. Recojo de pedidos trasvasados (nueva lógica)
                            if (camion.getReplanificacion() != null
                                    && camion.getReplanificacion().getCamionPedidosTrasvase() != null) {

                                Camion camionInmovilizado = camion.getReplanificacion().getCamionPedidosTrasvase()
                                        .keySet().stream()
                                        .filter(c -> c.getNodoActual().equals(camion.getNodoActual()))
                                        .findFirst()
                                        .orElse(null);

                                if (camionInmovilizado != null) {
                                    // Realizar el trasvase usando la lógica centralizada
                                    camion.verificarTrasvasePedido(camionInmovilizado);
                                    // Si el camión inmovilizado ya no tiene pedidos, se elimina de la lista global
                                    if (camionInmovilizado.getListaPedidos().isEmpty()) {
                                        camionesXRecogerPedidos.remove(camionInmovilizado);
                                    }
                                }
                            }
                        }
                    }

                    // camion puede moverse
                    if (camion.isEnRuta() && !camion.isDespachando() && !camion.isSaliendo()
                            && !camion.isTrasvasando()) {
                        // Solo considerar el depósito central de momento
                        if (!camion.getRutaVehiculo().isEmpty()) {

                            // primero avanzamos el camion
                            if (!camion.isInmovilizado()) {
                                camion.avanzarNodo(); // Avanzar al siguiente nodo de la ruta
                                // System.out.println("Camión " + camion.getCodigo() + " se mueve al nodo: "
                                // + camion.getNodoActual().getX() + ", " + camion.getNodoActual().getY());
                            }

                            // 1. Si llega al depósito final, ya no se encuentra en ruta
                            if (camion.estaEnDeposito(mapa) && camion.getRutaVehiculo().size() == 0) {
                                camion.setEnRuta(false); // camión ha llegado a su destino
                                for (Almacen almacen : almacenes) {
                                    almacen.actualizarCamionesEnAlmacen(camiones);
                                }
                                continue;
                            }

                            if (camion.verificarTrasvase()) {
                                continue;
                            }
                            // System.out.println(
                            // "=======================================================================CHEQUEO
                            // DE AVERIA
                            // =======================================================================");
                            // System.out.println(camion.getCodigo() + " se encuentra en el nodo: ");
                            // System.out.println(camion.getNodoActual().getX() + ", "
                            // + camion.getNodoActual().getY());

                            // 2. Si ha llegado a una averia o a su destino
                            if (camion.getAveria() != null &&
                                    (camion.getAveria().getNodoAveria().getX() == camion.getNodoActual().getX() &&
                                            camion.getAveria().getNodoAveria().getY() == camion.getNodoActual().getY())
                                    && !camion.isInmovilizado()) {

                                // En este punto el camión llega a un nodo de avería
                                // Se inmoviliza solo si es que el turno de la averia coincide con el turno
                                // actual
                                // depende de la fecha en la que nos encontramos
                                System.out.println(fechaActual);
                                if (camion.sufreAveria(fechaActual.plusMinutes(i / unMinutoSimulacion))) {
                                    System.out.println(
                                            "=======================================================================DEBERIA AVERIARSE ACA=======================================================================");

                                    camion.setInmovilizado(true);
                                    camion.setAveriado(true);
                                    camionesXRecogerPedidos.add(camion);

                                    camion.getAveria()
                                            .setFechaIncidente(fechaActual.plusMinutes(i / unMinutoSimulacion)); // Establecer
                                                                                                                 // la
                                                                                                                 // fecha
                                                                                                                 // del
                                                                                                                 // incidente
                                    camion.procesarAveria(); // Procesar la avería del camión
                                    System.out.println(
                                            camion.getAveria().getTiempoInamovilidad() + "," + camion.isInmovilizado());

                                    // Enviar evento de averia
                                    LogEventoDTO evento = new LogEventoDTO();
                                    evento.convertirAveriaEnEvento(camion.getCodigo(), camion.getAveria().getTipo(),
                                            camion.getAveria().getNodoAveria().getX(),
                                            camion.getAveria().getNodoAveria().getY());
                                    eventosPorEnviar.add(evento);
                                    System.out.println(
                                            "********************************************Averia Enviada********************************************");
                                }
                            }

                        }

                        // Si esta averiado replanificar. Se pone aca para que cheque a cada rato.
                        if (camion.isInmovilizado() && !camion.getListaPedidos().isEmpty()) {
                            ag.replanificar(camiones, camion, fechaActual.plusMinutes(i / unMinutoSimulacion), bloqueos,
                                    almacenes, camionesEnMantenimiento);
                        }
                    }
                }
                /*
                 * System.out.println("Camión " + camion.getCodigo() +
                 * " se encuentra en el nodo: "
                 * + camion.getNodoActual().getX() + ", " + camion.getNodoActual().getY());
                 */
            }
            if (i % unMinutoSimulacion == 0) {
                // Actualizar almacenes al inicio del día
                // Obtener bloqueos actuales para enviar al cliente
                List<Bloqueo> bloqueosActuales = new ArrayList<>();
                for (Bloqueo bloqueo : bloqueos) {
                    if (bloqueo.isActual(fechaActual.plusMinutes(i / unMinutoSimulacion))) {
                        bloqueosActuales.add(bloqueo); // Agregar bloqueos actuales a la lista
                    }
                }

                // Detectar nuevos bloqueos y crear eventos
                for (Bloqueo bloqueoActual : bloqueosActuales) {
                    // Verificar si este bloqueo es nuevo (no estaba en la lista previa)
                    boolean esNuevo = bloqueosPrevios.stream()
                            .noneMatch(previo -> previo.getFechaInicio().equals(bloqueoActual.getFechaInicio()) &&
                                    previo.getFechaFin().equals(bloqueoActual.getFechaFin()) &&
                                    previo.getTramo().equals(bloqueoActual.getTramo()));

                    if (esNuevo) {
                        // Crear evento para bloqueo nuevo
                        LogEventoDTO evento = new LogEventoDTO();

                        // Obtener el primer y último nodo del tramo para mostrar en el mensaje
                        if (!bloqueoActual.getTramo().isEmpty()) {
                            Nodo primerNodo = bloqueoActual.getTramo().get(0);
                            Nodo ultimoNodo = bloqueoActual.getTramo().get(bloqueoActual.getTramo().size() - 1);

                            evento.convertirBloqueoEnEvento(
                                    primerNodo.getX(), primerNodo.getY(),
                                    ultimoNodo.getX(), ultimoNodo.getY());
                            eventosPorEnviar.add(evento);
                            System.out.println("Nuevo bloqueo detectado: desde (" + primerNodo.getX() +
                                    "," + primerNodo.getY() + ") hasta (" + ultimoNodo.getX() +
                                    "," + ultimoNodo.getY() + ")");
                        }
                    }
                }

                // Actualizar la lista de bloqueos previos
                bloqueosPrevios.clear();
                bloqueosPrevios.addAll(bloqueosActuales);
                // Obtener pedidos por hora para enviar al cliente
                List<Pedido> pedidosPorHora = new ArrayList<>();
                for (Camion camion : camiones) {
                    for (Pedido pedido : camion.getListaPedidos()) {
                        pedidosPorHora.add(pedido); // Agregar pedidos por hora a la lista
                    }
                }
                // Agregar pedidos no asignados a la lista para mostrar al cliente. Sino los
                // pedidos no asignados no se ven
                pedidosPorHora.addAll(noAsignados);

                this.verificarActualizarAlmacenes(almacenes, camiones, fechaActual.plusMinutes(i / unMinutoSimulacion));

                SnapshotDTO snapshot = new SnapshotDTO();
                snapshot.setFechaHoraActual(fechaActual.plusMinutes(i / unMinutoSimulacion));
                snapshot.convertirSnapshotToDTO(camiones, pedidosPorHora, bloqueosActuales, almacenes);
                snapshot.setEventos(eventosPorEnviar);

                // Enviar snapshot por WebSocket
                messagingTemplate.convertAndSend("/topic/diaria", snapshot);

                eventosPorEnviar.clear();

                try {
                    simulationLock.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("La simulación fue interrumpida.");
                    return pedidosEntregadosTotal;
                }

            }
        }

        // Retornar la lista de pedidos entregados
        return pedidosEntregadosTotal;

    }

    public boolean hayColapsoLogistico(List<Pedido> pedidos, LocalDateTime tiempoActual) {
        for (Pedido p : pedidos) {
            if (p.getFechaLimiteEntrega().isBefore(tiempoActual)) {
                System.out.println("Colapso logístico detectado: Pedido " + p.getId() + "-" + p.getChildId()
                        + " no entregado a tiempo.");
                return true; // Retornar true si hay colapso logístico, false en caso contrario
            }
        }
        return false; // Retornar true si hay colapso logístico, false en caso contrario
    }

    // Actualizar almacenes al inicio del día y actualizar los camiones
    public void verificarActualizarAlmacenes(List<Almacen> almacenes, List<Camion> camiones,
            LocalDateTime fechaActual) {
        for (Almacen almacen : almacenes) {
            if (!almacen.isPrincipal()) {
                if (fechaActual.toLocalTime().equals(LocalTime.MIDNIGHT)) {
                    almacen.setCargaGLP(160);
                }
            }
            almacen.actualizarCamionesEnAlmacen(camiones); // Actualizar camiones en el almacén
        }
    }

    public List<Pedido> filtrarOrdenarPedidosCumplidos(List<Pedido> pedidosCumplidos) {
        List<Pedido> pedidosFinales = new ArrayList<>(
                pedidosCumplidos.stream()
                        .collect(Collectors.toMap(
                                Pedido::getId,
                                Function.identity(),
                                (p1, p2) -> p1))
                        .values());
        pedidosFinales.sort(Comparator.comparingInt(Pedido::getId));
        return pedidosFinales;
    }

    public List<Pedido> replanificarSiPedidosGrandes(List<Pedido> pedidosNuevos, List<Camion> camiones,
            List<Bloqueo> bloqueos,
            Mapa mapa, LocalDateTime fecha) {
        List<Pedido> pedidosTotalesProcesados = new ArrayList<>();
        List<Pedido> pedidosReplanificar = new ArrayList<>(pedidosNuevos);
        int capacidadTotal = 0;
        boolean hayCritico = false; // Variable para verificar si hay pedidos críticos

        // Extraer pedidos asignados en camiones
        for (Camion c : camiones) {
            if (!c.getListaPedidos().isEmpty())
                pedidosTotalesProcesados.addAll(c.getListaPedidos());
        }
        pedidosTotalesProcesados.addAll(pedidosNuevos);
        for (Pedido p : pedidosTotalesProcesados) {
            capacidadTotal += p.getVolumen();
            if (p.isCritico(fecha) && p.getCamionAsignado() == null) {
                hayCritico = true; // Si hay al menos un pedido crítico sin asignar
            }
        }
        if (capacidadTotal > 200 && hayCritico) {
            System.out.println("Replanificando por pedidos grandes, volumen total: " + capacidadTotal);
            Set<String> pedidoKeys = pedidosReplanificar.stream()
                    .map(p -> p.getId() + "-" + p.getChildId())
                    .collect(Collectors.toSet());

            for (Camion c : camiones) {
                List<Pedido> pedidosCamion = c.replanificarRuta(mapa, bloqueos, almacenes, fecha);
                for (Pedido p : pedidosCamion) {
                    String key = p.getId() + "-" + p.getChildId();
                    if (!pedidoKeys.contains(key)) {
                        pedidosReplanificar.add(p);
                        pedidoKeys.add(key);
                    }
                }
            }
        }

        return pedidosReplanificar; // Retornar la lista de pedidos para la replanificación
    }

    public List<Pedido> analizarMantenimiento(List<Camion> camiones,
            Map<String, List<LocalDate>> camionesEnMantenimiento,
            LocalDateTime fecha, Mapa mapa, List<Bloqueo> bloqueos, List<Almacen> almacenes) {
        // Este metodo me devolverá la lista de pedidos que deben regresar al almacén ya
        // que son de los camiones que estan en mantenimiento
        List<Pedido> pedidosARegresar = new ArrayList<>();
        for (Camion camion : camiones) {

            if (camionesEnMantenimiento.containsKey(camion.getCodigo())
                    && camionesEnMantenimiento.get(camion.getCodigo()).contains(fecha.toLocalDate())) {
                camion.setEn_mantenimiento(true);
                pedidosARegresar.addAll(camion.getListaPedidos()); // Agregar pedidos del camión a la lista de pedidos a
                                                                   // regresar
                camion.setListaPedidos(new ArrayList<>()); // Limpiar la lista de pedidos del camión
                camion.setCargaGLP(0);
                camion.regresarAlDeposito(mapa, bloqueos, fecha, almacenes); // Regresar al depósito
                System.out.println("Camión " + camion.getCodigo() + " está en mantenimiento. Regresando al depósito.");
            } else {
                camion.setEn_mantenimiento(false); // Si no está en mantenimiento, se asegura de que el camión no esté
                                                   // marcado como en mantenimiento
            }
        }
        return pedidosARegresar;
    }
}
