package edu.pucp.plg_back.dto;

import java.time.Duration;
import java.time.LocalDateTime;

import edu.pucp.plg_back.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDTO {

    private int id;
    private int childId; // ID del pedido hijo, si es un pedido dividido
    private String clienteId;
    private double volumen;
    private int X;
    private int Y;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaLimiteEntrega;
    private Duration tiempoRestante;
    private String camionAsignado;

    public void convertirPedidoToDTO(Pedido pedido, LocalDateTime fechaHoraActual) {
        this.id = pedido.getId();
        this.childId = pedido.getChildId();
        this.clienteId = pedido.getClienteId();
        this.volumen = pedido.getVolumen();
        this.X = pedido.getX();
        this.Y = pedido.getY();
        this.fechaPedido = pedido.getFechaPedido();
        this.fechaLimiteEntrega = pedido.getFechaLimiteEntrega();
        this.tiempoRestante = Duration.between(fechaHoraActual, this.fechaLimiteEntrega);
        this.camionAsignado = (pedido.getCamionAsignado() != null) ? pedido.getCamionAsignado().getCodigo() : null;
    }
}