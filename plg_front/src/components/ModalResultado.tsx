import {
  Box,
  Chip,
  Divider,
  IconButton,
  Modal,
  Typography,
} from "@mui/material";
import ClearIcon from "@mui/icons-material/Clear";
import {
  formatDiferenciaHoras,
  formatFechaHoraVerbose,
} from "../utils/utilsMethods";

const ModalResultado = ({ open, resultado, onClose }) => {
  return (
    <Modal
      open={open}
      onClose={onClose}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box
        sx={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: 420,
          bgcolor: "background.paper",
          borderRadius: 3,
          boxShadow: 24,
          p: 4,
          outline: "none",
        }}
      >
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            mb: 2,
            justifyContent: "space-between",
          }}
        >
          <Typography variant="h6" component="h2" sx={{ fontWeight: "bold" }}>
            Simulación Finalizada
          </Typography>
          {/* Close button */}
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
            onClick={onClose}
          >
            <ClearIcon sx={{ fontSize: 20 }} />
          </IconButton>
        </Box>

        <Divider sx={{ mb: 2 }} />

        <Box sx={{ mb: 1 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
            Finalización:
          </Typography>
          <Chip
            label={resultado?.tipoFin ? "Exitosa" : "Colapso Logístico"}
            color={resultado?.tipoFin ? "success" : "error"}
            sx={{ fontWeight: "bold", fontSize: 16, mr: 2, px: 2, py: 1 }}
          />
          <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
            Cantidad de pedidos entregados:
          </Typography>
          <Typography variant="body1" color="primary" sx={{ fontSize: 16 }}>
            {resultado?.cantidadPedidosEntregados}
          </Typography>
        </Box>
        <Box sx={{ mb: 1 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
            Fecha de fin de simulación:
          </Typography>
          <Typography variant="body1">
            {formatFechaHoraVerbose(resultado?.fechaFin)}
          </Typography>
        </Box>
        <Box sx={{ mb: 1 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
            Duración de simulación (virtual):
          </Typography>
          <Typography variant="body1">
            {formatDiferenciaHoras(resultado?.tiempoSimulado)}
          </Typography>
        </Box>
      </Box>
    </Modal>
  );
};

export default ModalResultado;
