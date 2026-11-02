package com.mediacore.modelo;

import java.util.Objects;

/** Anuncio publicitario (tabla anuncio). */
public class Anuncio {

    private int idAnuncio;
    private String nombre;
    private String descripcion;
    private String genero;
    private int duracion;
    private String empresa;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public Anuncio() {
    }

    // Constructor completo
    public Anuncio(int idAnuncio, String nombre, String descripcion, String genero, int duracion, String empresa) {
        this.idAnuncio = idAnuncio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.genero = genero;
        this.duracion = duracion;
        this.empresa = empresa;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(int idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anuncio that = (Anuncio) o;
        return idAnuncio == that.idAnuncio;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnuncio);
    }

    @Override
    public String toString() {
        return "Anuncio{" +
                "idAnuncio=" + idAnuncio +
                ", nombre='" + nombre + "'" +
                ", descripcion='" + descripcion + "'" +
                ", genero='" + genero + "'" +
                ", duracion=" + duracion +
                ", empresa='" + empresa + "'" +
                "}";
    }
}
