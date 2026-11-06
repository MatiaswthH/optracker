package edu.pucp.plg_back.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import edu.pucp.plg_back.model.Almacen;
import edu.pucp.plg_back.model.Averia;
import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.ResultadoPlanificacion;

public interface Planificador {
    ResultadoPlanificacion planificar(List<Camion> camiones, List<Pedido> pedidos, List<Bloqueo> bloqueos,
            Map<String, List<LocalDate>> camionesEnMantenimiento, List<Averia> averias, List<Almacen> almacenes,
            LocalDateTime fechaHoraActual, boolean isDiario);

    void replanificar(List<Camion> camiones, Camion camionInmovilizado,
            LocalDateTime fechaHoraActual, List<Bloqueo> bloqueos, List<Almacen> almacenes, Map<String, List<LocalDate>> camionesEnMantenimiento);
}
