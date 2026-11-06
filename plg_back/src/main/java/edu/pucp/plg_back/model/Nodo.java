package edu.pucp.plg_back.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Nodo implements Comparable<Nodo>, Serializable {

    private int idNodo;

    private int X;
    private int Y;
    private int destino; //Replicar lo de posicion (puede ser para trasvasar o para entregar)

    @Builder.Default
    private double distanciaMinima = Double.MAX_VALUE;

    @Builder.Default
    private double f = Double.MAX_VALUE;

    @Builder.Default
    private double g = Double.MAX_VALUE;

    private double h; // heuristica

    @Builder.Default
    private boolean estaBloqueado = false;

    private LocalDateTime inicioBloqueo;
    private LocalDateTime finBloqueo;

    @Builder.Default
    private Nodo nodoprevio = null;

    public Nodo(int X, int Y) {
        this.X = X;
        this.Y = Y;
        this.g = Double.MAX_VALUE;
        this.f = Double.MAX_VALUE;
    }

    // manhattan
    public double calculateHeuristic(Nodo destino) {
        this.h = Math.abs(this.X - destino.getX()) + Math.abs(this.Y - destino.getY());
        return this.h;
    }

    // recalcular f score
    public void calculateF() {
        this.f = this.g + this.h;
    }

    public float getDistancia(Nodo destino) {
        // calculadora de Manahattan
        int a, b, c, d;
        float r, r1;
        a = this.X;
        b = destino.getX();
        c = this.Y;
        d = destino.getY();
        r = Math.abs((float) b - a);
        r1 = Math.abs((float) d - c);

        return r + r1;
    }

    @Override
    public int compareTo(Nodo v) {
        // comparar por f score
        return Double.compare(this.f, v.getF());
    }

    // se sobrescribe para comparar los valores de las coordenadas x e y
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Nodo v = (Nodo) obj;
        return this.X == v.getX() && this.Y == v.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(X, Y); // hash based coordinates
    }

    public Nodo(Nodo n) {
        this.setX(n.getX());
        this.setY(n.getY());
        this.g = n.getG();
        this.h = n.getH();
        this.f = n.getF();
        this.nodoprevio = n.getNodoprevio();
        this.estaBloqueado = n.isEstaBloqueado();
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", X, Y);
    }

}
