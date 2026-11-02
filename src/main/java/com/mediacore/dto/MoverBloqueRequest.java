package com.mediacore.dto;

public class MoverBloqueRequest {
    private Integer origen;
    private Integer destino;

    public MoverBloqueRequest() {}

    public Integer getOrigen() { return origen; }
    public void setOrigen(Integer origen) { this.origen = origen; }

    public Integer getDestino() { return destino; }
    public void setDestino(Integer destino) { this.destino = destino; }
}
