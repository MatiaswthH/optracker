// API configuration
import { API_BASE_URL, WEBSOCKET_URL } from '../config';

// Types for the simulation data
export interface TruckPosition {
    id: number;
    placa: string;
    posicionActualX: number;
    posicionActualY: number;
    enRuta: boolean;
    inmovilizado: boolean;
    rutaActual?: string;
}

export interface Route {
    id: number;
    name: string;
    points: Array<{ x: number; y: number }>;
    active: boolean;
    truckId?: number;
}

export interface SimulationStatus {
    trucksInRoute: number;
    trucksInMaintenance: number;
    trucksAvailable: number;
    isRunning: boolean;
    connectedClients?: number;
}

export interface MapData {
    trucks: TruckPosition[];
    activeRoutes: Route[];
    width: number;
    height: number;
    depot: { x: number; y: number };
}

// REST API functions
export const simulationApi = {
    // Get current truck positions
    getTruckPositions: async (): Promise<TruckPosition[]> => {
        const response = await fetch(`${API_BASE_URL}/simulation/trucks/positions`);
        if (!response.ok) throw new Error('Failed to fetch truck positions');
        return response.json();
    },

    // Get active routes
    getActiveRoutes: async (): Promise<Route[]> => {
        const response = await fetch(`${API_BASE_URL}/simulation/routes/active`);
        if (!response.ok) throw new Error('Failed to fetch active routes');
        return response.json();
    },

    // Get simulation status
    getSimulationStatus: async (): Promise<SimulationStatus> => {
        const response = await fetch(`${API_BASE_URL}/simulation/status`);
        if (!response.ok) throw new Error('Failed to fetch simulation status');
        return response.json();
    },

    // Get map data (trucks + routes + config)
    getMapData: async (): Promise<MapData> => {
        const response = await fetch(`${API_BASE_URL}/simulation/map/data`);
        if (!response.ok) throw new Error('Failed to fetch map data');
        return response.json();
    },

    // Start simulation
    startSimulation: async (): Promise<string> => {
        const response = await fetch(`${API_BASE_URL}/simulation/start`, {
            method: 'POST',
        });
        if (!response.ok) throw new Error('Failed to start simulation');
        return response.text();
    },

    // Stop simulation
    stopSimulation: async (): Promise<string> => {
        const response = await fetch(`${API_BASE_URL}/simulation/stop`, {
            method: 'POST',
        });
        if (!response.ok) throw new Error('Failed to stop simulation');
        return response.text();
    },

    // Get trucks currently en route
    getTrucksEnRuta: async (): Promise<TruckPosition[]> => {
        const response = await fetch(`${API_BASE_URL}/simulation/trucks/en-ruta`);
        if (!response.ok) throw new Error('Failed to fetch trucks en ruta');
        return response.json();
    },

    // File upload endpoints
    uploadPedidos: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/pedidos/upload`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
        }

        return response.text();
    },

    uploadMantenimientos: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/mantenimientos/upload`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
        }

        return response.text();
    },

    uploadBloqueos: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/bloqueos/upload`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
        }

        return response.text();
    },
};

// WebSocket connection class
export class SimulationWebSocket {
    private ws: WebSocket | null = null;
    private reconnectInterval: number = 5000;
    private maxReconnectAttempts: number = 5;
    private reconnectAttempts: number = 0;
    private isConnecting: boolean = false;

    // Event callbacks
    public onTruckPositions?: (trucks: TruckPosition[]) => void;
    public onActiveRoutes?: (routes: Route[]) => void;
    public onSimulationStatus?: (status: SimulationStatus) => void;
    public onSimulationEvent?: (event: { type: string; data: any }) => void;
    public onConnectionChange?: (connected: boolean) => void;
    public onError?: (error: string) => void;

    connect(): void {
        if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
            return;
        }

        this.isConnecting = true;

        try {
            this.ws = new WebSocket(WEBSOCKET_URL);

            this.ws.onopen = () => {
                console.log('WebSocket connected to simulation');
                this.isConnecting = false;
                this.reconnectAttempts = 0;
                this.onConnectionChange?.(true);
            };

            this.ws.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                } catch (error) {
                    console.error('Error parsing WebSocket message:', error);
                    this.onError?.('Error parsing server message');
                }
            };

            this.ws.onclose = () => {
                console.log('WebSocket connection closed');
                this.isConnecting = false;
                this.onConnectionChange?.(false);
                this.attemptReconnect();
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.isConnecting = false;
                this.onError?.('WebSocket connection error');
            };

        } catch (error) {
            console.error('Error creating WebSocket connection:', error);
            this.isConnecting = false;
            this.onError?.('Failed to create WebSocket connection');
        }
    }

    private handleMessage(message: any): void {
        switch (message.type) {
            case 'truck_positions':
                this.onTruckPositions?.(message.data);
                break;
            case 'active_routes':
                this.onActiveRoutes?.(message.data);
                break;
            case 'simulation_status':
                this.onSimulationStatus?.(message.data);
                break;
            case 'simulation_event':
                this.onSimulationEvent?.({ type: message.event, data: message.data });
                break;
            case 'connection':
                console.log('Connection acknowledged:', message.data);
                break;
            default:
                console.warn('Unknown message type:', message.type);
        }
    }

    private attemptReconnect(): void {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('Max reconnection attempts reached');
            this.onError?.('Failed to reconnect after multiple attempts');
            return;
        }

        this.reconnectAttempts++;
        console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

        setTimeout(() => {
            this.connect();
        }, this.reconnectInterval);
    }

    disconnect(): void {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.reconnectAttempts = 0;
        this.isConnecting = false;
    }

    isConnected(): boolean {
        return this.ws?.readyState === WebSocket.OPEN;
    }

    // Send a message to the server (for future use)
    send(message: any): void {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.warn('WebSocket not connected, cannot send message');
        }
    }
}

export { WEBSOCKET_URL, API_BASE_URL };
