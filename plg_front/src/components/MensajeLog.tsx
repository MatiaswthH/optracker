import { Box, Divider, Typography } from "@mui/material";
import React from "react";
import { formatFechaHoraVerbose } from "../utils/utilsMethods";

const MensajeLog = React.memo(({ evento, fechaHoraActual }) => {
  // Cambiar cuando se implemente el backend
  const bgColor =
    evento.tipo === 3
      ? "rgba(56, 155, 255, 0.1)"
      : evento.tipo === 2
      ? "rgba(56, 255, 66, 0.1)"
      : evento.tipo === 1
      ? "rgba(255, 165, 0, 0.1)" // Orange background for blockages
      : evento.tipo === 0
      ? "rgba(255, 56, 56, 0.1)"
      : "#ffffff";

  const texto =
    evento.tipo === 3
      ? "Se han procesado pedidos"
      : evento.tipo === 2
      ? `El camion ${evento.pedidoEntregado.codigoCamion} entregó el pedido ${evento.pedidoEntregado.codigoPedido}-${evento.pedidoEntregado.codigoChildPedido} de volumen ${evento.pedidoEntregado.volumen} en la coordenada (${evento.pedidoEntregado.x},${evento.pedidoEntregado.y}).`
      : evento.tipo === 1
      ? `Bloqueo nuevo desde nodo (${evento.bloqueo.x1},${evento.bloqueo.y1}) hasta (${evento.bloqueo.x2},${evento.bloqueo.y2}).`
      : evento.tipo === 0
      ? `Averia de tipo ${evento.averia.tipoAveria} detectada en el camion ${evento.averia.codigoCamion} en la coordenada (${evento.averia.x},${evento.averia.y}).`
      : "#ffffff";
  return (
    <Box
      sx={{
        padding: "4px 2px",
        borderBottom: "1px solid #f0f0f0",
        fontSize: "0.85rem",
        backgroundColor: bgColor,
        minHeight: "50px",
        margin: "4px 0",
      }}
    >
      <Typography fontSize={12}>
        {formatFechaHoraVerbose(fechaHoraActual)}
      </Typography>
      <Divider sx={{ marginX: "4px", marginY: "2px" }}></Divider>
      <Typography fontSize={15}>{texto}</Typography>
    </Box>
  );
});

export default MensajeLog;
