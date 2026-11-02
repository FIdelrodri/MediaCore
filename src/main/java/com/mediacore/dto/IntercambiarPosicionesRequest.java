package com.mediacore.dto;

public class IntercambiarPosicionesRequest {
    private Integer posicionA;
    private Integer posicionB;

    public IntercambiarPosicionesRequest() {}

    public Integer getPosicionA() { return posicionA; }
    public void setPosicionA(Integer posicionA) { this.posicionA = posicionA; }

    public Integer getPosicionB() { return posicionB; }
    public void setPosicionB(Integer posicionB) { this.posicionB = posicionB; }
}
