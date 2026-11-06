// GAPlanificador.java
package edu.pucp.plg_back.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import edu.pucp.plg_back.model.Almacen;
import edu.pucp.plg_back.model.Averia;
import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Mapa;
import edu.pucp.plg_back.model.Nodo;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Resultado;
import edu.pucp.plg_back.model.ResultadoPlanificacion;
import edu.pucp.plg_back.model.ResultadoReplanificacion;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.Planificador;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GAPlanificador implements Planificador {

    private final GAParams cfg = GAParams.builder().build();
    private final Random rnd = new Random();
    private final Pathfinder pathfinder = new Pathfinder();
    // private final double DEADLINE_PENALTY_FACTOR = 10.0;

    // asumimos igualmente que el mapa es proveido o creado en otra parte
    private final Mapa mapa = new Mapa();

    @Override
    public void replanificar(List<Camion> camiones, Camion camionInmovilizado,
            LocalDateTime fechaHoraActual, List<Bloqueo> bloqueos, List<Almacen> almacenes,
            Map<String, List<LocalDate>> camionesEnMantenimiento) {

        /*
         * En esta funcion se asigna la replanificación a los camiones que están en ruta
         * o están en el depósito
         * A los pedidos del camion inmovilizado se les asigna el atributo seTrasvasa =
         * true
         * en caso dicho pedido tenga que ser recogido (trasvasado) por otro camión
         */

        // 1. Filtrar todos los camiones disponibles (sin los inmovilizados)
        List<Camion> camionesDisponibles = camiones.stream()
                .filter(camion -> {
                    String codigoCamion = camion.getCodigo();
                    // Si el camión no está en mantenimiento o la fecha de mantenimiento es
                    // posterior a la actual
                    /*
                     * if (camionesEnMantenimiento.containsKey(codigoCamion)) {
                     * boolean en_mantenimiento =
                     * camionesEnMantenimiento.get(codigoCamion).contains(fechaHoraActual.
                     * toLocalDate());
                     * camion.setEn_mantenimiento(en_mantenimiento);
                     * }
                     */
                    return !camion.isSaliendo() && !camion.isEn_mantenimiento()
                            && !camion.isAveriado();
                })
                .collect(Collectors.toList());
        /*
         * List<Camion> camionesDisponibles = camiones.stream()
         * .filter(c -> c.isEnRuta() && !c.isInmovilizado() && !c.isEn_mantenimiento()
         * && !c.isAveriado())
         * .collect(Collectors.toList());
         */
        // 2. Llamar al método planificar con los camiones disponibles y los pedidos
        // inmovilizados
        planificarRecoleccion(camionesDisponibles, camionInmovilizado, bloqueos, almacenes, fechaHoraActual);
    }

    /** Planifica rutas para recoger pedidos de camiones inmovilizados */
    public void planificarRecoleccion(List<Camion> camiones, Camion camionInmovilizado,
            List<Bloqueo> bloqueos, List<Almacen> almacenes, LocalDateTime fechaHoraActual) {

        List<Pedido> nuevosPedidos = new ArrayList<>(camionInmovilizado.getListaPedidos());
        // List<Pedido> pedidosNoAsignados = new ArrayList<>();

        Resultado resultadoReplanificacion = asignarPedidos(camiones, nuevosPedidos, almacenes, fechaHoraActual);
        // Sacamos los pedidos asignados del camion inmovilizado
        if (!resultadoReplanificacion.getPedidosEnQeue().isEmpty()) {
            List<Pedido> pedidosPorSacar = camionInmovilizado.getListaPedidos();
            for (Pedido p : resultadoReplanificacion.getPedidosEnQeue()) {
                pedidosPorSacar.remove(p);
                camionInmovilizado.setCargaGLP(camionInmovilizado.getCargaGLP() - p.getVolumen());
            }
            camionInmovilizado.setListaPedidos(pedidosPorSacar);
            if (camionInmovilizado.getListaPedidos().isEmpty())
                System.out.println(
                        "Se asignaron todos los pedidos del camion averiado: " + camionInmovilizado.getCodigo());
        }

        // ASIGNAMOS LAS RUTAS A LOS CAMIONES
        List<Ruta> rutas = new ArrayList<>();
        resultadoReplanificacion.getCamionesPedidos().forEach((c, lista) -> {
            if (!lista.isEmpty()) {
                Ruta r = evolucionar(c, lista, bloqueos, fechaHoraActual);
                // System.err.println("Error en la planificacion:" + r);
                if (r != null) {
                    // Se añade el nodo de trasvase
                    List<Nodo> ruta = r.getRuta();
                    ruta.add(1, camionInmovilizado.getNodoActual());
                    r.setRuta(ruta);
                    c.setNodoTrasvase(camionInmovilizado.getNodoActual());
                    // El resto de logica de asignacion de rutas
                    rutas.add(r);
                    c.setListaPedidos(lista); // asignar la lista de pedidos al camión
                    c.setCantidadPedidos(lista.size());
                    actualizarCamionesEnAlmacen(almacenes, c);
                    c.setRutaVehiculoNodos(r);
                    c.agregarDepositoFinal(almacenes); // agrega al ultimo nodo de la ruta el depósito más cercano
                    c.generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual); // completar la ruta (todos los nodos
                                                                                  // que va a recorrer)
                    if (c.getRutaVehiculo().isEmpty()) {
                        resultadoReplanificacion.getPedidosNoAtendidos().addAll(lista);
                        c.getListaPedidos().clear(); // limpiar la lista de pedidos del camión
                    } else {
                        c.setListaPedidos(lista);
                        // Cuando el CP ya asignó las rutas se generan las averías para ciertas unidades
                        // en un punto aleatorio de la ruta entre 5% a 35%

                        // De momento que no se averie si es trasvase xd
                        // asignarPuntoAveriaEnRuta(c, averias, fechaHoraActual);
                        c.setSaliendo(true);
                        c.setTiempoSalida(15);
                    }

                } else {
                    System.err.printf("No se pudo REPLANIFICAR la ruta para el camión %s del camion inmovilizado %s\n",
                            c.getCodigo(), camionInmovilizado.getCodigo());
                }
            }
        });
        /*
         * List<Pedido> nuevosPedidos = new
         * ArrayList<>(camionInmovilizado.getListaPedidos());
         * // List<Pedido> pedidosNoAsignados = new ArrayList<>();
         * 
         * // se ordenan los camiones de acuerdo a la cercania con el camion averiado
         * List<Camion> camionesOrdenados =
         * camiones.stream().sorted(Comparator.comparingDouble(
         * c ->
         * c.getNodoActual().getDistancia(camionInmovilizado.getNodoActual()))).collect(
         * Collectors.toList());
         * 
         * for (Camion camion : camionesOrdenados) {
         * 
         * double espacioDisponible = camion.getCapacidad() - camion.getCargaGLP();
         * if (espacioDisponible <= 0)
         * continue;
         * 
         * // Intenta asignar la mayor cantidad de pedidos posibles a este camión
         * Iterator<Pedido> it = nuevosPedidos.iterator();
         * List<Pedido> pedidosAsignados = new ArrayList<>(); // aca almacenamos los
         * posibles pedidos que se asignaran al camion para el trasvase
         * List<Pedido> pedidosAEliminar = new ArrayList<>();
         * while (it.hasNext()) {
         * Pedido pedido = it.next();
         * if (pedido.getVolumen() <= espacioDisponible) {
         * pedido.setSeTrasvasa(true);
         * pedidosAsignados.add(pedido);
         * espacioDisponible -= pedido.getVolumen();
         * pedidosAEliminar.add(pedido); // <-- Solo marcar para eliminar
         * }
         * }
         * 
         * if (!pedidosAsignados.isEmpty()) {
         * 
         * // No modificar la lista de pedidos del camión aquí
         * // Crear una nueva lista de pedidos que incluye los pedidos del camión y los
         * nuevos pedidos asignados
         * List<Pedido> nuevaLista = new ArrayList<>(camion.getListaPedidos());
         * nuevaLista.addAll(pedidosAsignados);
         * 
         * // Recalcula la ruta del camión con los nuevos pedidos
         * Ruta nuevaRuta = evolucionar(camion, nuevaLista, bloqueos, fechaHoraActual);
         * if (nuevaRuta != null) {
         * camion.setRutaVehiculoNodos(nuevaRuta);
         * camion.agregarDepositoFinal(almacenes);
         * camion.generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual);
         * 
         * // ahora vamos a insertar o actualizar la replanificacion del camion
         * ResultadoReplanificacion replanif = camion.getReplanificacion();
         * if (replanif == null) {
         * replanif = new ResultadoReplanificacion();
         * Map<Camion, List<Pedido>> mapNuevo = new HashMap<>();
         * mapNuevo.put(camionInmovilizado, new ArrayList<>(pedidosAsignados));
         * replanif.setCamionPedidosTrasvase(new HashMap<>(mapNuevo));
         * camion.setReplanificacion(replanif);
         * } else {
         * Map<Camion, List<Pedido>> mapAux = replanif.getCamionPedidosTrasvase();
         * mapAux.putIfAbsent(camionInmovilizado, new ArrayList<>());
         * mapAux.get(camionInmovilizado).addAll(pedidosAsignados);
         * replanif.setCamionPedidosTrasvase(mapAux);
         * camion.setReplanificacion(replanif);
         * }
         * 
         * nuevosPedidos.removeAll(pedidosAEliminar); // eliminar los pedidos que ya
         * fueron asignados
         * }
         * }
         * }
         */

    }

    @Override
    public ResultadoPlanificacion planificar(List<Camion> camiones, List<Pedido> pedidos, List<Bloqueo> bloqueos,
            Map<String, List<LocalDate>> camionesEnMantenimiento, List<Averia> averias, List<Almacen> almacenes,
            LocalDateTime fechaHoraActual, boolean isDiario) {

        mapa.setBloqueos(bloqueos);
        System.out.printf("Planificando %d camiones\n", camiones.size());
        // Establecemos un arreglo de camiones que estan en mantenimiento y estan
        // regresando al almacen para cumplirlo
        List<Camion> camionesRegresando = new ArrayList<>();

        // Con los bloqueos, filtramos los camiones disponibles
        camiones = filtrarCamionesDisponibles(camiones, camionesEnMantenimiento, camionesRegresando, fechaHoraActual);
        System.out.printf("Planificando %d camiones filtrados\n", camiones.size());

        // ASIGNAMOS PEDIDOS AL CAMION
        Resultado buckets = asignarPedidos(camiones, pedidos, almacenes, fechaHoraActual);

        // agregamos los pedidos pendientes en caso el camion regrese al almacen por
        // mantenimiento
        for (Camion c : camionesRegresando) {
            if (c.getListaPedidos() != null && !c.getListaPedidos().isEmpty()) {
                buckets.getPedidosNoAtendidos().addAll(c.getListaPedidos());
                c.getListaPedidos().clear(); // Limpia los pedidos del camión, ya que regresan a la cola
            }
        }
        // agregamos los camiones que regresan al almacen
        camiones.addAll(camionesRegresando);

        ResultadoPlanificacion resultadoFinal = new ResultadoPlanificacion();

        // ASIGNAMOS LAS RUTAS A LOS CAMIONES
        List<Ruta> rutas = new ArrayList<>();
        buckets.getCamionesPedidos().forEach((c, lista) -> {
            if (!lista.isEmpty()) {
                Ruta r = evolucionar(c, lista, bloqueos, fechaHoraActual);
                // System.err.println("Error en la planificacion:" + r);
                if (r != null) {
                    rutas.add(r);
                    c.setListaPedidos(lista); // asignar la lista de pedidos al camión
                    c.setCantidadPedidos(lista.size());
                    actualizarCamionesEnAlmacen(almacenes, c);
                    c.setRutaVehiculo(new ArrayList<>());
                    c.setRutaVehiculoNodos(r);
                    c.agregarDepositoFinal(almacenes); // agrega al ultimo nodo de la ruta el depósito más cercano
                    c.generarCaminosIntermedios(mapa, bloqueos, fechaHoraActual); // completar la ruta (todos los nodos
                                                                                  // que va a recorrer)
                    if (c.getRutaVehiculo().isEmpty()) {
                        buckets.getPedidosNoAtendidos().addAll(lista);
                        c.getListaPedidos().clear(); // limpiar la lista de pedidos del camión
                    } else {
                        c.setListaPedidos(lista);
                        // Cuando el CP ya asignó las rutas se generan las averías para ciertas unidades
                        // en un punto aleatorio de la ruta entre 5% a 35%
                        if (!isDiario) {
                            asignarPuntoAveriaEnRuta(c, averias, fechaHoraActual);
                        }
                        // Si el camión no está en replanificación, se marca como saliendo
                        if (!c.isReplanificar()) {
                            c.setSaliendo(true);
                            c.setTiempoSalida(15);
                        }
                        c.setReplanificar(false); // Resetear el flag de replanificación

                    }

                } else
                    System.err.printf("No se pudo planificar la ruta para el camión %s\n", c.getCodigo());
            }
        });

        rutas.sort(Comparator.naturalOrder());
        resultadoFinal.setPedidosEnQueue(buckets.getPedidosEnQeue());
        resultadoFinal.setPedidosNoAtendidos(buckets.getPedidosNoAtendidos());
        resultadoFinal.setRutas(rutas);

        return resultadoFinal; // devolver pedidos no asignados
    }

    /* ---------- GA-TSP para un único camión ---------- */
    private Ruta evolucionar(Camion camion, List<Pedido> pedidos, List<Bloqueo> bloqueos,
            LocalDateTime fechaHoraActual) {
        Nodo deposito = camion.getNodoActual(); // depósito central o ubicación del camión(movimiento)

        // Elaboramos un map con los nodos destino
        // Cada nodo puede tener una lista de pedidos en su posición
        Map<Nodo, List<Pedido>> pedidosPorDestino = new HashMap<>();
        for (Pedido p : pedidos) {
            Nodo destino = new Nodo(p.getX(), p.getY());
            pedidosPorDestino.computeIfAbsent(destino, k -> new ArrayList<>()).add(p);
        }
        List<Nodo> destinosUnicos = new ArrayList<>(pedidosPorDestino.keySet());

        int n = destinosUnicos.size() + 1; // 0 = depósito
        if (n <= 1)
            return null; // no hay pedidos

        List<Nodo> nodos = new ArrayList<>();
        nodos.add(deposito);
        nodos.addAll(destinosUnicos);

        double[][] dist = new double[n][n];
        System.out.printf("Calculando A* distancias para los nodos del camión %s...\n", camion.getCodigo());
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    continue;
                }
                Nodo from = nodos.get(i);
                Nodo to = nodos.get(j);
                // resetear los nodos
                from = new Nodo(from.getX(), from.getY());
                to = new Nodo(to.getX(), to.getY());

                Pathfinder.PathResult pathResult = pathfinder.findShortestPath(from, to, mapa, bloqueos,
                        fechaHoraActual);
                if (pathResult.isFound()) {
                    dist[i][j] = pathResult.getDistance();
                    dist[j][i] = pathResult.getDistance();
                } else {
                    System.err.printf("No se pudo encontrar un camino entre %s y %s\n", from, to);
                    dist[i][j] = Double.MAX_VALUE;
                    dist[j][i] = Double.MAX_VALUE;
                }
            }
        }
        System.out.printf("Distancias A* calculadas para el camión %s\n", camion.getCodigo());

        /* --- crear población inicial --- */
        Population pop = new Population(cfg.getPopSize());
        for (int i = 0; i < cfg.getPopSize(); i++)
            pop.add(randomChromosome(n));

        // evaluar la población inicial
        evaluar(pop, dist, nodos, pedidosPorDestino, camion);
        pop.sort(); // ordenar por fitness

        /* --- ciclo evolutivo --- */
        for (int gen = 0; gen < cfg.getNGenerations(); gen++) {
            Population next = new Population(cfg.getPopSize());

            if (!pop.individuals.isEmpty())
                next.add(copy(pop.best()));

            // generar el resto de la población
            while (next.individuals.size() < cfg.getPopSize()) {
                Chromosome p1 = torneo(pop);
                Chromosome p2 = torneo(pop);

                // crossover
                List<Chromosome> hijos = crossover(p1, p2);

                // mutacion
                List<Chromosome> mutableHijos = new ArrayList<>(hijos);
                mutableHijos.replaceAll(this::mutar); // mutate the copy

                // agregar hijos a la nueva población
                for (Chromosome h : mutableHijos)
                    if (next.individuals.size() < cfg.getPopSize())
                        next.add(h);
                    else
                        break;
            }

            // evaluar la nueva población
            evaluar(next, dist, nodos, pedidosPorDestino, camion);
            next.sort();
            pop = next;

            // Optional: Print best fitness per generation
            // if (gen % 10 == 0 && !pop.individuals.isEmpty()) {
            // System.out.printf("Gen %d: Best Fitness = %.6f (Dist: %.2f)\n",
            // gen, pop.best().getFitness(), 1.0/pop.best().getFitness());
            // }
        }

        if (pop.individuals.isEmpty()) {
            System.err.printf("Warning (GA): No se pudo encontrar una ruta para el camión %s\n", camion.getCodigo());
            return null;
        }

        Chromosome best = pop.best();

        // evaluar si la mejor solución es válida (fitness > 0)
        if (best.getFitness() <= 0) {
            System.err.printf("Warning (GA): Fitness <= 0 para el camión %s\n", camion.getCodigo());
            return null; // no feasible solution found
        }

        // convertir el mejor cromosoma a una ruta
        List<Nodo> ruta = best.getGenes().stream()
                .map(i -> {
                    Nodo nodo = nodos.get(i);
                    int esDestino = (i == 0) ? 0 : 1; // 0 = depósito, 1 = destino
                    nodo.setDestino(esDestino);
                    return nodo;
                }).collect(Collectors.toList());

        // calcular tiempos de inicio y fin
        LocalDateTime startTime = camion.getFechaInicio() != null
                ? camion.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        // recalcular la distancia final
        double distancia = 0;
        List<Integer> genes = best.getGenes();
        for (int i = 0; i < genes.size() - 1; i++) {
            double segmentDistance = dist[genes.get(i)][genes.get(i + 1)];
            if (segmentDistance == Double.MAX_VALUE) {
                System.err.printf("Error (GA): No se pudo encontrar un camino entre %s y %s\n",
                        nodos.get(genes.get(i)), nodos.get(genes.get(i + 1)));
                return null; // no feasible solution found
            }
            distancia += segmentDistance;
        }

        double totalTime = (distancia / camion.getVelocidad()) * 60; // en minutos
        LocalDateTime endTime = startTime.plusMinutes(Math.round(totalTime));

        return new Ruta(ruta, startTime.toString(), endTime.toString());
    }

    /* ---------- GA utils ---------- */

    private Chromosome randomChromosome(int n) {
        List<Integer> g = new ArrayList<>();
        // genes = [0, perm(1..n-1), 0] pero codificamos sólo la perm, 0 implícito
        g.add(0); // depósito al inicio
        List<Integer> destinations = IntStream.range(1, n).boxed().collect(Collectors.toList());
        Collections.shuffle(destinations, rnd);
        g.addAll(destinations);
        // g.add(0); // depósito al final
        return new Chromosome(g, 0.0); // fitness inicial 0
    }

    // evaluates the fitness of each chromosome in the population
    private void evaluar(Population pop, double[][] dist, List<Nodo> todos,
            Map<Nodo, List<Pedido>> pedidosDestino, Camion camion) {

        LocalDateTime startTime = camion.getFechaInicio() != null
                ? camion.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        for (Chromosome c : pop.individuals) {
            double currentLength = 0;
            LocalDateTime currentTime = startTime; // Reset time for each chromosome
            boolean deadlinesMet = true;
            boolean pathPossible = true;
            List<Integer> genes = c.getGenes();

            for (int i = 0; i < genes.size() - 1; i++) {
                int fromIdx = genes.get(i);
                int toIdx = genes.get(i + 1);

                Nodo fromNodo = todos.get(fromIdx);
                Nodo toNodo = todos.get(toIdx);

                if (!pathPossible)
                    break;

                double segmentDist = dist[fromIdx][toIdx];
                if (segmentDist == Double.MAX_VALUE) {
                    pathPossible = false;
                    break; // stop calculating if path is impossible
                }

                currentLength += segmentDist;

                // Calculate travel time
                double travelMinutes = (segmentDist / camion.getVelocidad()) * 60.0;
                currentTime = currentTime.plusMinutes(Math.round(travelMinutes));

                // Check deadlines at destination
                if (toIdx != 0) { // If it's not the depot
                    Nodo destinationNode = todos.get(toIdx);
                    List<Pedido> pedidosAtDestination = pedidosDestino.get(destinationNode);
                    if (pedidosAtDestination != null) {
                        for (Pedido p : pedidosAtDestination) {
                            if (p.getFechaLimiteEntrega() != null && currentTime.isAfter(p.getFechaLimiteEntrega())) {
                                deadlinesMet = false;
                                // Optional: Add penalty based on lateness
                                // double lateness = Duration.between(p.getFechaLimiteEntrega(),
                                // currentTime).toMinutes();
                                // currentLength += lateness * PENALTY_FACTOR; // Add penalty to distance
                                break;
                            }
                        }
                    }
                }
                if (!deadlinesMet)
                    break; // Stop checking this chromosome if deadline missed
            }

            // Assign fitness
            if (!pathPossible) {
                c.setFitness(0.0); // Impossible path = zero fitness
            } else if (!deadlinesMet) {
                // Option 1: Zero fitness for any deadline miss
                c.setFitness(0.0);
                // Option 2: Penalized fitness (inversely proportional to penalized distance)
                // double penalizedDistance = currentLength * DEADLINE_PENALTY_FACTOR; // Make
                // distance much larger
                // c.setFitness(1.0 / penalizedDistance);
            } else if (currentLength <= 0) {
                c.setFitness(0.0); // Avoid division by zero if distance is somehow zero
            } else {
                // Fitness is inversely proportional to distance for feasible solutions
                c.setFitness(1.0 / currentLength);
            }
        }
    }

    private Chromosome torneo(Population pop) {
        if (pop.individuals.isEmpty())
            return null;

        Population tournament = new Population(cfg.getTournamentK());
        for (int i = 0; i < cfg.getTournamentK(); i++) {
            Chromosome cand = pop.individuals.get(rnd.nextInt(pop.individuals.size()));
            tournament.add(cand);
        }
        tournament.sort(); // ordenar por fitness
        return tournament.best();
    }

    // Order-1 crossover
    private List<Chromosome> crossover(Chromosome p1, Chromosome p2) {
        // retorna copias de p1 y p2 si no se hace crossover
        if (rnd.nextDouble() > cfg.getCrossoverProb() || p1 == null || p2 == null)
            return List.of(copy(p1), copy(p2));

        List<Integer> g1 = p1.getGenes();
        List<Integer> g2 = p2.getGenes();
        int size = g1.size(); // Includes start/end depots

        // crossover the sequence of destinations (excluding start/end depots)
        int tourSize = size - 2;
        if (tourSize < 2) { // not enough destinations to perform crossover
            return List.of(copy(p1), copy(p2));
        }

        int a = rnd.nextInt(tourSize); // crossover points within the destination list
        int b = rnd.nextInt(tourSize);
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        // adjust indices to account for the starting depot at index 0
        int startIdx = a + 1;
        int endIdx = b + 1;

        // create offspring lists initialized with -1 (unassigned)
        List<Integer> h1Genes = new ArrayList<>(Collections.nCopies(size, -1));
        List<Integer> h2Genes = new ArrayList<>(Collections.nCopies(size, -1));
        h1Genes.set(0, 0); // start depot
        h2Genes.set(0, 0); // start depot
        h1Genes.set(size - 1, 0); // end depot
        h2Genes.set(size - 1, 0); // end depot

        // copy the segment from p1 to h1 and from p2 to h2
        Set<Integer> h1Segment = new HashSet<>();
        Set<Integer> h2Segment = new HashSet<>();
        for (int i = startIdx; i <= endIdx; i++) {
            h1Genes.set(i, g1.get(i));
            h2Genes.set(i, g2.get(i));
            h1Segment.add(g1.get(i));
            h2Segment.add(g2.get(i));
        }

        fillRest(h1Genes, g2, h1Segment, startIdx, endIdx);
        fillRest(h2Genes, g1, h2Segment, startIdx, endIdx);

        return List.of(new Chromosome(h1Genes, 0.0), new Chromosome(h2Genes, 0.0));
    }

    private void fillRest(List<Integer> child, List<Integer> donor, Set<Integer> childSegment, int startIdx,
            int endIdx) {
        int size = child.size();
        int donorIdx = 0;
        int childIdx = 0;

        while (child.contains(-1)) { // While there are unfilled slots
            // Find the next empty slot in the child, skipping the copied segment and depots
            while (childIdx < size - 1 && child.get(childIdx) != -1) {
                childIdx++;
            }
            if (childIdx >= size - 1)
                break; // Should be full except last depot

            // Find the next gene in the donor that's not already in the child's copied
            // segment
            int geneToAdd = -1;
            while (donorIdx < donor.size()) {
                int currentDonorGene = donor.get(donorIdx++);
                // Check if it's a destination and not already in the child's segment
                if (currentDonorGene != 0 && !childSegment.contains(currentDonorGene)
                        && !child.contains(currentDonorGene)) {
                    geneToAdd = currentDonorGene;
                    break;
                }
            }

            if (geneToAdd != -1) {
                child.set(childIdx, geneToAdd);
            } else {
                // This might happen if logic has issues or duplicates exist where they
                // shouldn't
                System.err.println("Error during crossover fillRemaining - could not find suitable gene.");
                // Fill with a placeholder or handle error
                if (child.get(childIdx) == -1)
                    child.set(childIdx, 0); // Avoid infinite loop
            }
        }
        // Ensure start/end depots are correct
        child.set(0, 0);
        child.set(size - 1, 0);
    }

    private Chromosome mutar(Chromosome c) {
        if (rnd.nextDouble() > cfg.getMutationProb() || c == null)
            return c;

        Chromosome mutated = copy(c);
        List<Integer> genes = mutated.getGenes();
        int size = genes.size();
        if (size <= 3)
            return mutated; // no mutation possible

        // Swap two random genes in the range [1, size - 2] (excluding depots)
        int i = 1 + rnd.nextInt(size - 2);
        int j = 1 + rnd.nextInt(size - 2);
        while (i == j) {
            j = 1 + rnd.nextInt(size - 2); // Ensure i and j are different
        }

        Collections.swap(genes, i, j);
        mutated.setFitness(0); // reset fitness to be recalculated
        return mutated;
    }

    private Chromosome copy(Chromosome c) {
        if (c == null)
            return null;
        return new Chromosome(new ArrayList<>(c.getGenes()), c.getFitness());
    }

    /* ---------- asignación FCFS idéntica a ACO ---------- */
    private Resultado asignarPedidos(List<Camion> camiones, List<Pedido> pedidos, List<Almacen> almacenes,
            LocalDateTime fechaHoraActual) {

        Resultado resultado = new Resultado();
        Map<Camion, List<Pedido>> map = new HashMap<>();
        // ordenar pedidos por fecha de pedido / deadline
        // pedidos.sort(Comparator.comparing((Pedido::getFechaLimiteEntrega),
        // Comparator.nullsFirst(Comparator.naturalOrder()))); // FCFS
        pedidos.sort(Comparator
                .<Pedido, Boolean>comparing(p -> p.isCritico(fechaHoraActual), Comparator.reverseOrder())
                .thenComparing(Pedido::getFechaLimiteEntrega));

        // Creamos una lista de pedidos no asignados para considerarlos
        // en las proximas asignaciones. Se comporta como una cola
        List<Pedido> pedidosAsignados = new ArrayList<>();
        List<Pedido> pedidosNoAsignados = new ArrayList<>();

        // Resetar camiones primero
        for (Camion c : camiones) {
            if (!c.isReplanificar()) {
                c.setCargaGLP(0);
            }
            map.put(c, new ArrayList<>());
        }

        // Asignar primero los pedidos a los camiones disponibles para replanificación
        Iterator<Pedido> itRep = pedidos.iterator();
        System.out.println("Asignando pedidos a camiones disponibles para replanificación... " + pedidos.size());
        while (itRep.hasNext()) {
            Pedido p = itRep.next();
            Optional<Camion> asignado = Optional.empty();

            asignado = camiones.stream()
                    .filter(c -> c.isReplanificar()) // si es replanificar
                    .filter(c -> c.getCargaGLPReplanificar() >= p.getVolumen()) // can hold the pedido replanificado
                    .sorted(Comparator.comparingDouble(c -> c.getCapacidad())) // prefer smaller capacity
                    .sorted(Comparator
                            .comparingDouble(c -> p.getDistancia(c.getNodoActual().getX(), c.getNodoActual().getY()))) // prefer
                                                                                                                       // less
                                                                                                                       // distancia
                    .findFirst();
            // Asignar el pedido al camión
            if (asignado.isPresent()) {
                Camion c = asignado.get();
                map.get(c).add(p);
                c.setCargaGLPReplanificar(c.getCargaGLPReplanificar() - p.getVolumen());
                p.setCamionAsignado(c); // asignar el camion al pedido
                System.out.println(
                        "Asignando pedido " + p.getId() + " al camión " + c.getCodigo() + " por replanificación.");
                pedidosAsignados.add(p); // agregar a la lista de pedidos asignados
                c.setRutaVehiculo(new ArrayList<>()); // limpiar la ruta del camion
            }
        }
        pedidos.removeAll(pedidosAsignados); // eliminar los pedidos asignados de la lista original
        System.out.println("Pedidos luego de la replanificación: " + pedidos.size());

        // Asignar el pedido a los almacenes cercanos, con ciertas condiciones
        // Se deberian asignar todos los pedidos ojo
        Iterator<Pedido> it = pedidos.iterator();
        while (it.hasNext()) {
            Pedido p = it.next();
            List<Almacen> almacenesCercanos = this.encontrarAlmacenesOrdenadosPorCercania(p, almacenes);
            if (!almacenesCercanos.isEmpty()) {
                Iterator<Almacen> almacenIt = almacenesCercanos.iterator();
                boolean almacenAsignadoBool = false;
                while (almacenIt.hasNext()) {
                    Almacen a = almacenIt.next();
                    // Si es principal o es secundario y tiene suficiente carga GLP y flota,
                    // asignar.
                    if ((a.getCargaGLP() >= p.getVolumen() && a.getCapacidadFlota() > p.getVolumen())) {
                        // asignar el pedido al almacen
                        if (!a.isPrincipal())
                            a.setCargaGLP(a.getCargaGLP() - (int) p.getVolumen());
                        List<Pedido> pedidosAlmacen = new ArrayList<>(a.getPedidosAlmacen());
                        pedidosAlmacen.add(p); // ahora sí puedes agregar
                        a.setPedidosAlmacen(pedidosAlmacen); // actualiza la lista en el almacén
                        p.setAlmacenAsignado(a); // asignar el almacen al pedido
                        almacenAsignadoBool = true;
                        System.out.printf(
                                "Pedido %d-%d asignado al almacén %s.\n",
                                p.getId(), p.getChildId(),
                                p.getAlmacenAsignado().getId());
                        break; // salir del bucle si se asignó el pedido
                    }
                }
                // Si ninguno cumple se asigna al almacén principal
                Iterator<Almacen> almacenIt2 = almacenesCercanos.iterator();
                if (!almacenAsignadoBool) {
                    while (almacenIt2.hasNext()) {
                        Almacen a = almacenIt2.next();
                        // Si es principal o es secundario y tiene suficiente carga GLP y flota,
                        // asignar.
                        if (a.isPrincipal()) {
                            // asignar el pedido al almacen
                            List<Pedido> pedidosAlmacen = new ArrayList<>(a.getPedidosAlmacen());
                            pedidosAlmacen.add(p); // ahora sí puedes agregar
                            a.setPedidosAlmacen(pedidosAlmacen); // actualiza la lista en el almacén
                            p.setAlmacenAsignado(a); // asignar el almacen al pedido
                            System.out.printf("Pedido %d-%d asignado al almacén %s, no había otra opcion.\n", p.getId(),
                                    p.getChildId(),
                                    p.getAlmacenAsignado().getId());
                            break; // salir del bucle si se asignó el pedido
                        }
                    }
                }
            } else {
                // No deberia pasar esto ojo.
                System.err.printf("Advertencia. No se encontró un almacén cercano para el pedido %d.\n", p.getId());
            }
        }

        // Asignar pedidos a camiones disponibles
        ListIterator<Pedido> listIterator = pedidos.listIterator();
        while (listIterator.hasNext()) {
            Pedido p = listIterator.next();
            Almacen almacenAsignado = p.getAlmacenAsignado(); // Obtenemos el camión asignado al almacén
            // Si el deadline es bajo, asignar a un camión que esté vacio
            Optional<Camion> asignado = Optional.empty();
            if (p.isCritico(fechaHoraActual)) {
                asignado = almacenAsignado.getCamionesEnAlmacen().stream()
                        .filter(c -> c.isReplanificar() == false) // no replanificar
                        .filter(c -> camiones.contains(c)) // only if it's in the available list
                        .filter(c -> c.getCargaGLP() == 0) // is empty
                        .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen()) // can hold the pedido
                        .sorted(Comparator.comparingDouble(c -> c.getCapacidad())) // prefer smaller capacity
                        .findFirst();
                if (!asignado.isPresent()) {
                    asignado = almacenAsignado.getCamionesEnAlmacen().stream()
                            .filter(c -> c.isReplanificar() == false) // no replanificar
                            .filter(c -> camiones.contains(c)) // only if it's in the available list
                            .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen()) // can hold the pedido
                            .sorted(Comparator.comparingDouble(c -> c.getCapacidad())) // prefer smaller capacity
                            .findFirst();
                }
            } else {
                asignado = almacenAsignado.getCamionesEnAlmacen().stream()
                        .filter(c -> c.isReplanificar() == false) // no replanificar
                        .filter(c -> camiones.contains(c)) // only if it's in the available list
                        .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen()) // can hold the pedido
                        .sorted(Comparator.comparingDouble(c -> c.getCapacidad())) // prefer smaller capacity
                        .findFirst();
            }

            // Asignar el pedido al camión
            if (asignado.isPresent()) {
                Camion c = asignado.get();
                map.get(c).add(p);
                c.setCargaGLP(c.getCargaGLP() + p.getVolumen());
                p.setCamionAsignado(c); // asignar el camion al pedido
                System.out.println("Asignando pedido " + p.getId() + " al camión " + c.getCodigo());
                pedidosAsignados.add(p); // agregar a la lista de pedidos asignados
            } else if (!asignado.isPresent()) {
                // Dividir el pedido si es necesario
                List<Camion> camionesEnAlmacen = almacenAsignado.getCamionesEnAlmacen();
                List<Pedido> pedidosDivididos = dividirPedido(p, camionesEnAlmacen);
                Optional<Camion> camionAsignado = Optional.empty();
                camionAsignado = camionesEnAlmacen.stream()
                        .filter(c -> c.getCapacidad() - c.getCargaGLP() > 0)
                        .findFirst();
                boolean hayCamionDisponible = camionAsignado.isPresent();
                if (!hayCamionDisponible) {
                    System.err.printf(
                            "Advertencia. No se pudo asignar el pedido %d con deadline %s a ningún camión.\n",
                            p.getId(), Duration.between(fechaHoraActual, p.getFechaLimiteEntrega()).toString());
                    pedidosNoAsignados.add(p); // agregar a la lista de pedidos no asignados
                } else {
                    for (int i = 1; hayCamionDisponible; i++) {
                        camionAsignado = Optional.empty();
                        camionAsignado = camionesEnAlmacen.stream()
                                .filter(c -> c.getCapacidad() - c.getCargaGLP() > 0)
                                .findFirst();
                        // No se pudo dividir el pedido
                        if (pedidosDivididos.isEmpty()) {
                            pedidosNoAsignados.add(p); // agregar a la lista de pedidos no asignados
                            System.err.printf(
                                    "Advertencia. No se pudo asignar el pedido %d con deadline %s a ningún camión, no se puede dividir mas.\n",
                                    p.getId(), Duration.between(fechaHoraActual, p.getFechaLimiteEntrega()).toString());
                            break; // no hay más pedidos divididos
                        }

                        // Se pudo dividir o se encontro un camion que puede llevar el pedido entero
                        Pedido pedidoDividido = pedidosDivididos.remove(0);
                        Camion c = camionAsignado.get();
                        if (!map.containsKey(c)) {
                            map.put(c, new ArrayList<>());
                        }
                        map.get(c).add(pedidoDividido);
                        pedidoDividido.setCamionAsignado(c); // asignar el camion al pedido
                        c.setCargaGLP(c.getCargaGLP() + pedidoDividido.getVolumen());
                        System.out.println(
                                "Asignando pedido dividido" + pedidoDividido.getId() + " - "
                                        + pedidoDividido.getChildId()
                                        + " al camión " + c.getCodigo() + " de carga " + pedidoDividido.getVolumen());
                        pedidosAsignados.add(pedidoDividido); // agregar a la lista de pedidos asignados

                        if (pedidosDivididos.size() == 0) { // El pedido fue asignado directamente sin dividir
                            System.out.println("Pedido " + pedidoDividido.getId() + " fue dividido " + i + " veces.");
                            break;
                        } else if (pedidosDivididos.size() == 1) { // Queda un pedido, osea hubo division
                            Pedido pedidoSobrante = pedidosDivididos.remove(0);
                            pedidosDivididos = dividirPedido(pedidoSobrante, camionesEnAlmacen);
                        }
                    }
                }
            } else {
                // No se pudo asignar el pedido a ningún camión
                pedidosNoAsignados.add(p); // agregar a la lista de pedidos no asignados
                System.err.printf(
                        "Advertencia. No se pudo asignar el pedido %d con deadline %s a ningún camión. No se cumplio ninguna condicion.\n",
                        p.getId(), Duration.between(fechaHoraActual, p.getFechaLimiteEntrega()).toString());
            }
        }
        if (!pedidosNoAsignados.isEmpty()) {
            System.err.printf("Advertencia. %d pedidos no asignados.\n", pedidosNoAsignados.size());
        }
        Set<Pedido> pedidosProcesados = new HashSet<>();
        pedidosProcesados.addAll(pedidosAsignados);
        pedidosProcesados.addAll(pedidosNoAsignados);

        for (Pedido p : pedidos) {
            if (!pedidosProcesados.contains(p)) {
                // Decide: should it be considered not assigned?
                pedidosNoAsignados.add(p);
                System.err
                        .println("WARNING: Pedido " + p.getId()
                                + " no fue clasificado, se agrega a pedidosNoAsignados.");
            }
        }
        resultado.setCamionesPedidos(map);
        resultado.setPedidosNoAtendidos(pedidosNoAsignados);
        resultado.setPedidosEnQeue(pedidosAsignados);
        return resultado;
    }

    /*
     * private boolean enSegmento(Nodo p, Nodo q, Nodo r) {
     * // Verificar si el nodo q está alineado con el tramo en el eje X o Y
     * if (p.getX() == r.getX()) { // Segmento vertical
     * return q.getX() == p.getX() &&
     * q.getY() >= Math.min(p.getY(), r.getY()) &&
     * q.getY() <= Math.max(p.getY(), r.getY());
     * } else if (p.getY() == r.getY()) { // Segmento horizontal
     * return q.getY() == p.getY() &&
     * q.getX() >= Math.min(p.getX(), r.getX()) &&
     * q.getX() <= Math.max(p.getX(), r.getX());
     * }
     * return false; // No es un segmento válido (no debería ocurrir en este caso)
     * }
     */

    private boolean nodoEnTramoBloqueado(Nodo nodo, Bloqueo bloqueo) {
        // Ahora que el tramo contiene todos los nodos bloqueados, solo verifica si está
        // en la lista
        return bloqueo.getTramo().contains(nodo);
    }

    // Filtra los camiones disponibles según su estado y mantenimiento
    private List<Camion> filtrarCamionesDisponibles(List<Camion> camiones,
            Map<String, List<LocalDate>> camionesEnMantenimiento, List<Camion> camionesRegresando,
            LocalDateTime fechaHoraActual) {

        // filtrar los camiones, de modo que nos quedemos con los que no estan en ruta
        // (en los almacenes)
        return camiones.stream()
                .filter(camion -> {
                    String codigoCamion = camion.getCodigo();
                    // Si el camión no está en mantenimiento o la fecha de mantenimiento es
                    // posterior a la actual

                    /*
                     * if (camionesEnMantenimiento.containsKey(codigoCamion)) {
                     * LocalDate fechaMantenimiento = camionesEnMantenimiento.get(codigoCamion);
                     * boolean en_mantenimiento =
                     * fechaMantenimiento.isEqual(fechaHoraActual.toLocalDate());
                     * camion.setEn_mantenimiento(en_mantenimiento);
                     * }
                     */

                    /*
                     * if (camion.isEnRuta() && camion.isEn_mantenimiento()) {
                     * // Si el camión está en ruta, no podemos filtrarlo por que le daremos una
                     * nueva ruta
                     * 
                     * List<Nodo> soloRegreso = new ArrayList<>();
                     * soloRegreso.add(camion.getNodoActual());
                     * soloRegreso.add(mapa.getDepositoMasCercano(camion.getNodoActual()));
                     * 
                     * camion.setRutaVehiculoNodos(new Ruta(soloRegreso, fechaHoraActual.toString(),
                     * null));
                     * camion.generarCaminosIntermedios(mapa, new ArrayList<>(), fechaHoraActual);
                     * camionesRegresando.add(camion);
                     * return false;
                     * }
                     */

                    return (!camion.isEnRuta() || camion.isReplanificar()) && !camion.isSaliendo()
                            && !camion.isEn_mantenimiento()
                            && !camion.isAveriado();
                })
                .collect(Collectors.toList());
    }

    // ---Seteo de parametros GA para el diseño de experimentos---
    public void setParametrosGA(int popSize, int nGenerations, double crossoverProb,
            double mutationProb) {
        this.cfg.setPopSize(popSize);
        this.cfg.setNGenerations(nGenerations);
        this.cfg.setCrossoverProb(crossoverProb);
        this.cfg.setMutationProb(mutationProb);
    }

    // Logica de division de pedidos
    public List<Pedido> dividirPedido(Pedido pedido, List<Camion> camiones) {

        List<Pedido> pedidosDivididos = new ArrayList<>();
        Optional<Camion> asignado = camiones.stream()
                .filter(c -> c.getCapacidad() - c.getCargaGLP() > 0)
                .findFirst();
        if (asignado.isPresent()) {
            double volumenEncontrado = asignado.get().getCapacidad() - asignado.get().getCargaGLP();
            if (volumenEncontrado < pedido.getVolumen()) {
                // Dividir el pedido
                int newChildId = Pedido.nextGlobalChildId();
                Pedido nuevoPedido = new Pedido(pedido.getId(), newChildId, pedido.getId(), true,
                        pedido.getClienteId(), volumenEncontrado,
                        pedido.getX(), pedido.getY(), pedido.getFechaPedido(), pedido.getFechaLimiteEntrega(),
                        pedido.getHorasPlazo());
                pedido.setVolumen(pedido.getVolumen() - volumenEncontrado);// Restar el volumen asignado
                pedidosDivididos.add(nuevoPedido);
                /*
                 * pedido.setLastChildId(pedido.getLastChildId() + 1); // Incrementar el ID del
                 * último hijo
                 */
                pedidosDivididos.add(pedido);
            } else {
                // Se encontro un camion que puede guardar el pedido entero
                pedidosDivididos.add(pedido);
            }
        }
        return pedidosDivididos;
    }

    /*
     * // NUEVA FUNCION DE ASIGNACION DE PEDIDOS EXCLUSIVAMENTE PARA LA
     * REPLANIFICACION
     * private Resultado asignarPedidosPorProximidad(List<Camion> camiones,
     * List<Pedido> pedidos, LocalDateTime fechaHoraActual) {
     * 
     * Resultado resultado = new Resultado();
     * Map<Camion, List<Pedido>> map = new HashMap<>();
     * 
     * // Inicializar el mapa de camiones y pedidos
     * for (Camion c : camiones) {
     * map.put(c, new ArrayList<>(c.getListaPedidos()));
     * }
     * 
     * List<Pedido> pedidosNoAsignados = new ArrayList<>();
     * 
     * for (Pedido pedido : pedidos) {
     * Camion camionMasCercano = null;
     * double distanciaMinima = Double.MAX_VALUE;
     * 
     * for (Camion camion : camiones) {
     * // Verificar que el camión no tenga pedidos críticos
     * boolean tienePedidosCriticos = camion.getListaPedidos().stream()
     * .anyMatch(p -> Duration.between(fechaHoraActual,
     * p.getFechaLimiteEntrega()).toMinutes() < 30);
     * 
     * boolean tieneCapacidad = camion.getCapacidad() >= pedido.getVolumen();
     * if (!tienePedidosCriticos && tieneCapacidad) {
     * // Calcular la distancia al pedido
     * double distancia = camion.getNodoActual().getDistancia(new
     * Nodo(pedido.getX(), pedido.getY()));
     * if (distancia < distanciaMinima) {
     * distanciaMinima = distancia;
     * camionMasCercano = camion;
     * }
     * }
     * }
     * 
     * // Asignar el pedido al camión más cercano
     * if (camionMasCercano != null) {
     * map.get(camionMasCercano).add(pedido);
     * } else {
     * pedidosNoAsignados.add(pedido);
     * System.err.println("No se pudo asignar el pedido " + pedido.getId() +
     * " a ningún camión.");
     * }
     * }
     * 
     * resultado.setCamionesPedidos(map);
     * resultado.setPedidosNoAtendidos(pedidosNoAsignados);
     * return resultado;
     * }
     */
    private void asignarPuntoAveriaEnRuta(Camion camion, List<Averia> averias, LocalDateTime fechaHoraActual) {

        Random rnd = new Random();
        Averia averia = averias.stream()
                .filter(a -> a.getCodigoUnidad().equals(camion.getCodigo()))
                .findFirst()
                .orElse(null);

        camion.setAveria(null); // Resetear avería antes de asignar una nueva
        // Si hay una avería asociada al camión y el camión está en ruta, asignar un
        // punto de avería
        System.out.println();
        if (averia != null) {
            // hallamos la distancia de la ruta (total de nodos)
            int totalNodos = camion.getRutaVehiculo().size();

            // Elegir un índice aleatorio entre el 5% y 35% de la ruta (sin incluir inicio
            // ni fin)
            int minIdx = Math.max(1, (int) Math.floor(totalNodos * 0.05));
            int maxIdx = Math.min(totalNodos - 2, (int) Math.ceil(totalNodos * 0.35));

            if (maxIdx <= minIdx)
                maxIdx = minIdx + 1;
            int idxAveria = minIdx + rnd.nextInt(maxIdx - minIdx + 1);
            Nodo nodoAveria = camion.getRutaVehiculo().get(idxAveria);

            averia.setNodoAveria(nodoAveria);
            camion.setAveria(averia);
            // finalmente removemos la avería de la lista de averías, no deberia volver a
            // pasar
            averias.remove(averia);
        }
    }

    private List<Almacen> encontrarAlmacenesOrdenadosPorCercania(Pedido pedido, List<Almacen> almacenes) {
        if (almacenes == null || almacenes.isEmpty() || pedido == null) {
            return Collections.emptyList();
        }
        return almacenes.stream()
                .sorted(Comparator.comparingDouble(
                        almacen -> pedido.getDistancia(almacen.getX(), almacen.getY())))
                .collect(Collectors.toList());
    }

    private void actualizarCamionesEnAlmacen(List<Almacen> almacenes, Camion camiones) {
        Iterator<Almacen> it = almacenes.iterator();
        while (it.hasNext()) {
            Almacen almacen = it.next();
            almacen.quitarCamionDelAlmacen(camiones);
        }
    }
}
