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
public class ResultadoReplanificacion {

    private Map<Camion, List<Pedido>> camionPedidosTrasvase;
}
