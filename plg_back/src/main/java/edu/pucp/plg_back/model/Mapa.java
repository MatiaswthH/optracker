// src/main/java/edu/pucp/plg_back/model/Mapa.java
package edu.pucp.plg_back.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.*;

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
public class Mapa {
    
    private int width = 70; // 70 km
    private int height = 50; // 50 km
    //private boolean[][] bloqueos; // true = nodo bloqueado
    private List<Bloqueo> bloqueos; // Lista de bloqueos
    private Set<Nodo> almacenes;
    private final Nodo depositoCentral = new Nodo(12, 8); // Nodo de la posicion del deposito central
    private final Nodo depositoIntNorte = new Nodo(42, 42); 
    private final Nodo depositoIntEste = new Nodo(63, 3); // Nodo de la posicion del deposito central

    public Mapa(int width, int height) {
        this.width = width;
        this.height = height;
        //this.bloqueos = new boolean[width][height];
        this.almacenes = new HashSet<>(); // ← ¡Inicializa aquí!
        // Agregar los depósitos como almacenes
        almacenes.add(depositoCentral);
        almacenes.add(depositoIntNorte);
        almacenes.add(depositoIntEste);
    }

    public Mapa() {
        this(70, 50);
    }

    public boolean esAlmacen(Nodo nodo) {
        if (almacenes == null) return false;
        for (Nodo almacen : almacenes) {
            if (almacen.getX() == nodo.getX() && almacen.getY() == nodo.getY()) {
                return true;
            }
        }
        return false;
    }

    // Puedes agregar un setter para los almacenes si lo necesitas
    public void setAlmacenes(Set<Nodo> almacenes) {
        this.almacenes = almacenes;
    }

    public Set<Nodo> getAlmacenes() {
        return almacenes;
    }
    

    public boolean estaLibre(int x, int y, List<Bloqueo> bloqueos) { // fuera de rango = bloqueado

        for (Bloqueo bloqueo : bloqueos) {
            for (Nodo nodo : bloqueo.getTramo()) {
                if (nodo.getX() == x && nodo.getY() == y) {
                    return false; // El nodo está bloqueado
                }
            }
        }   
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /* 
    // para actualizar bloqueos
    public void setBloqueado(int x, int y, boolean bloqueado) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            if (this.bloqueos == null) {
                this.bloqueos = new boolean[width][height];
            }
            this.bloqueos[x][y] = bloqueado;
        }
    }*/

    /* 
    // para adicionar multiples bloqueos
    public void addBlockages(List<Posicion> posiciones) {
        if (this.bloqueos == null) {
            this.bloqueos = new boolean[width][height];
        }
        if (posiciones != null) {
            for (Posicion p : posiciones) {
                setBloqueado(p.getX(), p.getY(), true);
            }
        }
    }
*/
    public Nodo getDepositoMasCercano(Nodo origen) {
        Set<Nodo> depositos = this.almacenes; // Asume que tienes este método
        Nodo masCercano = null;
        double minDist = Double.MAX_VALUE;

        for (Nodo deposito : depositos) {
            double dist = Math.hypot(deposito.getX() - origen.getX(), deposito.getY() - origen.getY());
            if (dist < minDist) {
                minDist = dist;
                masCercano = deposito;
            }
        }
        return masCercano;
    }
}
