package com.mediacore.dto;

public class CambiarContrasenaRequest {
    private String contrasenaActual;
    private String contrasenaNueva;

    public CambiarContrasenaRequest() {}

    public String getContrasenaActual() { return contrasenaActual; }
    public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }

    public String getContrasenaNueva() { return contrasenaNueva; }
    public void setContrasenaNueva(String contrasenaNueva) { this.contrasenaNueva = contrasenaNueva; }
}
