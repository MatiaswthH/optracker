import { useState, useEffect, useRef, useCallback } from 'react';
import { SimulationWebSocket } from '../services/simulationApi';
import type {
    TruckPosition,
    Route,
    SimulationStatus
} from '../services/simulationApi';

interface UseSimulationWebSocketReturn {
    // Connection state
    isConnected: boolean;
    isConnecting: boolean;
    error: string | null;

    // Simulation data
    truckPositions: TruckPosition[];
    activeRoutes: Route[];
    simulationStatus: SimulationStatus | null;
    lastEvent: { type: string; data: any } | null;

    // Connection management
    connect: () => void;
    disconnect: () => void;
    reconnect: () => void;

    // Manual data refresh
    refreshData: () => void;
}

export const useSimulationWebSocket = (): UseSimulationWebSocketReturn => {
    // Connection state
    const [isConnected, setIsConnected] = useState(false);
    const [isConnecting, setIsConnecting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Simulation data
    const [truckPositions, setTruckPositions] = useState<TruckPosition[]>([]);
    const [activeRoutes, setActiveRoutes] = useState<Route[]>([]);
    const [simulationStatus, setSimulationStatus] = useState<SimulationStatus | null>(null);
    const [lastEvent, setLastEvent] = useState<{ type: string; data: any } | null>(null);

    // WebSocket instance ref
    const wsRef = useRef<SimulationWebSocket | null>(null);

    // Initialize WebSocket
    useEffect(() => {
        if (!wsRef.current) {
            wsRef.current = new SimulationWebSocket();

            // Set up event handlers
            wsRef.current.onConnectionChange = (connected) => {
                setIsConnected(connected);
                setIsConnecting(false);
                if (connected) {
                    setError(null);
                }
            };

            wsRef.current.onTruckPositions = (trucks) => {
                setTruckPositions(trucks);
            };

            wsRef.current.onActiveRoutes = (routes) => {
                setActiveRoutes(routes);
            };

            wsRef.current.onSimulationStatus = (status) => {
                setSimulationStatus(status);
            };

            wsRef.current.onSimulationEvent = (event) => {
                setLastEvent(event);
                console.log('Simulation event:', event);
            };

            wsRef.current.onError = (errorMessage) => {
                setError(errorMessage);
                setIsConnecting(false);
            };
        }

        return () => {
            wsRef.current?.disconnect();
        };
    }, []);

    // Connection management functions
    const connect = useCallback(() => {
        if (wsRef.current && !isConnected && !isConnecting) {
            setIsConnecting(true);
            setError(null);
            wsRef.current.connect();
        }
    }, [isConnected, isConnecting]);

    const disconnect = useCallback(() => {
        if (wsRef.current) {
            wsRef.current.disconnect();
            setIsConnected(false);
            setIsConnecting(false);
        }
    }, []);

    const reconnect = useCallback(() => {
        disconnect();
        setTimeout(() => {
            connect();
        }, 1000);
    }, [connect, disconnect]);

    // Manual data refresh function (fallback when WebSocket is not working)
    const refreshData = useCallback(async () => {
        try {
            const { simulationApi } = await import('../services/simulationApi');

            const [trucks, routes, status] = await Promise.all([
                simulationApi.getTruckPositions(),
                simulationApi.getActiveRoutes(),
                simulationApi.getSimulationStatus(),
            ]);

            setTruckPositions(trucks);
            setActiveRoutes(routes);
            setSimulationStatus(status);
            setError(null);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to refresh data';
            setError(errorMessage);
            console.error('Error refreshing data:', err);
        }
    }, []);

    // Auto-connect on mount
    useEffect(() => {
        connect();
    }, [connect]);

    return {
        // Connection state
        isConnected,
        isConnecting,
        error,

        // Simulation data
        truckPositions,
        activeRoutes,
        simulationStatus,
        lastEvent,

        // Connection management
        connect,
        disconnect,
        reconnect,

        // Manual data refresh
        refreshData,
    };
};
