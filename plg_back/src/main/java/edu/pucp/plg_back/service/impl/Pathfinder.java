package edu.pucp.plg_back.service.impl;

import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Mapa;
import edu.pucp.plg_back.model.Nodo;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class Pathfinder {

    // resultado del intento de encontrar el camino
    public static class PathResult {
        private final double distance;
        private final List<Nodo> path; // reconstruct path if needed
        private final boolean found;

        public PathResult(double distance, List<Nodo> path, boolean found) {
            this.distance = distance;
            this.path = path;
            this.found = found;
        }

        public double getDistance() {
            return distance;
        }

        public List<Nodo> getPath() {
            return path;
        }

        public boolean isFound() {
            return found;
        }
    }

    // A* Algorithm Implementation
    public PathResult findShortestPath(Nodo startNode, Nodo endNode, Mapa map,
            List<Bloqueo> bloqueos, LocalDateTime fechaHoraActual) {

        PriorityQueue<Nodo> openSet = new PriorityQueue<>(Comparator.comparingDouble(Nodo::getF));
        Set<Nodo> closedSet = new HashSet<>();

        // initialize start node
        startNode.setG(0);
        startNode.calculateHeuristic(endNode);
        startNode.calculateF();
        startNode.setNodoprevio(null);
        openSet.add(startNode);

        List<Bloqueo> bloqueosConsiderar = new ArrayList<>();
        for (Bloqueo b : bloqueos) {
            if (b.getFechaInicio().isBefore(fechaHoraActual.plusHours(4)) && b.getFechaFin().isAfter(fechaHoraActual)) {
                bloqueosConsiderar.add(b);
            }
        }

        while (!openSet.isEmpty()) {
            Nodo current = openSet.poll();

            if (current.getX() == endNode.getX() && current.getY() == endNode.getY()) {
                return new PathResult(current.getG(), reconstructPath(current), true);
            }

            closedSet.add(current);

            for (Nodo neighbor : getNeighbors(current, bloqueosConsiderar, endNode)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                boolean esAlmacen = map.esAlmacen(neighbor);
                boolean esInicio = neighbor.getX() == startNode.getX() && neighbor.getY() == startNode.getY();
                boolean esDestino = neighbor.getX() == endNode.getX() && neighbor.getY() == endNode.getY();
                if (esAlmacen && !esInicio && !esDestino) {
                    continue;
                }

                double tentativeGScore = current.getG() + 1;

                boolean inOpenSet = openSet.stream().anyMatch(n -> n.equals(neighbor));

                if (!inOpenSet || tentativeGScore < neighbor.getG()) {
                    neighbor.setNodoprevio(current);
                    neighbor.setG(tentativeGScore);
                    neighbor.calculateHeuristic(endNode);
                    neighbor.calculateF();
                    if (!inOpenSet) {
                        openSet.add(neighbor);
                    } else {
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // camino no encontrado
        return new PathResult(Double.MAX_VALUE, Collections.emptyList(), false);
    }

    // helper para validad neighbors (up, down, left, right)
    private List<Nodo> getNeighbors(Nodo node, List<Bloqueo> bloqueos, Nodo endNode) {
        List<Nodo> neighbors = new ArrayList<>();
        int[] dx = { 0, 0, 1, -1 };
        int[] dy = { 1, -1, 0, 0 };

        for (int i = 0; i < 4; i++) {
            int newX = node.getX() + dx[i];
            int newY = node.getY() + dy[i];

            if (estaLibre(newX, newY, bloqueos) || (newX == endNode.getX() && newY == endNode.getY())) {
                // Create a new Nodo instance for the neighbor to avoid modifying
                // existing nodes in the open/closed sets incorrectly.
                // We only need coordinates for equality checks and heuristic.
                // G, H, F scores will be calculated if it's a better path.
                Nodo neighborNode = new Nodo(newX, newY);
                neighbors.add(neighborNode);
            }
        }
        return neighbors;
    }


    // Helper to reconstruct the path (optional)
    private List<Nodo> reconstructPath(Nodo target) {
        List<Nodo> path = new LinkedList<>();
        Nodo current = target;
        while (current != null) {
            path.add(0, current); // Add to the beginning
            current = current.getNodoprevio();
        }
        return path;
    }

    private boolean nodoEnTramoBloqueado(Nodo nodo, Bloqueo bloqueo) {

        List<Nodo> tramo = bloqueo.getTramo();
        for (Nodo bloqueado : tramo) {
            if (bloqueado.getX() == nodo.getX() && bloqueado.getY() == nodo.getY()) {
                return true;
            }
        }
        return false;
    }

    public boolean estaLibre(int x, int y, List<Bloqueo> bloqueos) { // fuera de rango =
                                                                     // bloqueado

        for (Bloqueo bloqueo : bloqueos) {
            for (Nodo nodo : bloqueo.getTramo()) {
                // Se le añade 4 horas extra para que considere tiempos de despacho, salida,
                // etc.
                if ((nodo.getX() == x && nodo.getY() == y)) {
                    return false; // El nodo está bloqueado
                }
            }
        }
        return x >= 0 && x < 70 && y >= 0 && y < 50;
    }
}
