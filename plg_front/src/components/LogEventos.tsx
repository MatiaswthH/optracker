import {
  Box,
  Divider,
  FormControl,
  FormControlLabel,
  FormLabel,
  Radio,
  RadioGroup,
  Avatar,
  Paper,
  Stack,
  IconButton,
  TextField,
  Tooltip,
  Typography,
  Switch,
} from "@mui/material";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import TakeoutDiningIcon from "@mui/icons-material/TakeoutDining";
import ClearIcon from "@mui/icons-material/Clear";
import MensajeLog from "./MensajeLog";
import ModalIniciarSimulacion from "./ModalIniciarSimulacion";
import PlayCircleIcon from "@mui/icons-material/PlayCircle";
import StopCircleIcon from "@mui/icons-material/StopCircle";
import PauseCircleIcon from "@mui/icons-material/PauseCircle";
import React, { useEffect, useRef, useState } from "react";
import ExtraInfoSimulacion from "./ExtraInfoSimulacion";
import Grow from "@mui/material/Grow";

const LogEventos = ({
  estadoSimulacion,
  setEstadoSimulacion,
  fechaInicio,
  setFechaInicio,
  snapshotsEventos,
  currentSnapshot,
  setTipoSimulacion,
}: any) => {
  const leyendaCamionIconos = [
    {
      id: "1",
      icon: <LocalShippingIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Camión",
      bgColor: "green",
    },
    {
      id: "2",
      icon: <LocalShippingIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Camión Averiado",
      bgColor: "red",
    },
    {
      id: "3",
      icon: <LocalShippingIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Despachando",
      bgColor: "rgb(255, 178, 46)",
    },
    {
      id: "4",
      icon: <WarehouseIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Depósito",
      bgColor: "black",
    },
    {
      id: "7",
      icon: <WarehouseIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Depósito Vacío",
      bgColor: "red",
    },
    {
      id: "5",
      icon: <TakeoutDiningIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Pedido",
      bgColor: "blue",
    },
    {
      id: "6",
      icon: <ClearIcon sx={{ height: "75%", width: "75%" }} />,
      text: "Bloqueo",
      bgColor: "red",
    },
  ];

  //Modo simulacion
  const [modoSimulacion, setModoSimulacion] = useState("Semanal");
  const handleChangeModoSimulacion = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setModoSimulacion((event.target as HTMLInputElement).value);
  };

  //Modal state
  const [modalOpen, setModalOpen] = useState(false);

  const handleOpenModal = () => {
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
  };

  const handleAceptarModal = () => {
    setEstadoSimulacion(1);
    setTipoSimulacion(modoSimulacion);
    setModalOpen(false);
  };

  //Logica para que log eventos funcione como un chat
  const eventos = snapshotsEventos?.flat() || [];
  const scrollRef = useRef<HTMLDivElement>(null);
  const isAtBottomRef = useRef(true);

  // Detect if user is at the bottom
  const handleScroll = () => {
    const el = scrollRef.current;
    if (!el) return;
    // Allow a small threshold (e.g., 10px) for floating point errors
    isAtBottomRef.current =
      el.scrollHeight - el.scrollTop - el.clientHeight < 10;
  };

  // Scroll to bottom if user was at the bottom when new events arrive
  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    if (isAtBottomRef.current) {
      el.scrollTop = el.scrollHeight;
    }
  }, [eventos.length]);

  // Extra info switch
  const [showLog, setShowLog] = useState(false);

  return (
    <>
      {/* //Modo de simulación */}
      <Box>
        <FormControl sx={{ margin: "5px" }}>
          <FormLabel disabled={estadoSimulacion > 0}>
            Modo de Simulación
          </FormLabel>
          <RadioGroup
            row
            value={modoSimulacion}
            onChange={handleChangeModoSimulacion}
          >
            <FormControlLabel
              value="Diario"
              control={<Radio />}
              label="Diario"
              disabled={estadoSimulacion != 0}
            />
            <FormControlLabel
              value="Semanal"
              control={<Radio />}
              label="Semanal"
              disabled={estadoSimulacion != 0}
            />
            <FormControlLabel
              value="ColapsoLogistico"
              control={<Radio />}
              label="Colapso Logístico"
              disabled={estadoSimulacion != 0}
            />
          </RadioGroup>
        </FormControl>
        <Stack
          sx={{
            display: "flex",
            flexDirection: "row",
            margin: "5px",
            columnGap: "10px",
          }}
        >
          {estadoSimulacion === 0 || estadoSimulacion === 2 ? (
            <Tooltip title={estadoSimulacion === 0 ? "Iniciar" : "Reanudar"}>
              <IconButton
                color="primary"
                size="small"
                sx={{
                  padding: 0,
                  minWidth: "unset",
                  minHeight: "unset",
                  width: "30px",
                  height: "30px",
                  borderRadius: "50%",
                  alignSelf: "center",
                }}
                disabled={fechaInicio === ""}
                onClick={() => {
                  if (estadoSimulacion === 0) {
                    handleOpenModal();
                  } else {
                    setEstadoSimulacion(1);
                  }
                }}
              >
                <PlayCircleIcon sx={{ fontSize: 28 }} />
              </IconButton>
            </Tooltip>
          ) : (
            <Tooltip title="Pausar">
              <IconButton
                color="primary"
                size="small"
                sx={{
                  padding: 0,
                  minWidth: "unset",
                  minHeight: "unset",
                  width: "30px",
                  height: "30px",
                  borderRadius: "50%",
                  alignSelf: "center",
                }}
                onClick={() => {
                  setEstadoSimulacion(2);
                }}
              >
                <PauseCircleIcon sx={{ fontSize: 28 }} />
              </IconButton>
            </Tooltip>
          )}

          <Tooltip title="Stop">
            <IconButton
              color="primary"
              size="small"
              disabled={estadoSimulacion === 0}
              sx={{
                padding: 0,
                minWidth: "unset",
                minHeight: "unset",
                width: "30px",
                height: "30px",
                borderRadius: "50%",
                alignSelf: "center",
              }}
              onClick={() => {
                setEstadoSimulacion(0);
              }}
            >
              <StopCircleIcon sx={{ fontSize: 28 }} />
            </IconButton>
          </Tooltip>
          <TextField
            fullWidth
            label="Fecha de Inicio"
            type="datetime-local"
            value={fechaInicio}
            onChange={(e) => {
              // Set minutes to 00
              const val = e.target.value;
              const [date, time] = val.split("T");
              const [hour] = time.split(":");
              setFechaInicio(`${date}T${hour}:00`);
            }}
            InputLabelProps={{ shrink: true }}
            inputProps={{
              min: "2025-01-01T00:00",
              max: "2026-12-31T23:00",
            }}
            helperText="Fecha de inicio de Simulación"
            disabled={estadoSimulacion != 0}
            sx={{ alignSelf: "center" }}
          />
        </Stack>
      </Box>
      <Divider
        color="#CEDADF"
        sx={{ height: 2, margin: "5px" }}
        variant="middle"
      />
      {/* Leyenda de eventos */}
      <Box>
        <Box sx={{ fontWeight: "bold", marginBottom: "8px" }}>Leyenda</Box>
        <Stack
          display="flex"
          flexDirection="row"
          flexWrap="wrap"
          padding="5px"
          align-content="space-evenly"
          flexGrow="0"
          flexShrink="0"
          flex-basis="16.6666%"
        >
          {leyendaCamionIconos.map((item) => (
            <Stack
              key={item.id}
              display="flex"
              flexDirection="column"
              rowGap="2px"
              flex="1"
              marginX="20px"
              marginY="3px"
            >
              <Avatar
                sx={{
                  bgcolor: item.bgColor,
                  height: "25px",
                  width: "25px",
                  alignSelf: "center",
                }}
              >
                {item.icon}
              </Avatar>
              <Typography
                sx={{
                  maxWidth: "80px",
                  whiteSpace: "normal",
                  textAlign: "center",
                  alignSelf: "center",
                  fontWeight: "400",
                }}
              >
                {item.text}
              </Typography>
            </Stack>
          ))}
        </Stack>
      </Box>
      {/* Log de eventos */}
      <Divider
        color="#CEDADF"
        sx={{ height: 1.5, margin: "5px" }}
        variant="middle"
      />
      <Box sx={{ display: "flex", alignItems: "center", marginBottom: "8px" }}>
        <Box sx={{ fontWeight: "bold", mr: 1 }}>Información de Simulación</Box>
        <Switch
          checked={showLog}
          onChange={() => setShowLog((prev) => !prev)}
          inputProps={{ "aria-label": "Mostrar log de eventos" }}
        />
        <Typography variant="body2" sx={{ ml: 1 }}>
          {showLog ? "Log de eventos" : "Extra Info"}
        </Typography>
      </Box>
      {showLog ? (
        <Paper
          ref={scrollRef}
          sx={{
            overflow: "auto",
            padding: "12px",
            height: "100%",
            background: "#f7fafc",
            borderRadius: "10px",
            boxShadow: 2,
            border: "1px solid #e0e0e0",
            marginBottom: "10px",
          }}
          onScroll={handleScroll}
        >
          {snapshotsEventos.length !== 0 ? (
            snapshotsEventos?.flat().map((item: any, idx: any) => (
              <Grow in timeout={400} key={idx}>
                <Box
                  key={idx}
                  sx={{
                    mb: 1.5,
                    p: 1.5,
                    background: "#fff",
                    borderRadius: "8px",
                    boxShadow: 1,
                    display: "flex",
                    alignItems: "center",
                    gap: 1,
                  }}
                >
                  <MensajeLog
                    evento={item.evento}
                    fechaHoraActual={item.fechaHoraActual}
                  />
                </Box>
              </Grow>
            ))
          ) : (
            <Typography sx={{ alignSelf: "center", justifySelf: "center" }}>
              No hay ningún evento aún.
            </Typography>
          )}
        </Paper>
      ) : (
        <ExtraInfoSimulacion snapshot={currentSnapshot} />
      )}
      <ModalIniciarSimulacion
        open={modalOpen}
        onClose={handleCloseModal}
        onAceptar={handleAceptarModal}
      />
    </>
  );
};

export default React.memo(LogEventos);
