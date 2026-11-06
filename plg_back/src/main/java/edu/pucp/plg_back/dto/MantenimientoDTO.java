
package edu.pucp.plg_back.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MantenimientoDTO {

    private LocalDateTime fechaInicioMantenimiento;
    private LocalDateTime fechaFinMantenimiento;

}