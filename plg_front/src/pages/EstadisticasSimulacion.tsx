import { useState, useEffect } from 'react';
import {
    Box,
    Typography,
    Card,
    CardContent,
    GridLegacy as Grid,
    Chip,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    IconButton,
    Tooltip,
    Alert,
    Stack,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import ClearAllIcon from '@mui/icons-material/ClearAll';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import { simulationHistoryService, type SimulationHistoryEntry } from '../services/simulationHistoryService';
import { formatFechaHoraVerbose, horasMinutosSegundos } from '../utils/utilsMethods';

const EstadisticasSimulacion = () => {
    const [history, setHistory] = useState<SimulationHistoryEntry[]>([]);
    const [statistics, setStatistics] = useState<any>(null);
    const [clearDialogOpen, setClearDialogOpen] = useState(false);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = () => {
        const historyData = simulationHistoryService.getSimulationHistory();
        const statsData = simulationHistoryService.getStatistics();
        setHistory(historyData);
        setStatistics(statsData);
    };

    const handleDeleteSimulation = (id: string) => {
        simulationHistoryService.deleteSimulation(id);
        loadData();
    };

    const handleClearAll = () => {
        simulationHistoryService.clearHistory();
        loadData();
        setClearDialogOpen(false);
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'exitosa': return 'success';
            case 'colapso': return 'error';
            case 'interrumpida': return 'warning';
            default: return 'default';
        }
    };

    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'exitosa': return <CheckCircleIcon />;
            case 'colapso': return <ErrorIcon />;
            case 'interrumpida': return <PauseCircleIcon />;
            default: return undefined;
        }
    };

    const getTypeColor = (type: string) => {
        return type === 'Diario' ? 'primary' : 'secondary';
    };

    return (
        <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h4" component="h1" fontWeight="bold">
                    Estadísticas de Simulaciones
                </Typography>
                {history.length > 0 && (
                    <Button
                        variant="outlined"
                        color="error"
                        startIcon={<ClearAllIcon />}
                        onClick={() => setClearDialogOpen(true)}
                    >
                        Limpiar Historial
                    </Button>
                )}
            </Box>

            {statistics && (
                <Grid container spacing={3} sx={{ mb: 4 }}>
                    <Grid item xs={12} sm={6} md={3}>
                        <Card>
                            <CardContent>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <TrendingUpIcon color="primary" />
                                    <Typography variant="h6">Total</Typography>
                                </Stack>
                                <Typography variant="h4" color="primary">
                                    {statistics.total}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Simulaciones ejecutadas
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                        <Card>
                            <CardContent>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <CheckCircleIcon color="success" />
                                    <Typography variant="h6">Exitosas</Typography>
                                </Stack>
                                <Typography variant="h4" color="success.main">
                                    {statistics.successful}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    {statistics.total > 0 ? Math.round((statistics.successful / statistics.total) * 100) : 0}% del total
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                        <Card>
                            <CardContent>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <ErrorIcon color="error" />
                                    <Typography variant="h6">Fallidas</Typography>
                                </Stack>
                                <Typography variant="h4" color="error.main">
                                    {statistics.failed}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Colapsos logísticos
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={6} md={3}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">Pedidos Entregados</Typography>
                                <Typography variant="h4" color="info.main">
                                    {statistics.totalOrdersDelivered}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Total acumulado
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Por Tipo de Simulación</Typography>
                                <Stack direction="row" spacing={2}>
                                    <Box>
                                        <Typography variant="body2" color="text.secondary">Diarias</Typography>
                                        <Typography variant="h5" color="primary.main">{statistics.byType.diario}</Typography>
                                    </Box>
                                    <Box>
                                        <Typography variant="body2" color="text.secondary">Semanales</Typography>
                                        <Typography variant="h5" color="secondary.main">{statistics.byType.semanal}</Typography>
                                    </Box>
                                </Stack>
                            </CardContent>
                        </Card>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Duración Promedio</Typography>
                                <Typography variant="h5" color="text.primary">
                                    {horasMinutosSegundos(Math.round(statistics.averageDuration))}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Tiempo real de ejecución
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            )}

            {history.length === 0 ? (
                <Alert severity="info" sx={{ textAlign: 'center' }}>
                    <Typography variant="h6" gutterBottom>
                        No hay simulaciones registradas
                    </Typography>
                    <Typography>
                        Las estadísticas aparecerán aquí después de ejecutar simulaciones.
                    </Typography>
                </Alert>
            ) : (
                <Card>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Historial de Simulaciones
                        </Typography>
                        <TableContainer component={Paper} variant="outlined">
                            <Table size="small">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Fecha Inicio</TableCell>
                                        <TableCell>Tipo</TableCell>
                                        <TableCell>Estado</TableCell>
                                        <TableCell align="right">Duración Real</TableCell>
                                        <TableCell align="right">Duración Virtual</TableCell>
                                        <TableCell align="right">Pedidos Entregados</TableCell>
                                        <TableCell align="center">Acciones</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {history.map((entry) => (
                                        <TableRow key={entry.id} hover>
                                            <TableCell>
                                                <Typography variant="body2">
                                                    {formatFechaHoraVerbose(entry.startDate.toISOString())}
                                                </Typography>
                                            </TableCell>
                                            <TableCell>
                                                <Chip
                                                    label={entry.simulationType}
                                                    color={getTypeColor(entry.simulationType)}
                                                    size="small"
                                                />
                                            </TableCell>
                                            <TableCell>
                                                <Chip
                                                    icon={getStatusIcon(entry.status)}
                                                    label={
                                                        entry.status === 'exitosa' ? 'Exitosa' :
                                                            entry.status === 'colapso' ? 'Colapso' : 'Interrumpida'
                                                    }
                                                    color={getStatusColor(entry.status)}
                                                    size="small"
                                                />
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography variant="body2">
                                                    {horasMinutosSegundos(entry.duration)}
                                                </Typography>
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography variant="body2">
                                                    {entry.virtualDuration}
                                                </Typography>
                                            </TableCell>
                                            <TableCell align="right">
                                                <Typography variant="body2" fontWeight="bold">
                                                    {entry.ordersDelivered}
                                                </Typography>
                                            </TableCell>
                                            <TableCell align="center">
                                                <Tooltip title="Eliminar simulación">
                                                    <IconButton
                                                        size="small"
                                                        color="error"
                                                        onClick={() => handleDeleteSimulation(entry.id)}
                                                    >
                                                        <DeleteIcon fontSize="small" />
                                                    </IconButton>
                                                </Tooltip>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </CardContent>
                </Card>
            )}

            <Dialog
                open={clearDialogOpen}
                onClose={() => setClearDialogOpen(false)}
            >
                <DialogTitle>Confirmar limpieza del historial</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        ¿Estás seguro de que quieres eliminar todo el historial de simulaciones?
                        Esta acción no se puede deshacer.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setClearDialogOpen(false)}>Cancelar</Button>
                    <Button onClick={handleClearAll} color="error" variant="contained">
                        Eliminar Todo
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default EstadisticasSimulacion;
