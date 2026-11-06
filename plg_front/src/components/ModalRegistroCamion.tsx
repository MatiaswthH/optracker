import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, TextField, Button, Grid, Select, MenuItem, InputLabel, FormControl } from '@mui/material';
import type { Camion, EstadoCamion } from '../types';
import { EstadosCamion } from '../types';

const style = {
    position: 'absolute' as 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 500,
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 4,
};

interface Props {
    open: boolean;
    onClose: () => void;
    onGuardar: (camion: Omit<Camion, 'id'>) => void;
    nextId: string;
}

const tiposCamion = [
    { tipo: 'TA', tara: 2.5, capacidadMaxima: 25, pesoCargaGLP: 12.5, pesoCombinado: 15.0 },
    { tipo: 'TB', tara: 2.0, capacidadMaxima: 15, pesoCargaGLP: 7.5, pesoCombinado: 9.5 },
    { tipo: 'TC', tara: 1.5, capacidadMaxima: 10, pesoCargaGLP: 5.0, pesoCombinado: 6.5 },
    { tipo: 'TD', tara: 1.0, capacidadMaxima: 5, pesoCargaGLP: 2.5, pesoCombinado: 3.5 },
];

export const ModalRegistroCamion: React.FC<Props> = ({ open, onClose, onGuardar }) => {
    const [tipoSeleccionado, setTipoSeleccionado] = useState('TA');
    const [estado, setEstado] = useState<EstadoCamion>('EN PLANTA');
    const [errors, setErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        if (!open) {
            setTipoSeleccionado('TA');
            setEstado('EN PLANTA');
            setErrors({});
        }
    }, [open]);

    const tipoInfo = tiposCamion.find(t => t.tipo === tipoSeleccionado) || tiposCamion[0];

    const validar = () => {
        const newErrors: Record<string, string> = {};
        if (!tipoSeleccionado) newErrors.tipo = 'El tipo es requerido';
        if (!estado) newErrors.estado = 'El estado es requerido';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleGuardarClick = () => {
        if (validar()) {
            onGuardar({
                tipo: tipoSeleccionado,
                tara: tipoInfo.tara,
                capacidadMaxima: tipoInfo.capacidadMaxima,
                pesoCargaGLP: tipoInfo.pesoCargaGLP,
                pesoCombinado: tipoInfo.pesoCombinado,
                estado: estado,
            });
            onClose();
        }
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={style}>
                <Typography variant="h6" component="h2">Registrar Nuevo Camión Cisterna</Typography>
                <Grid container spacing={2} sx={{ mt: 2 }}>
                    <Grid item xs={12}>
                        <FormControl fullWidth error={!!errors.tipo}>
                            <InputLabel>Tipo de Camión</InputLabel>
                            <Select
                                value={tipoSeleccionado}
                                label="Tipo de Camión"
                                onChange={(e) => setTipoSeleccionado(e.target.value)}
                            >
                                {tiposCamion.map(tipo => (
                                    <MenuItem key={tipo.tipo} value={tipo.tipo}>
                                        {tipo.tipo} - {tipo.capacidadMaxima}m³ - {tipo.pesoCombinado}Ton
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            label="Tara (Ton)"
                            value={tipoInfo.tara}
                            fullWidth
                            disabled
                            type="number"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            label="Capacidad (m³)"
                            value={tipoInfo.capacidadMaxima}
                            fullWidth
                            disabled
                            type="number"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            label="Peso GLP (Ton)"
                            value={tipoInfo.pesoCargaGLP}
                            fullWidth
                            disabled
                            type="number"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <TextField
                            label="Peso Combinado (Ton)"
                            value={tipoInfo.pesoCombinado}
                            fullWidth
                            disabled
                            type="number"
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <FormControl fullWidth error={!!errors.estado}>
                            <InputLabel>Estado</InputLabel>
                            <Select
                                value={estado}
                                label="Estado"
                                onChange={(e) => setEstado(e.target.value as EstadoCamion)}
                            >
                                {EstadosCamion.map(e => <MenuItem key={e} value={e}>{e}</MenuItem>)}
                            </Select>
                        </FormControl>
                    </Grid>
                </Grid>
                <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                    <Button onClick={onClose}>Cancelar</Button>
                    <Button onClick={handleGuardarClick} variant="contained" sx={{ ml: 2 }}>Guardar</Button>
                </Box>
            </Box>
        </Modal>
    );
};