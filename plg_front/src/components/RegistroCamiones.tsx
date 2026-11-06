import { useState, useEffect, useMemo } from 'react';
import { Box, Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, CircularProgress, Alert } from '@mui/material';
import { simulationApi, type TruckPosition } from '../services/simulationApi';
import { flotaEstatica, DEPOSITO_COORDS, type CamionEstatico } from '../data/flota';
import { ModalRegistroCamion } from '../components/ModalRegistroCamion';

// Nuevo tipo que combina la información estática y dinámica
type CamionCompleto = CamionEstatico & Partial<TruckPosition> & { estado: string };

export const GestionCamiones = () => {
    const [dynamicTrucks, setDynamicTrucks] = useState<Map<string, TruckPosition>>(new Map());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [modalOpen, setModalOpen] = useState(false);

    const handleOpenModal = () => setModalOpen(true);
    const handleCloseModal = () => setModalOpen(false);

    useEffect(() => {
        const fetchTrucks = async () => {
            try {
                const truckData = await simulationApi.getTruckPositions();
                // Usamos un Map para búsqueda rápida por placa/código
                setDynamicTrucks(new Map(truckData.map(truck => [truck.placa, truck])));
                if (error) setError(null);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'No se pudo conectar con la simulación.');
                console.error('Error fetching trucks:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchTrucks();
        const interval = setInterval(fetchTrucks, 5000);
        return () => clearInterval(interval);
    }, [error]);

    // Usamos useMemo para combinar los datos solo cuando cambien
    const camionesMostrados: CamionCompleto[] = useMemo(() => {
        return flotaEstatica.map(staticTruck => {
            const dynamicData = dynamicTrucks.get(staticTruck.codigo);

            const getEstado = (): string => {
                if (!dynamicData) return "Desconectado";
                if (dynamicData.inmovilizado) return "Inmovilizado";
                if (dynamicData.enRuta) return "En Ruta";
                return "Disponible";
            };

            return {
                ...staticTruck,
                ...dynamicData,
                id: dynamicData?.id,
                posicionActualX: dynamicData?.posicionActualX ?? DEPOSITO_COORDS.x,
                posicionActualY: dynamicData?.posicionActualY ?? DEPOSITO_COORDS.y,
                estado: getEstado(),
                rutaActual: dynamicData?.rutaActual || 'N/A',
            };
        });
    }, [dynamicTrucks]);

    const handleGuardarCamion = () => {
        console.log('La gestión de camiones ahora es estática según los requisitos.');
    };

    if (loading) {
        return (
            <Box sx={{ p: 3, display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 200 }}>
                <CircularProgress />
                <Typography sx={{ ml: 2 }}>Cargando camiones...</Typography>
            </Box>
        );
    }

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h4">Flota de Camiones</Typography>
                <Button variant="contained" onClick={handleOpenModal} disabled>
                    Registrar Camión
                </Button>
            </Box>

            {error && <Alert severity="warning" sx={{ mb: 2 }}>{error} Mostrando estado base.</Alert>}

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Código</TableCell>
                            <TableCell>Tipo</TableCell>
                            <TableCell>Capacidad (m³)</TableCell>
                            <TableCell>Tara (Ton)</TableCell>
                            <TableCell>Estado</TableCell>
                            <TableCell align="right">Posición (X, Y)</TableCell>
                            <TableCell>Ruta Actual</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {camionesMostrados.map((truck) => (
                            <TableRow key={truck.codigo}>
                                <TableCell>{truck.codigo}</TableCell>
                                <TableCell>{truck.tipo}</TableCell>
                                <TableCell>{truck.capacidadM3}</TableCell>
                                <TableCell>{truck.tara}</TableCell>
                                <TableCell>{truck.estado}</TableCell>
                                <TableCell align="right">{`${truck.posicionActualX}, ${truck.posicionActualY}`}</TableCell>
                                <TableCell>{truck.rutaActual}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <ModalRegistroCamion
                open={modalOpen}
                onClose={handleCloseModal}
                onGuardar={handleGuardarCamion}
                nextId=""
            />
        </Box>
    );
};