package edu.pucp.plg_back.simulation;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimulationParams {
    
    @Builder.Default
    private LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 1, 0, 0);
    @Builder.Default
    private LocalDateTime fechaFin = LocalDateTime.of(2025, 1, 8, 0, 0);
    @Builder.Default
    private boolean algoritmoACO = false; // true para ACO, false para GA
    @Builder.Default
    private boolean averias = true; // true para incluir averías, false para no incluir
    @Builder.Default
    private int nMinutosSimulacion = 15; // minutos de simulación (semanal: 10080, diario: 1440)
    @Builder.Default
    private int velocidadPromedioCamion = 120; // km/h

    public int getNIteracionesMovimientoCamiones() {
        return this.MCMSimulacion(this.nMinutosSimulacion, this.velocidadPromedioCamion);
    }

    public int getUnMinutoSimulacion() {
        // retorna cuantas iteraciones toma para simular un minuto de simulacion
        return this.MCMSimulacion(this.nMinutosSimulacion, this.velocidadPromedioCamion) / this.nMinutosSimulacion;
    }

    public int getUnKilometroMovimientoSimulacion() {
        // retorna cuantas iteraciones toma para simular un kilometro de movimiento
        return this.MCMSimulacion(this.nMinutosSimulacion, this.velocidadPromedioCamion)
                / (this.velocidadPromedioCamion / (60 / this.nMinutosSimulacion));
    }

    public int MCMSimulacion(int minutosSimulacion, int velocidadCamion) {
        int a = minutosSimulacion; // minutos de simulación
        int b = velocidadCamion / (60 / minutosSimulacion); // b vendria a ser cuantos kilometros totales reccore el
                                                            // camion dado su velocidad y minutos en movimiento

        // Checking for the largest
        // Number between them
        int ans = (a > b) ? a : b;

        // Checking for a smallest number that
        // can de divided by both numbers
        while (true) {
            if (ans % a == 0 && ans % b == 0)
                break;
            ans++;
        }

        return ans; // Return the LCM
    }
}