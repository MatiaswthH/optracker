export type EstadoCamion = "EN RUTA" | "EN MANTENIMIENTO" | "EN PLANTA" | "AVERIADO";

export const EstadosCamion: EstadoCamion[] = ["EN RUTA", "EN MANTENIMIENTO", "EN PLANTA", "AVERIADO"];

export interface Camion {
    id: string; // Formato: TTNN (ej: TA01, TB02, TC03, TD04)
    tipo: string; // TA, TB, TC, TD
    tara: number; // en toneladas
    capacidadMaxima: number; // en m3
    pesoCargaGLP: number; // en toneladas
    pesoCombinado: number; // en toneladas
    estado: EstadoCamion;
}

export type TipoIncidente = "TI1" | "TI2" | "TI3";

export const TiposIncidente: TipoIncidente[] = ["TI1", "TI2", "TI3"];

export interface Averia {
    id: number | string;
    camionId: string;
    turno: string; // T1, T2, T3
    tipoIncidente: TipoIncidente;
    nodoX: number;
    nodoY: number;
    descripcion: string;
    fechaHora: string;
    tiempoInmovilizacion: number; // en horas
    tiempoTaller: number; // en horas
}

export type TipoMantenimiento = "Preventivo" | "Correctivo";

export const TiposMantenimiento: TipoMantenimiento[] = ["Preventivo", "Correctivo"];

export interface Mantenimiento {
    id: number | string;
    camionId: string;
    fecha: string; // formato: aaaammdd
    tipo: TipoMantenimiento;
    descripcion: string;
    programado: boolean; // true si es del plan bimensual
}

export interface Pedido {
    id: number | string;
    dia: number;
    hora: number;
    minuto: number;
    posX: number;
    posY: number;
    clienteId: string; // formato: c-167
    cantidadM3: number;
    horasLimite: number;
    fechaHora: string;
    estado: "PENDIENTE" | "EN_PROCESO" | "ENTREGADO" | "VENCIDO";
}

export interface AveriaIn {
    codigoCamion: string;
    tipo: string;
}