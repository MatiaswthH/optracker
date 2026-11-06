import { API_ORIGIN } from "../config";
import {
  Box,
  Button,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Tab,
  Tabs,
  Typography,
  type SelectChangeEvent,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useRef, useState } from "react";
import { esES } from "@mui/x-data-grid/locales";
import {
  formatDiferenciaHoras,
  formatFechaHoraVerbose,
} from "../utils/utilsMethods";
import ScheduleSendIcon from "@mui/icons-material/ScheduleSend";
import FileUploadIcon from "@mui/icons-material/FileUpload";
import AddIcon from "@mui/icons-material/Add";
import ModalPedidosDiarios from "./ModalPedidosDiarios";
import { ModalRegistroPedidoIndividual } from "./ModalRegistroPedidoIndividual";

const ExtraInfoSimulacion = ({ snapshot }) => {
  const [value, setValue] = useState("Camiones");

  //Envio de averias
  const [tipoAvería, setTipoAvería] = useState("");
  const [camionAveria, setCamionAveria] = useState("");
  const averias = [
    { id: "1", nombre: "T1: Se baja la llanta." },
    { id: "2", nombre: "T2: Se obstruye el motor." },
    { id: "3", nombre: "T3: Se choca la unidad." },
  ];

  //Pedidos diarios
  const [archivo, setArchivo] = useState("");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Modal para pedido individual
  const [modalPedidoOpen, setModalPedidoOpen] = useState(false);

  const handleFileButtonClick = () => {
    fileInputRef.current?.click();
  };

  const handleIconUploadClick = async () => {
    if (!selectedFile) {
      alert("Primero selecciona un archivo.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);
    formData.append(
      "fechaPedido",
      snapshot?.fechaHoraActual ?? "AUTO"
    );

    try {
      const response = await fetch(
        `${API_ORIGIN}/diarioPedidos/archivo-pedidos`,
        {
          method: "POST",
          body: formData,
        }
      );
      if (response.ok) {
        alert("Archivo enviado correctamente");
        setArchivo("");
        setSelectedFile(null);
        if (fileInputRef.current) fileInputRef.current.value = "";
      } else {
        alert("Error al enviar archivo");
      }
    } catch (error) {
      alert("Error de red al enviar archivo");
    }
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      const file = event.target.files[0];
      setArchivo(file.name);
      setSelectedFile(file);
    }
  };

  const handleRegistrarPedidoIndividual = async (lineaPedido: string) => {
    try {
      // Crear un archivo con la línea del pedido
      const blob = new Blob([lineaPedido], { type: 'text/plain' });
      const file = new File([blob], 'pedido_individual.txt', { type: 'text/plain' });

      const formData = new FormData();
      formData.append("file", file);
      formData.append(
        "fechaPedido",
        snapshot?.fechaHoraActual ?? "AUTO"
      );

      const response = await fetch(
        `${API_ORIGIN}/diarioPedidos/archivo-pedidos`,
        {
          method: "POST",
          body: formData,
        }
      );

      if (response.ok) {
        alert("Pedido registrado correctamente");
      } else {
        alert("Error al registrar pedido");
      }
    } catch (error) {
      alert("Error de red al registrar pedido");
    }
  };

  //Selects averia
  const handleChangeAveria = (event: SelectChangeEvent) => {
    setTipoAvería(event.target.value as string);
  };

  const handleChangeCamionAveria = (event: SelectChangeEvent) => {
    setCamionAveria(event.target.value as string);
  };

  const handleUploadAveria = async () => {
    if (!camionAveria || !tipoAvería) {
      alert("Selecciona el camión y el tipo de avería.");
      return;
    }
    try {
      const response = await fetch(
        `${API_ORIGIN}/diarioAverias/averia`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            codigo: camionAveria,
            tipoAveria: tipoAvería,
          }),
        }
      );
      if (response.ok) {
        alert("Avería registrada correctamente");
        setCamionAveria("");
        setTipoAvería("");
      } else {
        alert("Error al registrar avería");
      }
    } catch (error) {
      alert("Error de red al registrar avería");
    }
  };

  // Datos para el datagrid
  const camiones = snapshot?.camiones ?? [];
  const pedidos = snapshot?.pedidos ?? [];
  const bloqueos = snapshot?.bloqueos ?? [];
  const almacenes = snapshot?.almacenes ?? [];

  // Filas del datagrid
  const columnsCamiones: GridColDef<any>[] = [
    { field: "codigo", headerName: "Cod.", width: 75 },
    {
      field: "carga",
      headerName: "Carga GLP",
      width: 100,
      valueGetter: (_, row) => {
        const c = row;
        if (!c) return "";
        if (c.principal) return "Infinito";
        return `${c.cargaGLP} (${((c.cargaGLP / c.capacidad) * 100).toFixed(
          1
        )}%)`;
      },
    },
    {
      field: "coords",
      headerName: "Coord. (X, Y)",
      width: 100,
      valueGetter: (_, row) => {
        const c = row;
        if (!c) return "";
        if (c.nodoActual.x === undefined || c.nodoActual.y === undefined)
          return "";
        return `${c.nodoActual.x}, ${c.nodoActual.y}`;
      },
    },
    {
      field: "estado",
      headerName: "Estado",
      width: 150,
      valueGetter: (_, row) => {
        const c = row;
        if (!c) return "Desconocido";
        if (c.enMantenimiento) return "En Mantenimiento";
        if (c.saliendo) return "Saliendo";
        if (c.despachando) return "Despachando";
        if (c.trasvasando) return "Trasvasando";
        if (c.inmovilizado) return "Inmovilizado";
        if (c.enRuta) return "En Ruta";
        return "Desocupado";
      },
    },
    {
      field: "cantidadPedidos",
      headerName: "Cant. Pedidos",
      width: 100,
    },
    {
      field: "idPedidos",
      headerName: "Id de Pedidos",
      minWidth: 150,
      valueGetter: (_, row) => {
        const camion = row;
        if (!camion.listaPedidos || camion.listaPedidos.length === 0)
          return "Sin pedidos";
        return camion.listaPedidos.join(", ");
      },
    },
    {
      field: "paradas",
      headerName: "Paradas",
      minWidth: 250,
      valueGetter: (_, row) => {
        const camion = row;
        if (!camion.paradas || camion.paradas.length === 0)
          return "Sin ruta asignada.";
        return camion.paradas.map((p) => `(${p.x},${p.y})`).join(", ");
      },
    },
  ];

  const columnsPedidos: GridColDef<any>[] = [
    {
      field: "id",
      headerName: "Id",
      width: 75,
      valueGetter: (_, row) => {
        const p = row;
        if (!p) return "";
        if (p.childId !== undefined) {
          return `${p.id}-${p.childId}`;
        }
        return `${p.id}`;
      },
    },
    {
      field: "volumen",
      headerName: "Volumen",
      width: 90,
    },
    {
      field: "clienteId",
      headerName: "Cliente",
      width: 70,
    },
    {
      field: "coords",
      headerName: "Coord. (X, Y)",
      width: 100,
      valueGetter: (_, row) => {
        const p = row;
        if (!p) return "";
        if (p.x === undefined || p.y === undefined) return "";
        return `${p.x}, ${p.y}`;
      },
    },
    {
      field: "fechaLimiteEntrega",
      headerName: "Deadline",
      width: 150,
      valueGetter: (_, row) => {
        const p = row;
        if (!p) return "";
        return formatFechaHoraVerbose(p.fechaLimiteEntrega);
      },
    },
    {
      field: "camionAsignado",
      headerName: "Camión Asignado",
      width: 150,
      valueGetter: (_, row) => {
        const p = row;
        if (!p?.camionAsignado) return "Sin asignar";
        return `${p.camionAsignado}`;
      },
    },
  ];

  const columnsBloqueos: GridColDef<any>[] = [
    { field: "id", headerName: "Id", width: 75 },
    {
      field: "tramos",
      headerName: "Tramos",
      minWidth: 250,
      valueGetter: (_, row) => {
        const bloqueo = row;
        if (!bloqueo.nodosIntermedios || bloqueo.nodosIntermedios.length === 0)
          return "Sin tramo asignado.";
        return bloqueo.nodosIntermedios
          .slice(1)
          .map((b) => `(${b.x},${b.y})`)
          .join(", ");
      },
    },
    {
      field: "tiempoRestante",
      headerName: "Tiempo Restante",
      width: 180,
      valueGetter: (_, row) => {
        const b = row;
        if (!b) return "";
        return formatDiferenciaHoras(b.tiempoRestante);
      },
    },
    {
      field: "fechaFin",
      headerName: "Fecha Fin",
      width: 180,
      valueGetter: (_, row) => {
        const b = row;
        if (!b) return "";
        return formatFechaHoraVerbose(b.fechaFin);
      },
    },
  ];

  const columnsAlmacenes: GridColDef<any>[] = [
    { field: "id", headerName: "Id", width: 75 },
    {
      field: "principal",
      headerName: "Tipo",
      width: 100,
      valueGetter: (_, row) => {
        const a = row;
        if (!a) return "";
        if (a.principal) return "Principal";
        return "Secundario";
      },
    },
    {
      field: "stock",
      headerName: "Stock GLP",
      width: 150,
      valueGetter: (_, row) => {
        const a = row;
        if (!a) return "";
        if (a.principal) return "Infinito";
        return `${a.cargaGLP} (${((a.cargaGLP / a.capacidad) * 100).toFixed(
          1
        )}%)`;
      },
    },
    {
      field: "camionesEnAlmacen",
      headerName: "Camiones en Almacen",
      minWidth: 500,
      valueGetter: (_, row) => {
        const a = row;
        if (!a) return "";
        return a.camionesEnAlmacen.length > 0
          ? `${a.cantCamionesEnAlmacen} - (${a.camionesEnAlmacen.join(", ")})`
          : "Sin camiones";
      },
    },
  ];

  return (
    <Box sx={{ overflow: "hidden", height: "100%", paddingBottom: "10px" }}>
      <Tabs
        value={value}
        onChange={(e, newValue) => setValue(newValue)}
        variant="scrollable"
        scrollButtons="auto"
        sx={{ overflow: "auto" }}
      >
        <Tab label="Registro" value="Registro" />
        <Tab label="Camiones" value="Camiones" />
        <Tab label="Pedidos" value="Pedidos" />
        <Tab label="Bloqueos" value="Bloqueos" />
        <Tab label="Almacenes" value="Almacenes" />
      </Tabs>
      {value === "Camiones" ? (
        <DataGrid
          localeText={esES.components.MuiDataGrid.defaultProps.localeText}
          disableMultipleRowSelection
          disableRowSelectionOnClick
          hideFooterSelectedRowCount
          //hideFooter
          hideFooterPagination
          columns={columnsCamiones}
          rows={camiones.map((c, idx) => ({ id: c.codigo ?? idx, ...c }))}
          sx={{
            "& .MuiDataGrid-columnHeaderCheckbox .MuiDataGrid-columnHeaderTitleContainer":
            {
              display: "none",
            },
          }}
        />
      ) : value === "Pedidos" ? (
        <DataGrid
          localeText={esES.components.MuiDataGrid.defaultProps.localeText}
          disableMultipleRowSelection
          disableRowSelectionOnClick
          hideFooterSelectedRowCount
          //hideFooter
          hideFooterPagination
          columns={columnsPedidos}
          rows={pedidos}
          getRowId={(row) =>
            row.id + (row.childId !== undefined ? `-${row.childId}` : "")
          }
          sx={{
            "& .MuiDataGrid-columnHeaderCheckbox .MuiDataGrid-columnHeaderTitleContainer":
            {
              display: "none",
            },
          }}
        />
      ) : value === "Bloqueos" ? (
        <DataGrid
          localeText={esES.components.MuiDataGrid.defaultProps.localeText}
          disableMultipleRowSelection
          disableRowSelectionOnClick
          hideFooterSelectedRowCount
          //hideFooter
          hideFooterPagination
          columns={columnsBloqueos}
          rows={bloqueos}
          getRowId={(row) =>
            row.id + (row.childId !== undefined ? `-${row.childId}` : "")
          }
          sx={{
            "& .MuiDataGrid-columnHeaderCheckbox .MuiDataGrid-columnHeaderTitleContainer":
            {
              display: "none",
            },
          }}
        />
      ) : value === "Almacenes" ? (
        <DataGrid
          localeText={esES.components.MuiDataGrid.defaultProps.localeText}
          disableMultipleRowSelection
          disableRowSelectionOnClick
          hideFooterSelectedRowCount
          //hideFooter
          hideFooterPagination
          columns={columnsAlmacenes}
          rows={almacenes}
          getRowId={(row) =>
            row.id + (row.childId !== undefined ? `-${row.childId}` : "")
          }
          sx={{
            "& .MuiDataGrid-columnHeaderCheckbox .MuiDataGrid-columnHeaderTitleContainer":
            {
              display: "none",
            },
          }}
        />
      ) : (
        <Box sx={{ padding: "5px" }}>
          <Paper sx={{ padding: "10px", marginBottom: "10px" }}>
            <Stack
              flexDirection="row"
              justifyContent="space-between"
              height="30px"
            >
              <Typography fontWeight="bold">Registrar Pedidos</Typography>
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
                disabled={!selectedFile}
                onClick={handleIconUploadClick}
              >
                <FileUploadIcon sx={{ fontSize: 28 }} />
              </IconButton>
            </Stack>
            <Stack flexDirection="row" gap="10px">
              <Button
                sx={{ width: "150px" }}
                variant="outlined"
                onClick={handleFileButtonClick}
              >
                Elegir Archivo
              </Button>
              <input
                type="file"
                ref={fileInputRef}
                style={{ display: "none" }}
                onChange={handleFileChange}
              />
              <Typography overflow="hidden">
                {archivo === ""
                  ? "No se ha seleccionado archivo."
                  : `${archivo}`}
              </Typography>
            </Stack>
            <Stack flexDirection="row" gap="10px" sx={{ marginTop: "10px" }}>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => setModalPedidoOpen(true)}
                sx={{ width: "150px" }}
              >
                Registrar Uno
              </Button>
            </Stack>
          </Paper>
          <Paper sx={{ padding: "10px" }}>
            <Stack
              flexDirection="row"
              justifyContent="space-between"
              height="30px"
            >
              <Typography fontWeight="bold">Registrar Avería</Typography>
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
                disabled={
                  camionAveria === "" || tipoAvería === ""
                }
                onClick={handleUploadAveria}
              >
                <ScheduleSendIcon sx={{ fontSize: 28 }} />
              </IconButton>
            </Stack>
            <Stack
              sx={{
                gap: "10px",
                flexDirection: "row",
                alignContent: "center",
                margin: "10px",
              }}
            >
              <FormControl sx={{ width: "200px" }}>
                <InputLabel>Código de Camión</InputLabel>
                <Select
                  value={camionAveria}
                  label="Código de Camión"
                  onChange={handleChangeCamionAveria}
                >
                  {camiones
                    .filter((c) => c.enRuta)
                    .map((c) => (
                      <MenuItem value={c.codigo}>{c.codigo}</MenuItem>
                    ))}
                </Select>
              </FormControl>
              <FormControl sx={{ width: "170px" }}>
                <InputLabel>Tipo Avería</InputLabel>
                <Select
                  value={tipoAvería}
                  label="Tipo Avería"
                  onChange={handleChangeAveria}
                >
                  {averias.map((a) => (
                    <MenuItem key={a.id} value={a.id}>
                      {a.nombre}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Stack>
          </Paper>
        </Box>
      )}

      <ModalRegistroPedidoIndividual
        open={modalPedidoOpen}
        onClose={() => setModalPedidoOpen(false)}
        onRegistrar={handleRegistrarPedidoIndividual}
      />
    </Box>
  );
};

export default ExtraInfoSimulacion;
