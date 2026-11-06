import React, { useState } from 'react';
import {
    Modal,
    Box,
    Typography,
    TextField,
    Button,
    Grid,
    Paper,
} from '@mui/material';

const style = {
    position: 'absolute' as 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 500,
    bgcolor: 'background.paper',
    boxShadow: 24,
    p: 4,
    borderRadius: 2,
};

interface Props {
    open: boolean;
    onClose: () => void;
    onRegistrar: (lineaPedido: string) => void;
}

export const ModalRegistroPedidoIndividual: React.FC<Props> = ({
    open,
    onClose,
    onRegistrar,
}) => {
    const [dias, setDias] = useState(1);
    const [horas, setHoras] = useState(0);
    const [minutos, setMinutos] = useState(0);
    const [x, setX] = useState(0);
    const [y, setY] = useState(0);
    const [idCliente, setIdCliente] = useState('');
    const [volumen, setVolumen] = useState(1);
    const [horasLimite, setHorasLimite] = useState(4);

    const handleRegistrar = () => {
        // Validaciones básicas
        if (idCliente.trim() === '') {
            alert('El ID del cliente es requerido');
            return;
        }

        // Construir la línea del pedido
        const diasFormatted = String(dias).padStart(2, '0');
        const horasFormatted = String(horas).padStart(2, '0');
        const minutosFormatted = String(minutos).padStart(2, '0');

        const lineaPedido = `${diasFormatted}d${horasFormatted}h${minutosFormatted}m:${x},${y},c-${idCliente},${volumen}m3,${horasLimite}h`;

        onRegistrar(lineaPedido);

        // Reset form
        setDias(1);
        setHoras(0);
        setMinutos(0);
        setX(0);
        setY(0);
        setIdCliente('');
        setVolumen(1);
        setHorasLimite(4);

        onClose();
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={style}>
                <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
                    Registrar Pedido Individual
                </Typography>

                <Grid container spacing={2}>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Días"
                            type="number"
                            value={dias}
                            onChange={(e) => setDias(Number(e.target.value))}
                            inputProps={{ min: 1, max: 31 }}
                            helperText="1-31"
                        />
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Horas"
                            type="number"
                            value={horas}
                            onChange={(e) => setHoras(Number(e.target.value))}
                            inputProps={{ min: 0, max: 23 }}
                            helperText="0-23"
                        />
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Minutos"
                            type="number"
                            value={minutos}
                            onChange={(e) => setMinutos(Number(e.target.value))}
                            inputProps={{ min: 0, max: 59 }}
                            helperText="0-59"
                        />
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Posición X"
                            type="number"
                            value={x}
                            onChange={(e) => setX(Number(e.target.value))}
                            inputProps={{ min: 0 }}
                            helperText="Coordenada X"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Posición Y"
                            type="number"
                            value={y}
                            onChange={(e) => setY(Number(e.target.value))}
                            inputProps={{ min: 0 }}
                            helperText="Coordenada Y"
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="ID Cliente"
                            value={idCliente}
                            onChange={(e) => setIdCliente(e.target.value.replace(/\D/g, ''))}
                            placeholder="198"
                            helperText="Solo números (se agregará automáticamente el prefijo 'c-')"
                        />
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Volumen (m³)"
                            type="number"
                            value={volumen}
                            onChange={(e) => setVolumen(Number(e.target.value))}
                            inputProps={{ min: 1 }}
                            helperText="Volumen en m³"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Horas Límite"
                            type="number"
                            value={horasLimite}
                            onChange={(e) => setHorasLimite(Number(e.target.value))}
                            inputProps={{ min: 1 }}
                            helperText="Límite en horas"
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <Paper sx={{ p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                            <Typography variant="subtitle2" gutterBottom>
                                Vista previa del formato:
                            </Typography>
                            <Typography variant="body2" fontFamily="monospace">
                                {String(dias).padStart(2, '0')}d{String(horas).padStart(2, '0')}h{String(minutos).padStart(2, '0')}m:{x},{y},c-{idCliente || 'XXX'},{volumen}m3,{horasLimite}h
                            </Typography>
                        </Paper>
                    </Grid>
                </Grid>

                <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                    <Button onClick={onClose} sx={{ mr: 1 }}>
                        Cancelar
                    </Button>
                    <Button variant="contained" onClick={handleRegistrar}>
                        Registrar Pedido
                    </Button>
                </Box>
            </Box>
        </Modal>
    );
};