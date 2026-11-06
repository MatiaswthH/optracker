package edu.pucp.plg_back.dto;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoSimulacionDTO {
    private String tipo = "resultadoSimulacion"; // Para que el front lo identifique
    private boolean tipoFin; //0 para colapso logistico. 1 para fin exitoso (todo bien)
    private int cantidadPedidosEntregados;
    private LocalDateTime fechaFin;
    private Duration tiempoSimulado;
}
