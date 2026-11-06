package edu.pucp.plg_back.dto;

public class AveriaRequest {
    private String codigo;
    private String tipoAveria;

    public AveriaRequest(String codigo, String tipoAveria) {
        this.codigo = codigo;
        this.tipoAveria = tipoAveria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTipoAveria() {
        return tipoAveria;
    }
}
