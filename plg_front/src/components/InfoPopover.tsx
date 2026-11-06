import {
  Box,
  Button,
  Divider,
  Popover,
  Stack,
  Typography,
} from "@mui/material";
import {
  formatDiferenciaHoras,
  formatFechaHora,
  formatFechaHoraVerbose,
  pad,
} from "../utils/utilsMethods";
import CloseIcon from "@mui/icons-material/Close";

interface InfoPopoverProps {
  anchorEl: HTMLElement | null;
  x: number | null;
  y: number | null;
  tipo: string | null;
  camion?: any;
  almacen?: any;
  pedido?: any;
  bloqueo?: any;
  onClose: () => void;
}

const InfoPopover = ({
  anchorEl,
  x,
  y,
  tipo,
  camion,
  almacen,
  pedido,
  bloqueo,
  onClose,
}: InfoPopoverProps) => {
  let content = null;

  //   useEffect(() => {
  //     if (!anchorEl) return;
  //     const observer = new MutationObserver(() => {
  //       if (!document.body.contains(anchorEl)) {
  //         onClose();
  //       }
  //     });
  //     observer.observe(document.body, { childList: true, subtree: true });
  //     return () => observer.disconnect();
  //   }, [anchorEl, onClose]);

  if (tipo === "camion" && camion) {
    const estado = camion.enMantenimiento
      ? "En Mantenimiento"
      : camion.inmovilizado
      ? "Inmovilizado"
      : camion.saliendo
      ? "Saliendo"
      : camion.despachando
      ? "Despachando"
      : camion.trasvasando
      ? "Trasvasando"
      : camion.enRuta
      ? "En ruta"
      : "En reposo";
    content = (
      <>
        <div>Codigo: {camion.codigo}</div>
        <div>Capacidad: {camion.capacidad}m3</div>
        <div>
          Carga Actual: {camion.cargaGLP}m3 (
          {((camion.cargaGLP / camion.capacidad) * 100).toFixed(1)}%)
        </div>
        <div>Estado: {estado}</div>
        <div>Cant. de pedidos: {camion.cantidadPedidos}</div>
        {camion.cantidadPedidos > 0 && (
          <div>
            Id de Pedidos:{" "}
            {camion.listaPedidos.map((p) => {
              return (
                <span key={p}>
                  {p}
                  {camion.listaPedidos.indexOf(p) !==
                  camion.listaPedidos.length - 1
                    ? ", "
                    : ""}
                </span>
              );
            })}
          </div>
        )}
        {camion.inmovilizado && (
          <>
            <div> Averia tipo: {camion.averia.tipo}</div>
            <div>
              Fin de averia:{" "}
              {formatFechaHoraVerbose(camion.averia.fechaFinAveria)}
            </div>
          </>
        )}
      </>
    );
  } else if (tipo === "almacen" && almacen) {
    content = (
      <>
        <div>Id: {almacen.id}</div>
        <div>
          Tipo: {almacen.principal ? "Almacen Principal" : "Almacen Secundario"}
        </div>
        <div>
          Capacidad: {almacen.principal ? "Infinito" : `${almacen.capacidad}m3`}
        </div>
        <div>
          GLP disponible:{" "}
          {almacen.principal ? "Infinito" : `${almacen.cargaGLP}m3 (${((almacen.cargaGLP / almacen.capacidad) * 100).toFixed(1)}%)`}
        </div>
        <div>
          Camiones en almacen: {almacen.cantCamionesEnAlmacen}{" "}
          {almacen.camionesEnAlmacen.length > 0
            ? `(${almacen.camionesEnAlmacen.join(", ")})`
            : ""}
        </div>
        {/* <div>Pedidos asignados: {almacen.cantPedidosAsignados}</div> */}
      </>
    );
  } else if (tipo === "pedido" && pedido) {
    content = (
      <>
        <div>
          Id: {pedido.id}-{pedido.childId}
        </div>
        <div>Volumen: {pedido.volumen}m3</div>
        <div>Fecha de solicitud: {formatFechaHora(pedido.fechaPedido)}</div>
        <div>Deadline: {formatFechaHora(pedido.fechaLimiteEntrega)}</div>
        <div>
          Horas hasta el deadline:{" "}
          {formatDiferenciaHoras(pedido.tiempoRestante)}
        </div>
        {pedido.camionAsignado ? (
          <div>Camion Asignado: {pedido.camionAsignado}</div>
        ) : (
          <div>Pedido sin asignar</div>
        )}
      </>
    );
  } else if (tipo === "bloqueo" && bloqueo) {
    content = (
      <>
        <div>Id: {bloqueo.id}</div>
        <Typography>
          Fecha de Inicio: {formatFechaHoraVerbose(bloqueo.fechaInicio)}
        </Typography>
        <Typography>
          Fecha de Fin: {formatFechaHoraVerbose(bloqueo.fechaFin)}
        </Typography>
        <Typography>
          Tiempo restante: {formatDiferenciaHoras(bloqueo.tiempoRestante)}
        </Typography>
      </>
    );
  }

  return (
    <Popover
      open={Boolean(anchorEl)}
      anchorEl={anchorEl}
      onClose={onClose}
      anchorOrigin={{
        vertical: "bottom",
        horizontal: "center",
      }}
      transformOrigin={{
        vertical: "top",
        horizontal: "center",
      }}
    >
      <Box p={2} width={400}>
        <Stack
          direction="row"
          alignItems="center"
          justifyContent={"space-between"}
          maxWidth={"100%"}
          sx={{ mb: 1, fontSize: "0.875rem", height: "1.5rem" }}
        >
          {camion ? (
            <Typography>
              {camion.nodoActual.x && camion.nodoActual.y
                ? `x:${pad(camion.nodoActual.x)} y:${pad(camion.nodoActual.y)}`
                : ""}
            </Typography>
          ) : (
            <Typography>{x && y ? `x:${pad(x)} y:${pad(y)}` : ""}</Typography>
          )}

          <Typography>
            {tipo ? tipo?.charAt(0).toUpperCase() + tipo?.slice(1) : ""}
          </Typography>
          <Button onClick={onClose} size="small" variant="text">
            <CloseIcon />
          </Button>
        </Stack>
        <Divider sx={{ my: 1 }} />
        <Box sx={{ mt: 1, fontSize: "0.875rem" }}>{content}</Box>
      </Box>
    </Popover>
  );
};

export default InfoPopover;
