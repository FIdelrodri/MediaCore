package com.mediacore.modelo;

import java.util.Objects;

/** Cantidad de usuarios por rol (procedure contar_usuarios_por_rol). */
public class RolConteo {

    private int idRol;
    private String nombreRol;
    private long cantidadUsuarios;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public RolConteo() {
    }

    // Constructor completo
    public RolConteo(int idRol, String nombreRol, long cantidadUsuarios) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.cantidadUsuarios = cantidadUsuarios;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public long getCantidadUsuarios() {
        return cantidadUsuarios;
    }

    public void setCantidadUsuarios(long cantidadUsuarios) {
        this.cantidadUsuarios = cantidadUsuarios;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolConteo that = (RolConteo) o;
        return idRol == that.idRol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol);
    }

    @Override
    public String toString() {
        return "RolConteo{" +
                "idRol=" + idRol +
                ", nombreRol='" + nombreRol + "'" +
                ", cantidadUsuarios=" + cantidadUsuarios +
                "}";
    }
}
