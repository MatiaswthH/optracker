import { useEffect, useRef } from 'react';
import { simulationHistoryService } from '../services/simulationHistoryService';

export const useSimulationTracker = (
    estadoSimulacion: number,
    tipoSimulacion: 'Diario' | 'Semanal',
    resultado: any,
    currentSnapshot: any,
    queueLength: number
) => {
    const simulationStartTime = useRef<Date | null>(null);
    const simulationStarted = useRef<boolean>(false);

    // Track simulation start
    useEffect(() => {
        if (estadoSimulacion === 1 && !simulationStarted.current) {
            simulationStartTime.current = new Date();
            simulationStarted.current = true;
        } else if (estadoSimulacion === 0) {
            simulationStartTime.current = null;
            simulationStarted.current = false;
        }
    }, [estadoSimulacion]);

    // Track simulation end and save to localStorage
    useEffect(() => {
        if (
            simulationStarted.current &&
            simulationStartTime.current &&
            estadoSimulacion === 1 && // Was running
            queueLength === 0 && // No more snapshots
            currentSnapshot != null && // Has a final snapshot
            resultado !== null // Has results
        ) {
            const endTime = new Date();
            const duration = Math.floor((endTime.getTime() - simulationStartTime.current.getTime()) / 1000);

            // Determine status based on resultado
            let status: 'exitosa' | 'colapso' | 'interrumpida' = 'interrumpida';
            if (resultado.tipoFin === true) {
                status = 'exitosa';
            } else if (resultado.tipoFin === false) {
                status = 'colapso';
            }

            const simulationEntry = {
                startDate: simulationStartTime.current,
                endDate: endTime,
                duration: duration,
                virtualDuration: resultado.tiempoSimulado || 'N/A',
                simulationType: tipoSimulacion,
                status: status,
                ordersDelivered: resultado.cantidadPedidosEntregados || 0,
                details: {
                    fechaFin: resultado.fechaFin,
                    tiempoSimulado: resultado.tiempoSimulado,
                    cantidadPedidosEntregados: resultado.cantidadPedidosEntregados,
                    tipoFin: resultado.tipoFin,
                }
            };

            try {
                simulationHistoryService.saveSimulation(simulationEntry);
                console.log('Simulation saved to history:', simulationEntry);
            } catch (error) {
                console.error('Error saving simulation to history:', error);
            }

            // Reset tracking state
            simulationStartTime.current = null;
            simulationStarted.current = false;
        }
    }, [estadoSimulacion, queueLength, resultado, currentSnapshot, tipoSimulacion]);

    // Handle manual interruption (when user stops simulation before completion)
    useEffect(() => {
        if (simulationStarted.current && simulationStartTime.current && estadoSimulacion === 0) {
            // Simulation was manually stopped
            const endTime = new Date();
            const duration = Math.floor((endTime.getTime() - simulationStartTime.current.getTime()) / 1000);

            // Only save if simulation ran for at least 5 seconds (to avoid accidental stops)
            if (duration >= 5) {
                const simulationEntry = {
                    startDate: simulationStartTime.current,
                    endDate: endTime,
                    duration: duration,
                    virtualDuration: currentSnapshot?.fechaHoraActual ? 'Interrumpida' : 'N/A',
                    simulationType: tipoSimulacion,
                    status: 'interrumpida' as const,
                    ordersDelivered: currentSnapshot?.pedidos?.filter((p: any) => p.entregado)?.length || 0,
                    details: {
                        fechaFin: endTime.toISOString(),
                        tiempoSimulado: 'Simulación interrumpida por el usuario',
                        cantidadPedidosEntregados: currentSnapshot?.pedidos?.filter((p: any) => p.entregado)?.length || 0,
                        tipoFin: false,
                    }
                };

                try {
                    simulationHistoryService.saveSimulation(simulationEntry);
                    console.log('Interrupted simulation saved to history:', simulationEntry);
                } catch (error) {
                    console.error('Error saving interrupted simulation to history:', error);
                }
            }

            // Reset tracking state
            simulationStartTime.current = null;
            simulationStarted.current = false;
        }
    }, [estadoSimulacion, currentSnapshot, tipoSimulacion]);
};
