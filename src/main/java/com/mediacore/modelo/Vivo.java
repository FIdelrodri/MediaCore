package com.mediacore.modelo;

import java.util.Objects;

/** Transmision en vivo (tabla vivo). */
public class Vivo {

    private int idEnvivo;
    private String titulo;
    private String descripcion;
    private int duracion;
    private boolean retransmitido;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public Vivo() {
    }

    // Constructor completo
    public Vivo(int idEnvivo, String titulo, String descripcion, int duracion, boolean retransmitido) {
        this.idEnvivo = idEnvivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.duracion = duracion;
        this.retransmitido = retransmitido;
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

    public boolean isRetransmitido() {
        return retransmitido;
    }

    public void setRetransmitido(boolean retransmitido) {
        this.retransmitido = retransmitido;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vivo that = (Vivo) o;
        return idEnvivo == that.idEnvivo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEnvivo);
    }

    @Override
    public String toString() {
        return "Vivo{" +
                "idEnvivo=" + idEnvivo +
                ", titulo='" + titulo + "'" +
                ", descripcion='" + descripcion + "'" +
                ", duracion=" + duracion +
                ", retransmitido=" + retransmitido +
                "}";
    }
}
