import {
  Avatar,
  Box,
  Button,
  Chip,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import { useEffect, useState, useRef } from "react";
import IconoMapa from "./IconoMapa";
import IconoCamion from "./IconoCamion";
import ZoomInIcon from "@mui/icons-material/ZoomIn";
import ZoomOutIcon from "@mui/icons-material/ZoomOut";
import {
  arrayToDate,
  formatFechaHoraVerbose,
  horasMinutosSegundos,
  formatDiferenciaHorasMs,
} from "../utils/utilsMethods";
import InfoPopover from "./InfoPopover";
import ModalResultado from "./ModalResultado";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import IconoAlmacen from "./IconoAlmacen";

const Mapa = ({
  estadoSimulacion,
  currentSnapshot,
  resultado,
  queueLenght,
  fechaInicioSimulacion,
}: any) => {
  const filas = 51;
  const columnas = 71;

  const containerRef = useRef<HTMLDivElement>(null);
  const [containerSize, setContainerSize] = useState({ width: 0, height: 0 });

  useEffect(() => {
    const updateSize = () => {
      if (containerRef.current) {
        setContainerSize({
          width: containerRef.current.offsetWidth,
          height: containerRef.current.offsetHeight,
        });
      }
    };
    window.addEventListener("resize", updateSize);
    updateSize();
    return () => window.removeEventListener("resize", updateSize);
  }, []);

  const [duracionSimulacion, setDuracionSimulacion] = useState(0);
  const [openModal, setOpenModal] = useState(false);

  // Data from snapshot
  const pedidos = currentSnapshot?.pedidos ?? [];
  const bloqueos = currentSnapshot?.bloqueos || [];
  const camiones = currentSnapshot?.camiones || [];
  const almacenes = currentSnapshot?.almacenes || [];

  // Tiempo simulado
  const fechaActualDate = Array.isArray(currentSnapshot?.fechaHoraActual)
    ? arrayToDate(currentSnapshot?.fechaHoraActual)
    : new Date(currentSnapshot?.fechaHoraActual);

  const fechaInicioStr =
    fechaInicioSimulacion.length === 16
      ? fechaInicioSimulacion + ":00"
      : fechaInicioSimulacion;
  const fechaInicioDate = new Date(fechaInicioStr);

  const diffMs = fechaActualDate.getTime() - fechaInicioDate.getTime();

  // Capacidad de la flota
  const totalCargaGLP = camiones.reduce((sum: any, c: any) => sum + (c.cargaGLP ?? 0), 0);
  const totalCapacidad = camiones.reduce(
    (sum: any, c: any) => sum + (c.capacidad ?? 0),
    0
  );
  const porcentajeLlenado =
    totalCapacidad > 0
      ? ((totalCargaGLP / totalCapacidad) * 100).toFixed(1)
      : "0";

  //Tooltip state
  const [tooltipInfo, setTooltipInfo] = useState<{
    anchorEl: HTMLElement | null;
    x: number | null;
    y: number | null;
    tipo: string | null;
    camion?: any;
    almacen?: any;
    pedido?: any;
    bloqueo?: any;
    camionCodigo?: string | null;
    pedidoId?: string | null;
  }>({ anchorEl: null, x: null, y: null, tipo: null });

  const camionSeleccionado = camiones.find(
    (c: any) => c.codigo === tooltipInfo.camionCodigo
  );
  const pedidoSeleccionado = pedidos.find(
    (p: any) => p.id + "-" + p.childId === tooltipInfo.pedidoId
  );

  //Zoom controls
  const [zoom, setZoom] = useState(1.0);

  const handleZoomIn = () => setZoom((z) => Math.min(z + 0.2, 2));
  const handleZoomOut = () => setZoom((z) => Math.max(z - 0.2, 0.8));

  //const cellSize = 20 * zoom; // Size of each cell in the grid, adjusted by zoom level
  const borderSize = 17;
  const cellSize =
    Math.min(
      Math.floor(containerSize.width / columnas),
      Math.floor((containerSize.height - borderSize) / filas)
    ) * zoom;

  //Duracion de simulacion
  useEffect(() => {
    if (estadoSimulacion === 0) {
      setDuracionSimulacion(0);
    }
    if (estadoSimulacion === 1) {
      const interval = setInterval(() => {
        setDuracionSimulacion((prev) => {
          return prev + 1;
        });
      }, 1000); //add 1 second every second

      return () => clearInterval(interval);
    }
  }, [estadoSimulacion]);

  //Logica para mostrar el modal de resultados. Detecta fin de simulacion
  useEffect(() => {
    if (
      estadoSimulacion === 1 && //Ya se inicio la simulacion antes
      queueLenght === 0 && //Ya no hay mas snapshots en cola
      currentSnapshot != null && //Hay un snapshot mostrandose (el ultimo)
      resultado !== null //Recibio un resultado
    ) {
      setOpenModal(true);
    }
  }, [estadoSimulacion, queueLenght, resultado]);

  return (
    <Box
      ref={containerRef}
      sx={{
        width: "100%",
        height: "100%",
        position: "relative",
        overflow: "hidden",
        minHeight: 0,
        minWidth: 0,
        display: "flex",
        flexDirection: "column",
        margin: 0,
      }}
    >
      {/* Map Controls and Info */}
      <Box sx={{ mb: 1, display: "flex", gap: 1, alignItems: "center" }}>
        <Button variant="contained" size="small" onClick={handleZoomIn}>
          <ZoomInIcon />
        </Button>
        <Button variant="contained" size="small" onClick={handleZoomOut}>
          <ZoomOutIcon />
        </Button>
        {/* <Button
          variant={simulacionSpeed === 1 / 2 ? "contained" : "outlined"}
          size="small"
          onClick={() => setSimulacionSpeed(1 / 2)}
        >
          x1
        </Button>
        <Button
          variant={simulacionSpeed === 4 ? "contained" : "outlined"}
          size="small"
          onClick={() => setSimulacionSpeed(4)}
        >
          x2
        </Button> */}
        <Typography
          variant="body2"
          fontWeight="bold"
          sx={{ flexGrow: 2, textAlign: "center" }}
        >
          {currentSnapshot
            ? `Tiempo actual en simulación: ${formatFechaHoraVerbose(
              currentSnapshot.fechaHoraActual
            )}`
            : "Tiempo actual en simulación: -- ---, ---- --:--"}
        </Typography>
        <Typography
          variant="body2"
          fontWeight="bold"
          sx={{ flexGrow: 2, textAlign: "center" }}
        >
          {currentSnapshot
            ? `Tiempo transcurrido en la simulación: ${formatDiferenciaHorasMs(
              diffMs
            )}`
            : "Tiempo transcurrido en la simulación: -- ---, ---- --:--"}
        </Typography>
        <Typography
          variant="body2"
          fontWeight="bold"
          sx={{ flexGrow: 2, textAlign: "center" }}
        >
          {currentSnapshot
            ? `Tiempo actual (real): ${formatFechaHoraVerbose(new Date())}`
            : "Tiempo actual (real): -- ---, ---- --:--"}
        </Typography>
        <Typography
          variant="body2"
          fontWeight="bold"
          sx={{ flexGrow: 2, textAlign: "center" }}
        >
          {currentSnapshot
            ? `Duración de simulación: ${horasMinutosSegundos(
              duracionSimulacion
            )}`
            : "Duración de simulación: --:--:--"}
        </Typography>
        <Stack direction={"row"} spacing={1} alignItems="center">
          <Tooltip title="Carga total de GLP en los camiones">
            <Box>
              <Stack direction="row" spacing={0.5} alignItems="center">
                <Box
                  sx={{
                    width: cellSize,
                    height: cellSize,
                    background: "transparent",
                    cursor: "pointer",
                  }}
                >
                  <Avatar
                    sx={{
                      bgcolor:
                        Number(porcentajeLlenado) > 90
                          ? "red"
                          : Number(porcentajeLlenado) > 50
                            ? "orange"
                            : "green",
                      width: "90%",
                      height: "90%",
                      margin: "1px",
                    }}
                  >
                    <LocalShippingIcon sx={{ width: "75%", height: "75%" }} />
                  </Avatar>
                </Box>
                <Typography variant="body2">
                  {currentSnapshot
                    ? `Carga total GLP: ${totalCargaGLP} / ${totalCapacidad} (${porcentajeLlenado}%)`
                    : "----"}
                </Typography>
              </Stack>
            </Box>
          </Tooltip>
        </Stack>
        <Chip
          label={
            estadoSimulacion === 2
              ? "Pausado"
              : estadoSimulacion === 1
                ? "Corriendo"
                : "Sin Iniciar"
          }
          color={
            estadoSimulacion === 2
              ? "warning"
              : estadoSimulacion === 1
                ? "success"
                : "default"
          }
        />
      </Box>
      {/* Map Grid */}
      <Box
        sx={{
          width: "100%",
          height: "100%",
          overflow: "auto",
          display: "block",
          textAlign: "center",
          alignContent: "center",
          padding: 0,
          boxSizing: "border-box",
        }}
      >
        <Box
          sx={{
            display: "inline-block",
            position: "relative",
            width: columnas * cellSize,
            height: filas * cellSize,
            backgroundImage: `
              linear-gradient(to right, rgba(189, 189, 189, 0.8) 1px, transparent 1px),
              linear-gradient(to bottom, rgba(189, 189, 189, 0.8) 1px, transparent 1px)
            `,
            backgroundSize: `${cellSize}px ${cellSize}px`,
            backgroundPosition: "0 0",
            backgroundColor: "white",
            border: "1px solid #bdbdbd",
          }}
        >
          {/* Camiones */}
          {camiones.map((c: any) => {
            return (
              <Box
                key={`camion-${c.codigo}`}
                sx={{
                  position: "absolute",
                  left: c.nodoActual?.x * cellSize - cellSize / 2,
                  top: (filas - c.nodoActual?.y) * cellSize - cellSize / 2,
                  width: cellSize,
                  height: cellSize,
                  zIndex: 3,
                  background: "transparent",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  cursor: "pointer",
                  // border: `2px solid ${color}`, // borde del color del camión
                  borderRadius: "50%",
                  // Smooth transition for position changes
                  transition: "left 0.3s ease-in-out, top 0.3s ease-in-out",
                }}
                onClick={(e) =>
                  setTooltipInfo({
                    anchorEl: e.currentTarget,
                    x: c.nodoActual?.x,
                    y: c.nodoActual?.y,
                    tipo: "camion",
                    camionCodigo: c.codigo,
                  })
                }
              >
                <IconoCamion camion={c} color={"green"} />
              </Box>
            );
          })}
          {/* Pedidos */}
          {pedidos.map((p: any) => (
            <Box
              key={`pedido-${p.id}-${p.childId}-${p.clienteId}`}
              sx={{
                position: "absolute",
                left: p.x * cellSize - cellSize / 2,
                top: (filas - p.y) * cellSize - cellSize / 2,
                width: cellSize,
                height: cellSize,
                background: "transparent",
                zIndex: 4,
                cursor: "pointer",
              }}
              onClick={(e) =>
                setTooltipInfo({
                  anchorEl: e.currentTarget,
                  x: p.x,
                  y: p.y,
                  tipo: "pedido",
                  pedido: p,
                  pedidoId: p.id + "-" + p.childId,
                })
              }
            >
              <IconoMapa icono="pedido" />
            </Box>
          ))}
          {/* Bloqueos */}
          {/* {nodosBloqueados.map((b) => (
            <Box
              //key={`bloqueo-${b.x}-${b.y}`}
              sx={{
                position: "absolute",
                left: b.x * cellSize - cellSize / 2,
                top: (filas - b.y) * cellSize - cellSize / 2,
                width: cellSize,
                height: cellSize,
                background: "transparent",
                zIndex: 2,
                cursor: "pointer",
              }}
              onClick={(e) =>
                setTooltipInfo({
                  anchorEl: e.currentTarget,
                  x: b.x,
                  y: b.y,
                  tipo: "bloqueo",
                  bloqueo: b,
                })
              }
            >
              <IconoMapa icono="bloqueo" />
            </Box>
          ))}
          Bloqueos como líneas */}
          {bloqueos.map((b: any, i: any) =>
            (b.nodosIntermedios || []).length > 1
              ? b.nodosIntermedios
                .slice(0, -1)
                .map((startNode: any, idx: number) => {
                  const endNode = b.nodosIntermedios[idx + 1];
                  // Calculate positions (center of each cell)
                  const x1 = startNode.x * cellSize;
                  const y1 = (filas - startNode.y) * cellSize;
                  const x2 = endNode.x * cellSize;
                  const y2 = (filas - endNode.y) * cellSize;
                  // Calculate distance and angle
                  const dx = x2 - x1;
                  const dy = y2 - y1;
                  const length = Math.sqrt(dx * dx + dy * dy);
                  const angle = Math.atan2(dy, dx) * (180 / Math.PI);

                  return (
                    <Box
                      key={`bloqueo-linea-${i}-${idx}`}
                      sx={{
                        position: "absolute",
                        left: x1,
                        top: y1,
                        width: length,
                        height: 6, // thickness of the line
                        background: "red",
                        zIndex: 2,
                        transform: `rotate(${angle}deg)`,
                        transformOrigin: "0 50%",
                        borderRadius: "2px",
                        cursor: "pointer", // Make it look clickable
                        opacity: 0.8,
                      }}
                      onClick={(e) =>
                        setTooltipInfo({
                          anchorEl: e.currentTarget,
                          x: (startNode.x + endNode.x) / 2,
                          y: (startNode.y + endNode.y) / 2,
                          tipo: "bloqueo",
                          bloqueo: b,
                        })
                      }
                    />
                  );
                })
              : null
          )}
          {/* Almacenes */}
          {almacenes.map((a: any) => (
            <Box
              key={`almacen-${a.id}`}
              sx={{
                position: "absolute",
                left: a.x * cellSize - cellSize / 2,
                top: (filas - a.y) * cellSize - cellSize / 2,
                width: cellSize,
                height: cellSize,
                background: a.cargaGLP > 0 ? "black" : "red",
                zIndex: 4,
                cursor: "pointer",
              }}
              onClick={(e) =>
                setTooltipInfo({
                  anchorEl: e.currentTarget,
                  x: a.x,
                  y: a.y,
                  tipo: "almacen",
                  almacen: a,
                })
              }
            >
              <IconoAlmacen almacen={a} />
            </Box>
          ))}
          {/* Rutas como líneas */}
          {camiones.map((r: any, i: any) => {
            return (r.rutaVehiculo || []).length > 1
              ? r.rutaVehiculo
                .slice(0, -1)
                .map((startNode: any, idx: number) => {
                  const endNode = r.rutaVehiculo[idx + 1];
                  // Calculate positions (over grid lines - corner to corner)
                  const x1 = startNode.x * cellSize;
                  const y1 = (filas - startNode.y) * cellSize;
                  const x2 = endNode.x * cellSize;
                  const y2 = (filas - endNode.y) * cellSize;
                  // Calculate distance and angle
                  const dx = x2 - x1;
                  const dy = y2 - y1;
                  const length = Math.sqrt(dx * dx + dy * dy);
                  const angle = Math.atan2(dy, dx) * (180 / Math.PI);

                  return (
                    <Box
                      key={`ruta-linea-${i}-${idx}`}
                      sx={{
                        position: "absolute",
                        left: x1,
                        top: y1,
                        width: length,
                        height: 6, // Made slightly thicker for better visibility
                        background: "black",
                        zIndex: 2, // Higher than trucks (zIndex: 3) so lines appear on top
                        transform: `rotate(${angle}deg)`,
                        transformOrigin: "0 50%",
                        borderRadius: "3px",
                        pointerEvents: "none", // so it doesn't block clicks
                        opacity: 0.8, // Slight transparency so truck is still clearly visible
                      }}
                    />
                  );
                })
              : null;
          })}
        </Box>
      </Box>

      <InfoPopover
        anchorEl={tooltipInfo.anchorEl}
        x={tooltipInfo.x}
        y={tooltipInfo.y}
        tipo={tooltipInfo.tipo}
        camion={camionSeleccionado}
        almacen={tooltipInfo.almacen}
        pedido={pedidoSeleccionado}
        bloqueo={tooltipInfo.bloqueo}
        onClose={() =>
          setTooltipInfo({
            anchorEl: null,
            x: null,
            y: null,
            tipo: null,
          })
        }
      />
      {/*Modal que muestra el resultado de la simulacion*/}
      <ModalResultado
        open={openModal}
        resultado={resultado}
        onClose={() => setOpenModal(false)}
      />
    </Box>
  );
};

export default Mapa;
