import { useState } from 'react';
import { Box, Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import type { Mantenimiento } from '../types';
import { ModalRegistroMantenimiento } from '../components/ModalRegistroMantenimiento';

const MANTENIMIENTOS_DUMMY: Mantenimiento[] = [
    { id: 1, camionId: 'TA01', fecha: '20250701', tipo: 'Preventivo', descripcion: 'Cambio de aceite y filtros', programado: true },
    { id: 2, camionId: 'TB02', fecha: '20250705', tipo: 'Correctivo', descripcion: 'Revisión de frenos y suspensión', programado: false },
    { id: 3, camionId: 'TC03', fecha: '20250710', tipo: 'Preventivo', descripcion: 'Mantenimiento programado bimensual', programado: true },
];

export const GestionMantenimientos = () => {
    const [mantenimientos, setMantenimientos] = useState<Mantenimiento[]>(MANTENIMIENTOS_DUMMY);
    const [modalOpen, setModalOpen] = useState(false);

    const handleOpenModal = () => setModalOpen(true);
    const handleCloseModal = () => setModalOpen(false);

    const handleGuardarMantenimiento = (nuevoMantenimiento: Omit<Mantenimiento, 'id'>) => {
        setMantenimientos(prev => [
            ...prev,
            { ...nuevoMantenimiento, id: new Date().getTime() }
        ]);
    };

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h4">Gestión de Mantenimientos</Typography>
                <Button variant="contained" onClick={handleOpenModal}>
                    Registrar Nuevo Mantenimiento
                </Button>
            </Box>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>ID Camión</TableCell>
                            <TableCell>Fecha</TableCell>
                            <TableCell>Tipo</TableCell>
                            <TableCell>Descripción</TableCell>
                            <TableCell align="right">Duración (hrs)</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {mantenimientos.map((mantenimiento) => (
                            <TableRow key={mantenimiento.id}>
                                <TableCell>{mantenimiento.camionId}</TableCell>
                                <TableCell>{mantenimiento.fecha}</TableCell>
                                <TableCell>{mantenimiento.tipo}</TableCell>
                                <TableCell>{mantenimiento.descripcion}</TableCell>
                                <TableCell align="right">{mantenimiento.duracion}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <ModalRegistroMantenimiento
                open={modalOpen}
                onClose={handleCloseModal}
                onGuardar={handleGuardarMantenimiento}
            />
        </Box>
    );
};
