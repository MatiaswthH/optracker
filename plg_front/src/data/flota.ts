export interface CamionEstatico {
    codigo: string;
    tipo: 'TA' | 'TB' | 'TC' | 'TD';
    tara: number; // en Toneladas
    capacidadM3: number; // en m3
}

const flotaBase: (Omit<CamionEstatico, 'codigo'> & { unidades: number })[] = [
    { tipo: 'TA', tara: 2.5, capacidadM3: 25, unidades: 2 },
    { tipo: 'TB', tara: 2.0, capacidadM3: 15, unidades: 4 },
    { tipo: 'TC', tara: 1.5, capacidadM3: 10, unidades: 4 },
    { tipo: 'TD', tara: 1.0, capacidadM3: 5, unidades: 10 },
];

// Generamos la lista completa de camiones con sus códigos
export const flotaEstatica: CamionEstatico[] = flotaBase.flatMap(tipoInfo => {
    return Array.from({ length: tipoInfo.unidades }, (_, i) => {
        const numero = (i + 1).toString().padStart(2, '0');
        return {
            codigo: `${tipoInfo.tipo}${numero}`,
            tipo: tipoInfo.tipo,
            tara: tipoInfo.tara,
            capacidadM3: tipoInfo.capacidadM3,
        };
    });
});

// Coordenadas del depósito central según el documento
export const DEPOSITO_COORDS = { x: 12, y: 8 };