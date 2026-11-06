package edu.pucp.plg_back.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int childId; // ID del pedido hijo, si es un pedido dividido
    private int parentId; // ID del pedido padre, si es un pedido dividido
    private boolean esDividido = false; // Indica si el pedido es un pedido dividido
    private int lastChildId = 0; // ID del último pedido hijo creado, para pedidos divididos

    private String clienteId;
    private double volumen;
    private int X;
    private int Y;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaLimiteEntrega;
    private double horasPlazo;
    private boolean seTrasvasa = false;
    private Almacen almacenAsignado;
    private Camion camionAsignado;

    private static int globalChildId = 1;

    public static synchronized int nextGlobalChildId() {
        return globalChildId++;
    }

    // Add this constructor if it does not exist
    public Pedido(int id, String clienteId, double volumen, int x, int y, java.time.LocalDateTime fechaPedido,
            java.time.LocalDateTime fechaLimiteEntrega, double horasPlazo) {
        this.id = id;
        this.clienteId = clienteId;
        this.volumen = volumen;
        this.X = x;
        this.Y = y;
        this.fechaPedido = fechaPedido;
        this.fechaLimiteEntrega = fechaLimiteEntrega;
    }

    // Constructor para pedidos divididos
    public Pedido(int id, int childId, int parentId, boolean esDividido, String clienteId, double volumen, int x, int y,
            java.time.LocalDateTime fechaPedido,
            java.time.LocalDateTime fechaLimiteEntrega, double horasPlazo) {
        this.id = id;
        this.childId = childId;
        this.parentId = parentId;
        this.esDividido = esDividido;
        this.clienteId = clienteId;
        this.volumen = volumen;
        this.X = x;
        this.Y = y;
        this.fechaPedido = fechaPedido;
        this.fechaLimiteEntrega = fechaLimiteEntrega;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", this.getX(), this.getY());
    }

    public double getDistancia(int x, int y) {
        return Math.sqrt(Math.pow(this.X - x, 2) + Math.pow(this.Y - y, 2));
    }

    public boolean isCritico(LocalDateTime fechaHoraActual) {
        Nodo nodoPromedio = new Nodo(39, 32);
        Nodo nodoPedido = new Nodo(this.X, this.Y);
        // Si el pedido es grande, deadline es menor a 4h y esta lejos en el mapa
        return (this.volumen > 25 && nodoPromedio.getDistancia(nodoPedido) >= 30)
                || Duration.between(fechaHoraActual, this.fechaLimiteEntrega).toHours() <= 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pedido pedido = (Pedido) o;
        return id.equals(pedido.id) && childId == pedido.childId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, childId);
    }
}
