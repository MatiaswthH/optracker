package edu.pucp.plg_back.dto;

import edu.pucp.plg_back.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogEventoDTO {
    private int tipo; // 0 averia detectada. 1 bloqueo detectado. 2 pedido entregado
    private AveriaEventoDTO averia;
    private PedidoEntregadoDTO pedidoEntregado;
    private BloqueoEventoDTO bloqueo;

    public void convertirPedidoEntregadoEnEvento(Pedido pedido, String codigoCamion) {
        PedidoEntregadoDTO ped = new PedidoEntregadoDTO();
        ped.setCodigoCamion(codigoCamion);
        ped.setCodigoPedido(pedido.getId());
        ped.setCodigoChildPedido(pedido.getChildId());
        ped.setVolumen(pedido.getVolumen());
        ped.setX(pedido.getX());
        ped.setY(pedido.getY());
        this.setPedidoEntregado(ped);
        this.setTipo(2);
    }

    public void convertirAveriaEnEvento(String codigoCamion, int tipoAveria, int coordX, int coordY) {
        AveriaEventoDTO av = new AveriaEventoDTO();
        av.setCodigoCamion(codigoCamion);
        av.setTipoAveria(tipoAveria);
        av.setX(coordX);
        av.setY(coordY);
        this.averia = av;
        this.setTipo(0);
    }

    public void convertirBloqueoEnEvento(int x1, int y1, int x2, int y2) {
        BloqueoEventoDTO bloq = new BloqueoEventoDTO();
        bloq.setX1(x1);
        bloq.setY1(y1);
        bloq.setX2(x2);
        bloq.setY2(y2);
        this.bloqueo = bloq;
        this.setTipo(1);
    }
}
