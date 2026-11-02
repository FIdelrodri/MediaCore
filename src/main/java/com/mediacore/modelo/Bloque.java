package com.mediacore.modelo;

import java.util.Objects;

/** Bloque de contenido que se puede poner en la grilla (vista vista_bloques). */
public class Bloque {

    private int idBloque;
    private String tipoContenido;
    private int idReferencia;
    private String titulo;
    private int duracion;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public Bloque() {
    }

    // Constructor completo
    public Bloque(int idBloque, String tipoContenido, int idReferencia, String titulo, int duracion) {
        this.idBloque = idBloque;
        this.tipoContenido = tipoContenido;
        this.idReferencia = idReferencia;
        this.titulo = titulo;
        this.duracion = duracion;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdBloque() {
        return idBloque;
    }

    public void setIdBloque(int idBloque) {
        this.idBloque = idBloque;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public int getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(int idReferencia) {
        this.idReferencia = idReferencia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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
        Bloque that = (Bloque) o;
        return idBloque == that.idBloque;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBloque);
    }

    @Override
    public String toString() {
        return "Bloque{" +
                "idBloque=" + idBloque +
                ", tipoContenido='" + tipoContenido + "'" +
                ", idReferencia=" + idReferencia +
                ", titulo='" + titulo + "'" +
                ", duracion=" + duracion +
                "}";
    }
}
