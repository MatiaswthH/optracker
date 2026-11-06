package edu.pucp.plg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AveriaEventoDTO {
    private String codigoCamion;
    private int tipoAveria;
    private int X;
    private int Y;
}
