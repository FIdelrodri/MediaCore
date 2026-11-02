package com.mediacore.modelo;

import java.util.Objects;

/** Vivo ya retransmitido tal como se muestra en la vista publica (sin el estado). */
public class VivoPublico {

    private int idEnvivo;
    private String titulo;
    private String descripcion;
    private int duracion;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public VivoPublico() {
    }

    // Constructor completo
    public VivoPublico(int idEnvivo, String titulo, String descripcion, int duracion) {
        this.idEnvivo = idEnvivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.duracion = duracion;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdEnvivo() {
        return idEnvivo;
    }

    public void setIdEnvivo(int idEnvivo) {
        this.idEnvivo = idEnvivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VivoPublico that = (VivoPublico) o;
        return idEnvivo == that.idEnvivo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEnvivo);
    }

    @Override
    public String toString() {
        return "VivoPublico{" +
                "idEnvivo=" + idEnvivo +
                ", titulo='" + titulo + "'" +
                ", descripcion='" + descripcion + "'" +
                ", duracion=" + duracion +
                "}";
    }
}
