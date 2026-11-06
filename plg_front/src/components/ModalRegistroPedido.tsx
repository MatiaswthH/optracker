import { Modal, Box, Typography, TextField, Button, Grid, Chip } from '@mui/material';
import { useState } from 'react';
import type { Pedido } from '../types';

interface Props {
    open: boolean;
    onClose: () => void;
    onGuardar: (pedido: Omit<Pedido, 'id'>) => void;
}

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

export const ModalRegistroPedido = ({ open, onClose, onGuardar }: Props) => {
    const [dia, setDia] = useState(1);
    const [hora, setHora] = useState(0);
    const [minuto, setMinuto] = useState(0);
    const [posX, setPosX] = useState(0);
    const [posY, setPosY] = useState(0);
    const [clienteId, setClienteId] = useState('');
    const [cantidadM3, setCantidadM3] = useState(1);
    const [horasLimite, setHorasLimite] = useState(4);

    const handleGuardar = () => {
        const fechaHora = `${String(dia).padStart(2, '0')}d${String(hora).padStart(2, '0')}h${String(minuto).padStart(2, '0')}m`;

        onGuardar({
            dia,
            hora,
            minuto,
            posX,
            posY,
            clienteId: clienteId.startsWith('c-') ? clienteId : `c-${clienteId}`,
            cantidadM3,
            horasLimite,
            fechaHora,
            estado: 'PENDIENTE'
        });
        onClose();
        // Reset form
        setDia(1);
        setHora(0);
        setMinuto(0);
        setPosX(0);
        setPosY(0);
        setClienteId('');
        setCantidadM3(1);
        setHorasLimite(4);
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={style}>
                <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
                    Registrar Nuevo Pedido
                </Typography>

                <Grid container spacing={2}>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Día"
                            type="number"
                            value={dia}
                            onChange={(e) => setDia(Number(e.target.value))}
                            inputProps={{ min: 1, max: 31 }}
                            helperText="1-31"
                        />
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Hora"
                            type="number"
                            value={hora}
                            onChange={(e) => setHora(Number(e.target.value))}
                            inputProps={{ min: 0, max: 23 }}
                            helperText="0-23"
                        />
                    </Grid>
                    <Grid item xs={4}>
                        <TextField
                            fullWidth
                            label="Minuto"
                            type="number"
                            value={minuto}
                            onChange={(e) => setMinuto(Number(e.target.value))}
                            inputProps={{ min: 0, max: 59 }}
                            helperText="0-59"
                        />
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Posición X"
                            type="number"
                            value={posX}
                            onChange={(e) => setPosX(Number(e.target.value))}
                            inputProps={{ min: 0, max: 70 }}
                            helperText="0-70 Km"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Posición Y"
                            type="number"
                            value={posY}
                            onChange={(e) => setPosY(Number(e.target.value))}
                            inputProps={{ min: 0, max: 50 }}
                            helperText="0-50 Km"
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="ID Cliente"
                            value={clienteId}
                            onChange={(e) => setClienteId(e.target.value)}
                            placeholder="167 (se agregará 'c-' automáticamente)"
                            helperText="Ingrese solo el número del cliente"
                        />
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Cantidad GLP (m³)"
                            type="number"
                            value={cantidadM3}
                            onChange={(e) => setCantidadM3(Number(e.target.value))}
                            inputProps={{ min: 1, max: 200 }}
                            helperText="Entero"
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
                            helperText="Mín. 4h"
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <Box sx={{ p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                            <Typography variant="subtitle2" gutterBottom>
                                Vista previa del registro:
                            </Typography>
                            <Typography variant="body2" fontFamily="monospace">
                                {String(dia).padStart(2, '0')}d{String(hora).padStart(2, '0')}h{String(minuto).padStart(2, '0')}m:{posX},{posY},c-{clienteId},{cantidadM3}m3,{horasLimite}h
                            </Typography>
                        </Box>
                    </Grid>
                </Grid>

                <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                    <Button onClick={onClose} sx={{ mr: 1 }}>
                        Cancelar
                    </Button>
                    <Button variant="contained" onClick={handleGuardar}>
                        Registrar Pedido
                    </Button>
                </Box>
            </Box>
        </Modal>
    );
};
