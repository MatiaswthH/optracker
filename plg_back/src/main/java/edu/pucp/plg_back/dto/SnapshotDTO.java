package edu.pucp.plg_back.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.pucp.plg_back.model.Almacen;
import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotDTO {

    private List<CamionDTO> camiones;
    private List<PedidoDTO> pedidos;
    private List<BloqueoDTO> bloqueos;
    private LocalDateTime fechaHoraActual;
    private List<LogEventoDTO> eventos;
    private List<AlmacenDTO> almacenes;

    public void convertirSnapshotToDTO(List<Camion> camionesToConvert, List<Pedido> pedidosToConvert,
            List<Bloqueo> bloqueosToConvert, List<Almacen> almacenesToConvert) {
        this.camiones = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.bloqueos = new ArrayList<>();
        this.almacenes = new ArrayList<>();

        for (Camion camion : camionesToConvert) {
            CamionDTO camionDTO = new CamionDTO();
            camionDTO.convertirCamionToDTO(camion);
            this.camiones.add(camionDTO);
        }
        for (Pedido pedido : pedidosToConvert) {
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.convertirPedidoToDTO(pedido, this.fechaHoraActual);
            this.pedidos.add(pedidoDTO);
        }
        for (Bloqueo bloqueo : bloqueosToConvert) {
            BloqueoDTO bloqueoDTO = new BloqueoDTO();
            bloqueoDTO.convertirBloqueoToDTO(bloqueo, this.fechaHoraActual);
            this.bloqueos.add(bloqueoDTO);
        }
        for (Almacen almacen : almacenesToConvert) {
            AlmacenDTO almacenDTO = new AlmacenDTO();
            almacenDTO.convertirAlmacenToDTO(almacen);
            this.almacenes.add(almacenDTO);
        }
    }
}
