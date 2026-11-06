import React, { useState } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    Box,
    Typography,
    Paper,
    Stack,
    CircularProgress,
    Alert,
} from "@mui/material";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import { useEnviarPedidos } from "../hooks/useEnviarPedidos";
import { useEnviarMantenimientos } from "../hooks/useEnviarMantenimientos";
import { useEnviarBloqueos } from "../hooks/useEnviarBloqueos";

interface ModalIniciarSimulacionProps {
    open: boolean;
    onClose: () => void;
    onAceptar: () => void;
}

const ModalIniciarSimulacion: React.FC<ModalIniciarSimulacionProps> = ({
    open,
    onClose,
    onAceptar,
}) => {
    const [mantenimientosFile, setMantenimientosFile] = useState<File | null>(null);
    const [bloqueosFile, setBloqueosFile] = useState<File | null>(null);
    const [pedidosFile, setPedidosFile] = useState<File | null>(null);

    // Initialize hooks
    const { loading: loadingPedidos, error: errorPedidos, success: successPedidos, enviarPedidos } = useEnviarPedidos();
    const { loading: loadingMantenimientos, error: errorMantenimientos, success: successMantenimientos, enviarMantenimientos } = useEnviarMantenimientos();
    const { loading: loadingBloqueos, error: errorBloqueos, success: successBloqueos, enviarBloqueos } = useEnviarBloqueos();

    const isLoading = loadingPedidos || loadingMantenimientos || loadingBloqueos;
    const hasErrors = errorPedidos || errorMantenimientos || errorBloqueos;
    const allSuccess = successPedidos && successMantenimientos && successBloqueos;

    const handleAceptar = async () => {
        try {
            const promises = [];

            if (pedidosFile) {
                promises.push(enviarPedidos(pedidosFile));
            }

            if (mantenimientosFile) {
                promises.push(enviarMantenimientos(mantenimientosFile));
            }

            if (bloqueosFile) {
                promises.push(enviarBloqueos(bloqueosFile));
            }

            if (promises.length > 0) {
                await Promise.all(promises);

                // If all uploads were successful, proceed with the original onAceptar
                if (!hasErrors) {
                    onAceptar();
                }
            } else {
                // If no files selected, just proceed with original onAceptar
                onAceptar();
            }
        } catch (error) {
            console.error('Error uploading files:', error);
        }
    };

    const handleFileUpload = (
        event: React.ChangeEvent<HTMLInputElement>,
        setFile: (file: File | null) => void
    ) => {
        const file = event.target.files?.[0] || null;
        setFile(file);
    };

    const FileUploadSection = ({
        title,
        file,
        onFileChange,
        accept = "*/*",
    }: {
        title: string;
        file: File | null;
        onFileChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
        accept?: string;
    }) => (
        <Paper
            elevation={1}
            sx={{
                p: 2,
                border: "2px dashed #ccc",
                borderRadius: 2,
                textAlign: "center",
                minHeight: "120px",
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                cursor: "pointer",
                transition: "all 0.3s ease",
                "&:hover": {
                    borderColor: "primary.main",
                    backgroundColor: "rgba(25, 118, 210, 0.04)",
                },
            }}
        >
            <input
                type="file"
                accept={accept}
                onChange={onFileChange}
                style={{ display: "none" }}
                id={`file-upload-${title.replace(/\s+/g, "-").toLowerCase()}`}
            />
            <label htmlFor={`file-upload-${title.replace(/\s+/g, "-").toLowerCase()}`}>
                <Box
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        cursor: "pointer",
                    }}
                >
                    <CloudUploadIcon
                        sx={{
                            fontSize: 40,
                            color: file ? "success.main" : "grey.400",
                            mb: 1,
                        }}
                    />
                    <Typography variant="h6" sx={{ mb: 1, fontWeight: "bold" }}>
                        {title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        {file ? `Archivo: ${file.name}` : "Haz clic para seleccionar un archivo"}
                    </Typography>
                </Box>
            </label>
        </Paper>
    );
    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="sm"
            fullWidth
            PaperProps={{
                sx: {
                    borderRadius: 2,
                    padding: 2,
                },
            }}
        >
            <DialogTitle
                sx={{
                    textAlign: "center",
                    fontWeight: "bold",
                    paddingBottom: 2,
                }}
            >
                Iniciar Simulación
            </DialogTitle>
            <DialogContent>
                <Stack spacing={3} sx={{ minWidth: "400px" }}>
                    <FileUploadSection
                        title="Cargar Mantenimientos"
                        file={mantenimientosFile}
                        onFileChange={(e) => handleFileUpload(e, setMantenimientosFile)}
                        accept=".txt,.csv,.xlsx,.xls"
                    />
                    <FileUploadSection
                        title="Cargar Bloqueos"
                        file={bloqueosFile}
                        onFileChange={(e) => handleFileUpload(e, setBloqueosFile)}
                        accept=".txt,.csv,.xlsx,.xls"
                    />
                    <FileUploadSection
                        title="Cargar Pedidos"
                        file={pedidosFile}
                        onFileChange={(e) => handleFileUpload(e, setPedidosFile)}
                        accept=".txt,.csv,.xlsx,.xls"
                    />
                </Stack>
            </DialogContent>
            <DialogActions
                sx={{
                    justifyContent: "center",
                    paddingTop: 2,
                }}
            >
                {hasErrors && (
                    <Stack spacing={1} sx={{ width: "100%", mb: 2 }}>
                        {errorPedidos && (
                            <Alert severity="error">
                                Error en pedidos: {errorPedidos}
                            </Alert>
                        )}
                        {errorMantenimientos && (
                            <Alert severity="error">
                                Error en mantenimientos: {errorMantenimientos}
                            </Alert>
                        )}
                        {errorBloqueos && (
                            <Alert severity="error">
                                Error en bloqueos: {errorBloqueos}
                            </Alert>
                        )}
                    </Stack>
                )}
                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleAceptar}
                    disabled={isLoading}
                    sx={{
                        minWidth: "100px",
                        borderRadius: 2,
                        '&:hover': {
                            backgroundColor: 'primary.dark',
                        },
                    }}
                >
                    {isLoading ? (
                        <Stack direction="row" spacing={1} alignItems="center">
                            <CircularProgress size={20} color="inherit" />
                            <Typography variant="body2">Enviando...</Typography>
                        </Stack>
                    ) : (
                        "Aceptar"
                    )}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ModalIniciarSimulacion;
