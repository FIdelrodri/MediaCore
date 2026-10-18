package com.mediacore.dto;

public class AsignarRolRequest {
    private Integer idUsuario;
    private Integer idRol;

    public AsignarRolRequest() {}

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
}