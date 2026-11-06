package edu.pucp.plg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEntregadoDTO {
    private String codigoCamion;
    private int codigoPedido;
    private int codigoChildPedido; // ID del pedido hijo, si es un pedido dividido
    private double volumen;
    private int X;
    private int Y;
}
