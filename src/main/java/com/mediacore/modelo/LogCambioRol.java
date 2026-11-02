package com.mediacore.modelo;

import java.util.Objects;

/** Registro de auditoria de un cambio de rol (procedure ver_log_cambios_rol). */
public class LogCambioRol {

    private int idLog;
    private int idUsuario;
    private String nombreUsuario;
    private Integer rolAnterior;
    private Integer rolNuevo;
    private String fecha;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public LogCambioRol() {
    }

    // Constructor completo
    public LogCambioRol(int idLog, int idUsuario, String nombreUsuario, Integer rolAnterior, Integer rolNuevo, String fecha) {
        this.idLog = idLog;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.rolAnterior = rolAnterior;
        this.rolNuevo = rolNuevo;
        this.fecha = fecha;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Integer getRolAnterior() {
        return rolAnterior;
    }

    public void setRolAnterior(Integer rolAnterior) {
        this.rolAnterior = rolAnterior;
    }

    public Integer getRolNuevo() {
        return rolNuevo;
    }

    public void setRolNuevo(Integer rolNuevo) {
        this.rolNuevo = rolNuevo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogCambioRol that = (LogCambioRol) o;
        return idLog == that.idLog;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLog);
    }

    @Override
    public String toString() {
        return "LogCambioRol{" +
                "idLog=" + idLog +
                ", idUsuario=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + "'" +
                ", rolAnterior=" + rolAnterior +
                ", rolNuevo=" + rolNuevo +
                ", fecha='" + fecha + "'" +
                "}";
    }
}
