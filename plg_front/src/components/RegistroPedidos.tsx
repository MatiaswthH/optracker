import { useState } from 'react';
import { Box, Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip } from '@mui/material';
import { FileUpload as FileUploadIcon } from '@mui/icons-material';
import type { Pedido } from '../types';
import { ModalRegistroPedido } from '../components/ModalRegistroPedido';

const PEDIDOS_DUMMY: Pedido[] = [
    {
        id: 1,
        dia: 11,
        hora: 13,
        minuto: 31,
        posX: 45,
        posY: 43,
        clienteId: 'c-167',
        cantidadM3: 9,
        horasLimite: 36,
        fechaHora: '11d13h31m',
        estado: 'PENDIENTE'
    },
    {
        id: 2,
        dia: 12,
        hora: 8,
        minuto: 15,
        posX: 25,
        posY: 30,
        clienteId: 'c-245',
        cantidadM3: 15,
        horasLimite: 24,
        fechaHora: '12d08h15m',
        estado: 'EN_PROCESO'
    },
    {
        id: 3,
        dia: 10,
        hora: 16,
        minuto: 45,
        posX: 60,
        posY: 40,
        clienteId: 'c-189',
        cantidadM3: 5,
        horasLimite: 48,
        fechaHora: '10d16h45m',
        estado: 'ENTREGADO'
    },
];

export const GestionPedidos = () => {
    const [pedidos, setPedidos] = useState<Pedido[]>(PEDIDOS_DUMMY);
    const [modalOpen, setModalOpen] = useState(false);

    const handleOpenModal = () => setModalOpen(true);
    const handleCloseModal = () => setModalOpen(false);

    const handleGuardarPedido = (nuevoPedido: Omit<Pedido, 'id'>) => {
        setPedidos(prev => [
            ...prev,
            { ...nuevoPedido, id: new Date().getTime() }
        ]);
    };

    const getEstadoColor = (estado: string) => {
        switch (estado) {
            case 'PENDIENTE': return 'warning';
            case 'EN_PROCESO': return 'info';
            case 'ENTREGADO': return 'success';
            case 'VENCIDO': return 'error';
            default: return 'default';
        }
    };

    const getEstadoTexto = (estado: string) => {
        switch (estado) {
            case 'PENDIENTE': return 'Pendiente';
            case 'EN_PROCESO': return 'En Proceso';
            case 'ENTREGADO': return 'Entregado';
            case 'VENCIDO': return 'Vencido';
            default: return estado;
        }
    };

    const handleSubirArchivo = () => {
        // Implementar lógica para subir archivo de pedidos
        alert('Funcionalidad de subir archivo de pedidos - Por implementar');
    };

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h4">Gestión de Pedidos</Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button
                        variant="outlined"
                        startIcon={<FileUploadIcon />}
                        onClick={handleSubirArchivo}
                    >
                        Subir Archivo
                    </Button>
                    <Button variant="contained" onClick={handleOpenModal}>
                        Nuevo Pedido
                    </Button>
                </Box>
            </Box>

            <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                    Formato de registro: DDdHHhMMm:posX,posY,c-idCliente,cantidadm3,horasLímiteh
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Ejemplo: 11d13h31m:45,43,c-167,9m3,36h
                </Typography>
            </Box>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Fecha/Hora</TableCell>
                            <TableCell>Cliente</TableCell>
                            <TableCell>Ubicación (X,Y)</TableCell>
                            <TableCell align="right">Cantidad (m³)</TableCell>
                            <TableCell align="right">Límite (h)</TableCell>
                            <TableCell>Estado</TableCell>
                            <TableCell>Registro</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {pedidos.map((pedido) => (
                            <TableRow key={pedido.id}>
                                <TableCell>
                                    <Typography variant="body2">
                                        Día {pedido.dia} - {String(pedido.hora).padStart(2, '0')}:{String(pedido.minuto).padStart(2, '0')}
                                    </Typography>
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2" fontWeight="bold">
                                        {pedido.clienteId}
                                    </Typography>
                                </TableCell>
                                <TableCell>({pedido.posX}, {pedido.posY})</TableCell>
                                <TableCell align="right">
                                    <Typography variant="body2" fontWeight="bold">
                                        {pedido.cantidadM3}
                                    </Typography>
                                </TableCell>
                                <TableCell align="right">{pedido.horasLimite}</TableCell>
                                <TableCell>
                                    <Chip
                                        label={getEstadoTexto(pedido.estado)}
                                        color={getEstadoColor(pedido.estado) as any}
                                        size="small"
                                    />
                                </TableCell>
                                <TableCell>
                                    <Typography variant="body2" fontFamily="monospace" fontSize="0.75rem">
                                        {pedido.fechaHora}:{pedido.posX},{pedido.posY},{pedido.clienteId},{pedido.cantidadM3}m3,{pedido.horasLimite}h
                                    </Typography>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <Box sx={{ mt: 2, p: 2, bgcolor: 'info.main', color: 'info.contrastText', borderRadius: 1 }}>
                <Typography variant="body2">
                    <strong>Información importante:</strong> Los pedidos históricos son anteriores al tiempo actual.
                    Los pedidos futuros se procesan según la fecha/hora programada.
                    Tiempo mínimo de entrega: 4 horas.
                </Typography>
            </Box>

            <ModalRegistroPedido
                open={modalOpen}
                onClose={handleCloseModal}
                onGuardar={handleGuardarPedido}
            />
        </Box>
    );
};
