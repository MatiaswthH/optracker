package edu.pucp.plg_back.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Resultado {
    private Map<Camion, List<Pedido>> camionesPedidos;
    private List<Pedido> pedidosNoAtendidos;
    private List<Pedido> pedidosEnQeue;

    // public Resultado(int width, int height) {
    //     this.width = width;
    //     this.height = height;
    //     this.bloqueos = new boolean[width][height];
    // }
}
