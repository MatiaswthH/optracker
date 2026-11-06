import { Box, CircularProgress, IconButton, Paper, Stack } from "@mui/material";
import { useCallback, useEffect, useState } from "react";
import Mapa from "../components/Mapa";
import LogEventos from "../components/LogEventos";
import { useSimulacionSemanal } from "../hooks/useSimulacionSemanal";
import { useSimulacionDiaria } from "../hooks/useSimulacionDiaria";
import stompClient from "../stomp-client";
import { useSimulacion } from "../context/SimulacionContext";
import { GridExpandMoreIcon } from "@mui/x-data-grid";
import MenuOpenIcon from "@mui/icons-material/MenuOpen"; // Puedes usar cualquier icono de abrir
import { TiposIncidente } from "../types";

// import { useOutletContext } from "react-router-dom";
// import ExtraInfoSimulacion from "../components/ExtraInfoSimulacion";

const Simulacion = () => {
  const [estadoSimulacion, setEstadoSimulacion] = useState(0);
  const [fechaInicio, setFechaInicio] = useState("2025-01-01T00:00");
  const [snapshotQueue, setSnapshotQueue] = useState<any[]>([]);
  const [currentSnapshot, setCurrentSnapshot] = useState<any>(null);
  const [snapshotsEventos, setSnapshotsEventos] = useState<any[]>([]);
  const [resultado, setResultado] = useState<any>(null);
  const [simulacionSpeed, setSimulacionSpeed] = useState(1); // 1=x1, 2=x2
  const [tipoSimulacion, setTipoSimulacion] = useState("Semanal");
  const [showLogEventos, setShowLogEventos] = useState(true);

  //Logica de animacion del mapa
  const onSnapshot = useCallback((snapshot) => {
    setSnapshotQueue((prev) => [...prev, snapshot]);
    console.log("Snapshot recibido.");
  }, []);

  const onResultado = useCallback((res) => {
    setResultado(res);
    console.log("Resultado recibido:", res);
  }, []);

  // Usar el hook correcto según el tipo de simulación
  useSimulacionSemanal(
    tipoSimulacion === "Semanal" || tipoSimulacion === "ColapsoLogistico" ? estadoSimulacion : 0,
    onSnapshot,
    onResultado,
    fechaInicio
  );

  useSimulacionDiaria(
    tipoSimulacion === "Diario" ? estadoSimulacion : 0,
    onSnapshot,
    onResultado,
    fechaInicio
  );

  // Animation logic (move from Mapa)
  useEffect(() => {
    if (tipoSimulacion === "Diario") return;
    if (estadoSimulacion === 0) {
      setResultado(null);
      setSnapshotQueue([]);
      setCurrentSnapshot(null);
      setSnapshotsEventos([]);
    }
    if (estadoSimulacion === 1) {
      const interval = setInterval(() => {
        setSnapshotQueue((prevQueue) => {
          if (prevQueue.length > 0) {
            const nextSnapshot = prevQueue[0];
            setCurrentSnapshot(nextSnapshot);
            if (nextSnapshot?.eventos.length > 0) {
              setSnapshotsEventos((prev) =>
                [
                  ...prev,
                  ...nextSnapshot.eventos.map((evento) => ({
                    evento,
                    fechaHoraActual: nextSnapshot.fechaHoraActual,
                  })),
                ].slice(-30)
              );
            }
            return prevQueue.slice(1);
          }
          return prevQueue;
        });
      }, 100 / simulacionSpeed);
      return () => clearInterval(interval);
    }
  }, [tipoSimulacion, estadoSimulacion, simulacionSpeed]);

  //Animacion del mapa solo para simulacion diaria
  useEffect(() => {
    if (estadoSimulacion === 0) {
      setResultado(null);
      setSnapshotQueue([]);
      setCurrentSnapshot(null);
      setSnapshotsEventos([]);
      if (stompClient.active) {
        stompClient.publish({ destination: "/app/simulation/stop" });
        stompClient.deactivate();
      }
    }
    if (resultado && estadoSimulacion != 0) {
      if (stompClient.active && stompClient.connected) {
        stompClient.publish({ destination: "/app/simulation/stop" });
        stompClient.deactivate();
      }
    }
    else if (
      tipoSimulacion === "Diario" &&
      estadoSimulacion === 1 &&
      snapshotQueue.length > 0 &&
      resultado === null
    ) {
      const nextSnapshot = snapshotQueue[0];
      setCurrentSnapshot(nextSnapshot);
      if (nextSnapshot?.eventos?.length > 0) {
        setSnapshotsEventos((prev) =>
          [
            ...prev,
            ...nextSnapshot.eventos.map((evento) => ({
              evento,
              fechaHoraActual: nextSnapshot.fechaHoraActual,
            })),
          ].slice(-30)
        );
      }
      // Espera 50ms antes de enviar el continue
      const timeout = setTimeout(() => {
        if (stompClient.active) {
          stompClient.publish({ destination: "/app/simulation/continue" });
        }
        setSnapshotQueue((prevQueue) => prevQueue.slice(1));
      }, 20000);
      return () => clearTimeout(timeout);
    }
  }, [tipoSimulacion, estadoSimulacion, snapshotQueue]);

  // const { sidebarVisible } = useOutletContext(); //Sidebar visibility from context

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        width: "100%",
        height: "calc(100vh - 3px)",
        overflow: "hidden",
        position: "relative",
      }}
    >
      {/* Botón para mostrar el log de eventos cuando está oculto */}
      {!showLogEventos && (
        <IconButton
          size="large"
          sx={{
            position: "absolute",
            bottom: 32,
            left: 32,
            zIndex: 10,
            background: "white",
            boxShadow: 2,
          }}
          onClick={() => setShowLogEventos(true)}
          aria-label="Mostrar log de eventos"
        >
          <MenuOpenIcon />
        </IconButton>
      )}
      <Box
        sx={{
          flex: 1,
          display: "flex",
          padding: "10px",
          overflow: "hidden",
          minHeight: 0,
        }}
      >
        <Stack
          spacing="1rem"
          direction="row"
          sx={{
            width: "100%",
            height: "100%",
            minHeight: 0,
          }}
        >
          {showLogEventos && (
            <Paper
              square
              sx={{
                width: "300px",
                flexShrink: 0,
                padding: "16px",
                overflow: "auto",
                display: "flex",
                flexDirection: "column",
                flex: 1,
                position: "relative",
              }}
            >
              <IconButton
                size="small"
                sx={{ position: "absolute", top: 8, right: 8, zIndex: 2 }}
                onClick={() => setShowLogEventos(false)}
                aria-label="Ocultar log de eventos"
              >
                <GridExpandMoreIcon />
              </IconButton>
              <LogEventos
                estadoSimulacion={estadoSimulacion}
                setEstadoSimulacion={setEstadoSimulacion}
                fechaInicio={fechaInicio}
                setFechaInicio={setFechaInicio}
                snapshotsEventos={snapshotsEventos}
                currentSnapshot={currentSnapshot}
                setTipoSimulacion={setTipoSimulacion}
              />
            </Paper>
          )}
          <Paper
            square
            sx={{
              overflow: "hidden",
              display: "flex",
              justifyContent: "center",
              alignContent: "center",
              padding: "16px",
              paddingBottom: "0px",
              flex: 4,
              height: "100%",
            }}
          >
            <Mapa
              estadoSimulacion={estadoSimulacion}
              currentSnapshot={currentSnapshot}
              resultado={resultado}
              queueLenght={snapshotQueue.length}
              simulacionSpeed={simulacionSpeed}
              setSimulacionSpeed={setSimulacionSpeed}
              fechaInicioSimulacion={fechaInicio}
            />
          </Paper>
        </Stack>
      </Box>
    </Box>
  );
};

export default Simulacion;
