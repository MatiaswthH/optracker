import { Modal, Box, Typography, TextField, Button, Select, MenuItem, InputLabel, FormControl, Grid, Switch, FormControlLabel } from '@mui/material';
import { useState } from 'react';
import type { Mantenimiento, TipoMantenimiento } from '../types';
import { TiposMantenimiento } from '../types';

interface Props {
    open: boolean;
    onClose: () => void;
    onGuardar: (mantenimiento: Omit<Mantenimiento, 'id'>) => void;
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

export const ModalRegistroMantenimiento = ({ open, onClose, onGuardar }: Props) => {
    const [camionId, setCamionId] = useState('');
    const [fecha, setFecha] = useState('');
    const [tipo, setTipo] = useState<TipoMantenimiento>('Preventivo');
    const [descripcion, setDescripcion] = useState('');
    const [programado, setProgramado] = useState(false);

    const handleGuardar = () => {
        // Convertir fecha YYYY-MM-DD a formato AAAAMMDD
        const fechaFormateada = fecha.replace(/-/g, '');

        onGuardar({
            camionId,
            fecha: fechaFormateada,
            tipo,
            descripcion: descripcion || getDescripcionPorDefecto(tipo),
            programado
        });
        onClose();
        // Reset form
        setCamionId('');
        setFecha('');
        setTipo('Preventivo');
        setDescripcion('');
        setProgramado(false);
    };

    const getDescripcionPorDefecto = (tipoMantenimiento: TipoMantenimiento) => {
        switch (tipoMantenimiento) {
            case 'Preventivo':
                return 'Mantenimiento preventivo programado - Revisión general, cambio de filtros y aceite';
            case 'Correctivo':
                return 'Mantenimiento correctivo - Reparación de componentes defectuosos';
            default:
                return '';
        }
    };

    const formatearFechaParaVisualizacion = () => {
        if (!fecha) return '';
        const fechaFormateada = fecha.replace(/-/g, '');
        return `Formato archivo: ${fechaFormateada}:${camionId}`;
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={style}>
                <Typography variant="h6" component="h2" sx={{ mb: 2 }}>
                    Programar Mantenimiento
                </Typography>

                <Grid container spacing={2}>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="ID Camión"
                            value={camionId}
                            onChange={(e) => setCamionId(e.target.value.toUpperCase())}
                            placeholder="TA01, TB02, TC03, TD04"
                            helperText="Formato: TTNN (ej: TA01)"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            fullWidth
                            label="Fecha"
                            type="date"
                            value={fecha}
                            onChange={(e) => setFecha(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            helperText="Fecha del mantenimiento"
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <FormControl fullWidth>
                            <InputLabel>Tipo de Mantenimiento</InputLabel>
                            <Select
                                value={tipo}
                                label="Tipo de Mantenimiento"
                                onChange={(e) => setTipo(e.target.value as TipoMantenimiento)}
                            >
                                {TiposMantenimiento.map(t => (
                                    <MenuItem key={t} value={t}>
                                        {t}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>

                    <Grid item xs={12}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={programado}
                                    onChange={(e) => setProgramado(e.target.checked)}
                                />
                            }
                            label="Mantenimiento del plan bimensual"
                        />
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            El plan se repite cada 2 meses. Si está activado, será parte del plan preventivo oficial.
                        </Typography>
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="Descripción"
                            value={descripcion}
                            onChange={(e) => setDescripcion(e.target.value)}
                            multiline
                            rows={3}
                            placeholder={getDescripcionPorDefecto(tipo)}
                            helperText="Descripción detallada del mantenimiento a realizar"
                        />
                    </Grid>

                    {fecha && camionId && (
                        <Grid item xs={12}>
                            <Box sx={{ p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                                <Typography variant="subtitle2" gutterBottom>
                                    Formato para archivo de mantenimiento:
                                </Typography>
                                <Typography variant="body2" fontFamily="monospace">
                                    {formatearFechaParaVisualizacion()}
                                </Typography>
                            </Box>
                        </Grid>
                    )}

                    <Grid item xs={12}>
                        <Box sx={{ p: 2, bgcolor: 'warning.light', borderRadius: 1 }}>
                            <Typography variant="body2" color="warning.contrastText">
                                <strong>Importante:</strong> Un camión en mantenimiento NO estará disponible
                                desde las 00:00 hasta las 23:59 del día programado. Si está en ruta al inicio
                                del mantenimiento, deberá regresar inmediatamente a la planta.
                            </Typography>
                        </Box>
                    </Grid>
                </Grid>

                <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                    <Button onClick={onClose} sx={{ mr: 1 }}>
                        Cancelar
                    </Button>
                    <Button variant="contained" onClick={handleGuardar}>
                        Programar Mantenimiento
                    </Button>
                </Box>
            </Box>
        </Modal>
    );
};
