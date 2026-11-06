import { Modal, Box, Typography, TextField, Button, Select, MenuItem, InputLabel, FormControl, Grid } from '@mui/material';
import { useState } from 'react';
import type { Averia, TipoIncidente } from '../types';
import { TiposIncidente } from '../types';

interface Props {
    open: boolean;
    onClose: () => void;
    onGuardar: (averia: Omit<Averia, 'id'>) => void;
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

const turnos = ['T1', 'T2', 'T3'];

const tiemposIncidente = {
    TI1: { inmovilizacion: 2, taller: 0 },
    TI2: { inmovilizacion: 2, taller: 8 }, // 1 turno
    TI3: { inmovilizacion: 4, taller: 72 }, // 3 días
};

const descripcionesIncidente = {
    TI1: 'Se baja la llanta - reparable por conductor',
    TI2: 'Se ahoga (obstruye) el motor',
    TI3: 'Se choca la unidad',
};

export const ModalRegistroAveria = ({ open, onClose, onGuardar }: Props) => {
    const [camionId, setCamionId] = useState('');
    const [turno, setTurno] = useState('T1');
    const [tipoIncidente, setTipoIncidente] = useState<TipoIncidente>('TI1');
    const [nodoX, setNodoX] = useState(0);
    const [nodoY, setNodoY] = useState(0);
    const [descripcionPersonalizada, setDescripcionPersonalizada] = useState('');

    const handleGuardar = () => {
        const tiempos = tiemposIncidente[tipoIncidente];
        const descripcionBase = descripcionesIncidente[tipoIncidente];
        const descripcionFinal = descripcionPersonalizada || descripcionBase;

        onGuardar({
            camionId,
            turno,
            tipoIncidente,
            nodoX,
            nodoY,
            descripcion: descripcionFinal,
            fechaHora: new Date().toISOString(),
            tiempoInmovilizacion: tiempos.inmovilizacion,
            tiempoTaller: tiempos.taller
        });
        onClose();
        // Reset form
        setCamionId('');
        setTurno('T1');
        setTipoIncidente('TI1');
        setNodoX(0);
        setNodoY(0);
        setDescripcionPersonalizada('');
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={style}>
                <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
                    Registrar Incidente/Avería
                </Typography>

                <Grid container spacing={2}>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="ID Camión (ej: TA01)"
                            value={camionId}
                            onChange={(e) => setCamionId(e.target.value)}
                            placeholder="TA01, TB02, etc."
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <FormControl fullWidth>
                            <InputLabel>Turno</InputLabel>
                            <Select
                                value={turno}
                                label="Turno"
                                onChange={(e) => setTurno(e.target.value)}
                            >
                                {turnos.map(t => (
                                    <MenuItem key={t} value={t}>
                                        {t} ({t === 'T1' ? '00:00-08:00' : t === 'T2' ? '08:00-16:00' : '16:00-24:00'})
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>

                    <Grid item xs={12}>
                        <FormControl fullWidth>
                            <InputLabel>Tipo de Incidente</InputLabel>
                            <Select
                                value={tipoIncidente}
                                label="Tipo de Incidente"
                                onChange={(e) => setTipoIncidente(e.target.value as TipoIncidente)}
                            >
                                {TiposIncidente.map(t => (
                                    <MenuItem key={t} value={t}>
                                        {t} - {descripcionesIncidente[t]}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Nodo X (0-70)"
                            type="number"
                            value={nodoX}
                            onChange={(e) => setNodoX(Number(e.target.value))}
                            inputProps={{ min: 0, max: 70 }}
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Nodo Y (0-50)"
                            type="number"
                            value={nodoY}
                            onChange={(e) => setNodoY(Number(e.target.value))}
                            inputProps={{ min: 0, max: 50 }}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="Descripción adicional (opcional)"
                            value={descripcionPersonalizada}
                            onChange={(e) => setDescripcionPersonalizada(e.target.value)}
                            multiline
                            rows={2}
                            placeholder={descripcionesIncidente[tipoIncidente]}
                        />
                    </Grid>

                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Tiempo Inmovilización"
                            value={`${tiemposIncidente[tipoIncidente].inmovilizacion}h`}
                            disabled
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Tiempo en Taller"
                            value={`${tiemposIncidente[tipoIncidente].taller}h`}
                            disabled
                        />
                    </Grid>
                </Grid>

                <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                    <Button onClick={onClose} sx={{ mr: 1 }}>
                        Cancelar
                    </Button>
                    <Button variant="contained" onClick={handleGuardar}>
                        Registrar Incidente
                    </Button>
                </Box>
            </Box>
        </Modal>
    );
};
