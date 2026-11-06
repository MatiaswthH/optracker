package edu.pucp.plg_back.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoPlanificacion {
    private List<Ruta> rutas;
    private List<Pedido> pedidosNoAtendidos;
    private List<Pedido> pedidosEnQueue;
}
