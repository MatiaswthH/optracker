package edu.pucp.plg_back.model;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Posicion implements Serializable {
    private int X;
    private int Y;
    private int destino;
}
