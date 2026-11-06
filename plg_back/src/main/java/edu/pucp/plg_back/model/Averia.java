package edu.pucp.plg_back.model;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Averia {

    private int tipo; // Tipo de incidente (1, 2 o 3)
    private String turno; // turno 1, 2 o 3
    private String codigoUnidad;
    private Nodo nodoAveria; // Nodo donde ocurre la averia

    private LocalDateTime fechaIncidente; // Fecha y hora del incidente
    private LocalDateTime fechaDisponible; // Fecha y hora en la que el camión estará disponible para volver a operar
    private LocalDateTime fechaMovilizado; // Fecha y hora en la que el camión se moviliza después de la avería
    private double tiempoInamovilidad; // Tiempo de inamovilidad del camión en minutos
    private double tiempoIndisponibilidad; // Tiempo de indisponibilidad del camión en minutos
    private boolean requiereTrasvase; // Indica si el incidente requiere trasvase de carga

    /*
     * public Averia(int tipoIncidente, LocalDateTime fechaIncidente, Nodo
     * nodoIncidente) {
     * this.tipoIncidente = tipoIncidente;
     * this.fechaIncidente = fechaIncidente;
     * this.nodoIncidente = nodoIncidente;
     * }
     */
    public Averia(String turno, String codigoUnidad, int tipo) {
        this.turno = turno;
        this.codigoUnidad = codigoUnidad;
        this.tipo = tipo;
    }

    public void calcularTiempoDisponible() {
        switch (tipo) {
            case 1: // Incidente tipo 1
                this.fechaDisponible = fechaIncidente.plusHours(2); // Disponible en 2 horas
                this.fechaMovilizado = fechaIncidente.plusHours(2); // Puede moverse en 2 horas
                this.requiereTrasvase = true; // Requiere trasvase
                this.tiempoInamovilidad = 2 * 60; // Tiempo de inamovilidad en minutos
                this.tiempoIndisponibilidad = 2 * 60; // Tiempo de indisponibilidad en minutos
                break;
            case 2: // Incidente tipo 2
                this.fechaDisponible = calcularDisponibilidadTipo2(fechaIncidente);
                this.fechaMovilizado = fechaIncidente.plusHours(2); // Puede moverse en 2 horas
                this.requiereTrasvase = true; // Requiere trasvase
                this.tiempoInamovilidad = 2 * 60;
                this.tiempoIndisponibilidad = fechaDisponible.until(fechaIncidente,
                        java.time.temporal.ChronoUnit.MINUTES); // Tiempo de indisponibilidad hasta que esté disponible
                break;
            case 3: // Incidente tipo 3
                this.fechaDisponible = fechaIncidente.plusDays(3).withHour(0); // Disponible en el turno 1 del día A+3
                this.fechaMovilizado = fechaIncidente.plusHours(4); // Puede moverse en 2 horas
                this.requiereTrasvase = true; // Requiere trasvase
                this.tiempoInamovilidad = 4 * 60;
                this.tiempoIndisponibilidad = fechaDisponible.until(fechaIncidente,
                        java.time.temporal.ChronoUnit.MINUTES); // Tiempo de indisponibilidad hasta que esté disponible
                break;
        }
    }

    private LocalDateTime calcularDisponibilidadTipo2(LocalDateTime fechaIncidente) {
        int hora = fechaIncidente.getHour();
        if (hora < 8) { // Turno 1
            return fechaIncidente.withHour(16); // Disponible en el turno 3 del mismo día
        } else if (hora < 16) { // Turno 2
            return fechaIncidente.plusDays(1).withHour(0); // Disponible en el turno 1 del día siguiente
        } else { // Turno 3
            return fechaIncidente.plusDays(1).withHour(8); // Disponible en el turno 2 del día siguiente
        }
    }

    public void actualizarRequerimientoTrasvase(boolean requiereTrasvase) {
        if (this.tiempoInamovilidad == 0)
            this.requiereTrasvase = false;
    }

    public void setTurnoDiario(LocalDateTime fecha) {
        int hora = fecha.getHour();
        if (hora < 8) {
            this.turno = "T1";
        } else if (hora < 16) {
            this.turno = "T2";
        } else {
            this.turno = "T3";
        }
    }
}