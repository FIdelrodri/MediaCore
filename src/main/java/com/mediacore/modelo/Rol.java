package com.mediacore.modelo;

import java.util.Objects;

/** Rol de un usuario (tabla rol, fija). */
public class Rol {

    private int idRol;
    private String nombreRol;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public Rol() {
    }

    // Constructor completo
    public Rol(int idRol, String nombreRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
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

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol that = (Rol) o;
        return idRol == that.idRol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol);
    }

    @Override
    public String toString() {
        return "Rol{" +
                "idRol=" + idRol +
                ", nombreRol='" + nombreRol + "'" +
                "}";
    }
}
