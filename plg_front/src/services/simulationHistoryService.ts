export interface SimulationHistoryEntry {
    id: string;
    startDate: Date;
    endDate: Date;
    duration: number; // in seconds (real time)
    virtualDuration: string; // formatted virtual time
    simulationType: 'Diario' | 'Semanal';
    status: 'exitosa' | 'colapso' | 'interrumpida';
    ordersDelivered: number;
    totalCargo?: number;
    totalCapacity?: number;
    details?: {
        fechaFin?: string;
        tiempoSimulado?: string;
        cantidadPedidosEntregados?: number;
        tipoFin?: boolean;
    };
}

class SimulationHistoryService {
    private readonly STORAGE_KEY = 'plg_simulation_history';

    saveSimulation(entry: Omit<SimulationHistoryEntry, 'id'>): SimulationHistoryEntry {
        const histories = this.getSimulationHistory();
        const newEntry: SimulationHistoryEntry = {
            ...entry,
            id: this.generateId(),
        };

        histories.unshift(newEntry); // Add to beginning

        // Keep only last 50 simulations to avoid localStorage bloat
        const limitedHistories = histories.slice(0, 50);

        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(limitedHistories));
        return newEntry;
    }

    getSimulationHistory(): SimulationHistoryEntry[] {
        try {
            const stored = localStorage.getItem(this.STORAGE_KEY);
            if (!stored) return [];

            const parsed = JSON.parse(stored);
            // Convert date strings back to Date objects
            return parsed.map((entry: any) => ({
                ...entry,
                startDate: new Date(entry.startDate),
                endDate: new Date(entry.endDate),
            }));
        } catch (error) {
            console.error('Error loading simulation history:', error);
            return [];
        }
    }

    clearHistory(): void {
        localStorage.removeItem(this.STORAGE_KEY);
    }

    deleteSimulation(id: string): void {
        const histories = this.getSimulationHistory();
        const filtered = histories.filter(entry => entry.id !== id);
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(filtered));
    }

    getStatistics() {
        const histories = this.getSimulationHistory();

        return {
            total: histories.length,
            successful: histories.filter(h => h.status === 'exitosa').length,
            failed: histories.filter(h => h.status === 'colapso').length,
            interrupted: histories.filter(h => h.status === 'interrumpida').length,
            totalOrdersDelivered: histories.reduce((sum, h) => sum + h.ordersDelivered, 0),
            averageDuration: histories.length > 0
                ? histories.reduce((sum, h) => sum + h.duration, 0) / histories.length
                : 0,
            byType: {
                diario: histories.filter(h => h.simulationType === 'Diario').length,
                semanal: histories.filter(h => h.simulationType === 'Semanal').length,
            }
        };
    }

    private generateId(): string {
        return `sim_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    }
}

export const simulationHistoryService = new SimulationHistoryService();
