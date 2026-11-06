import React, { createContext, useState, useCallback, useEffect, useContext } from 'react';
import { useSimulacionSemanal } from '../hooks/useSimulacionSemanal';
import { useSimulacionDiaria } from '../hooks/useSimulacionDiaria';
import { useSimulationTracker } from '../hooks/useSimulationTracker';
import stompClient from '../stomp-client';

const SimulacionContext = createContext(null);

export const useSimulacion = () => useContext(SimulacionContext);

export const SimulacionProvider = ({ children }) => {
    const [estadoSimulacion, setEstadoSimulacion] = useState(0); // 0: Sin Iniciar, 1: Corriendo, 2: Pausado
    const [fechaInicio, setFechaInicio] = useState("");
    const [snapshotQueue, setSnapshotQueue] = useState([]);
    const [currentSnapshot, setCurrentSnapshot] = useState(null);
    const [snapshotsEventos, setSnapshotsEventos] = useState([]);
    const [resultado, setResultado] = useState(null);
    const [simulacionSpeed, setSimulacionSpeed] = useState(1);
    const [tipoSimulacion, setTipoSimulacion] = useState("Semanal");

    const onSnapshot = useCallback((snapshot) => {
        setSnapshotQueue((prev) => [...prev, snapshot]);
    }, []);

    const onResultado = useCallback((res) => {
        setResultado(res);
    }, []);

    if (tipoSimulacion === "Semanal") {
        useSimulacionSemanal(estadoSimulacion, onSnapshot, onResultado, fechaInicio);
    } else if (tipoSimulacion === "Diario") {
        useSimulacionDiaria(estadoSimulacion, onSnapshot, onResultado, fechaInicio);
    }

    // Track simulation history
    useSimulationTracker(
        estadoSimulacion,
        tipoSimulacion as 'Diario' | 'Semanal',
        resultado,
        currentSnapshot,
        snapshotQueue.length
    );

    useEffect(() => {
        if (estadoSimulacion === 0) {
            setSnapshotQueue([]);
            setCurrentSnapshot(null);
            setSnapshotsEventos([]);
            setResultado(null);
        }
        if (estadoSimulacion === 1) {
            const interval = setInterval(() => {
                setSnapshotQueue((prevQueue) => {
                    if (prevQueue.length > 0) {
                        const nextSnapshot = prevQueue[0];
                        setCurrentSnapshot(nextSnapshot);
                        if (nextSnapshot?.eventos.length > 0) {
                            setSnapshotsEventos((prev) =>
                                [
                                    ...prev,
                                    ...nextSnapshot.eventos.map((evento) => ({
                                        evento,
                                        fechaHoraActual: nextSnapshot.fechaHoraActual,
                                    })),
                                ].slice(-30)
                            );
                        }
                        if (stompClient.active && tipoSimulacion === "Diario") {
                            stompClient.publish({ destination: '/app/simulation/continue' });
                        }
                        return prevQueue.slice(1);
                    }
                    return prevQueue;
                });
            }, 50 / simulacionSpeed); // Reduced from 100ms to 50ms for faster updates
            return () => clearInterval(interval);
        }
    }, [estadoSimulacion, simulacionSpeed, tipoSimulacion]);

    const value = {
        estadoSimulacion,
        setEstadoSimulacion,
        fechaInicio,
        setFechaInicio,
        snapshotQueue,
        currentSnapshot,
        snapshotsEventos,
        resultado,
        simulacionSpeed,
        setSimulacionSpeed,
        tipoSimulacion,
        setTipoSimulacion,
    };

    return (
        <SimulacionContext.Provider value={value}>
            {children}
        </SimulacionContext.Provider>
    );
};