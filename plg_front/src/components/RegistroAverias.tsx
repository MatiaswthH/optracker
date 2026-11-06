import { useState } from 'react';
import { Box, Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip } from '@mui/material';
import type { Averia } from '../types';
import { ModalRegistroAveria } from '../components/ModalRegistroAveria';

const AVERIAS_DUMMY: Averia[] = [
    {
        id: 1,
        camionId: 'TA01',
        turno: 'T1',
        tipoIncidente: 'TI1',
        nodoX: 25,
        nodoY: 30,
        descripcion: 'Se baja la llanta - reparable por conductor',
        fechaHora: '2025-06-24T10:30:00Z',
        tiempoInmovilizacion: 2,
        tiempoTaller: 0
    },
    {
        id: 2,
        camionId: 'TB02',
        turno: 'T2',
        tipoIncidente: 'TI2',
        nodoX: 45,
        nodoY: 20,
        descripcion: 'Se ahoga (obstruye) el motor',
        fechaHora: '2025-06-24T14:15:00Z',
        tiempoInmovilizacion: 2,
        tiempoTaller: 8
    },
];

export const GestionAverias = () => {
    const [averias, setAverias] = useState<Averia[]>(AVERIAS_DUMMY);
    const [modalOpen, setModalOpen] = useState(false);

    const handleOpenModal = () => setModalOpen(true);
    const handleCloseModal = () => setModalOpen(false);

    const handleGuardarAveria = (nuevaAveria: Omit<Averia, 'id'>) => {
        setAverias(prev => [
            ...prev,
            { ...nuevaAveria, id: new Date().getTime() }
        ]);
    };

    const getSeverityColor = (tipo: string) => {
        switch (tipo) {
            case 'TI1': return 'success';
            case 'TI2': return 'warning';
            case 'TI3': return 'error';
            default: return 'default';
        }
    };

    const formatFecha = (fechaHora: string) => {
        return new Date(fechaHora).toLocaleString('es-ES');
    };

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h4">Gestión de Incidentes/Averías</Typography>
                <Button variant="contained" onClick={handleOpenModal}>
                    Registrar Nuevo Incidente
                </Button>
            </Box>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Camión</TableCell>
                            <TableCell>Turno</TableCell>
                            <TableCell>Tipo</TableCell>
                            <TableCell>Ubicación (X,Y)</TableCell>
                            <TableCell>Descripción</TableCell>
                            <TableCell>Fecha/Hora</TableCell>
                            <TableCell align="center">Inmov. (h)</TableCell>
                            <TableCell align="center">Taller (h)</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {averias.map((averia) => (
                            <TableRow key={averia.id}>
                                <TableCell>
                                    <Typography variant="body2" fontWeight="bold">
                                        {averia.camionId}
                                    </Typography>
                                </TableCell>
                                <TableCell>{averia.turno}</TableCell>
                                <TableCell>
                                    <Chip
                                        label={averia.tipoIncidente}
                                        color={getSeverityColor(averia.tipoIncidente) as any}
                                        size="small"
                                    />
                                </TableCell>
                                <TableCell>({averia.nodoX}, {averia.nodoY})</TableCell>
                                <TableCell sx={{ maxWidth: 200 }}>
                                    <Typography variant="body2" noWrap title={averia.descripcion}>
                                        {averia.descripcion}
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2">
                                        {formatFecha(averia.fechaHora)}
                                    </Typography>
                                </TableCell>
                                <TableCell align="center">{averia.tiempoInmovilizacion}</TableCell>
                                <TableCell align="center">{averia.tiempoTaller}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <ModalRegistroAveria
                open={modalOpen}
                onClose={handleCloseModal}
                onGuardar={handleGuardarAveria}
            />
        </Box>
    );
};
