import { useState, useCallback } from 'react';
import { simulationApi } from '../services/simulationApi';

interface UseEnviarPedidosReturn {
    loading: boolean;
    error: string | null;
    success: boolean;
    enviarPedidos: (file: File) => Promise<void>;
    reset: () => void;
}

export const useEnviarPedidos = (): UseEnviarPedidosReturn => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const enviarPedidos = useCallback(async (file: File) => {
        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            await simulationApi.uploadPedidos(file);
            setSuccess(true);
            console.log('Pedidos enviados exitosamente');
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Error desconocido al enviar pedidos';
            setError(errorMessage);
            console.error('Error al enviar pedidos:', errorMessage);
        } finally {
            setLoading(false);
        }
    }, []);

    const reset = useCallback(() => {
        setLoading(false);
        setError(null);
        setSuccess(false);
    }, []);

    return {
        loading,
        error,
        success,
        enviarPedidos,
        reset,
    };
};
