package com.mediacore.modelo;

import java.util.Objects;

/** Programa de la grilla (tabla programa). */
public class Programa {

    private int idPrograma;
    private String nombre;
    private String descripcion;
    private String genero;
    private int duracion;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public Programa() {
    }

    // Constructor completo
    public Programa(int idPrograma, String nombre, String descripcion, String genero, int duracion) {
        this.idPrograma = idPrograma;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.genero = genero;
        this.duracion = duracion;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdPrograma() {
        return idPrograma;
    }

    public void setIdPrograma(int idPrograma) {
        this.idPrograma = idPrograma;
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

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Programa that = (Programa) o;
        return idPrograma == that.idPrograma;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrograma);
    }

    @Override
    public String toString() {
        return "Programa{" +
                "idPrograma=" + idPrograma +
                ", nombre='" + nombre + "'" +
                ", descripcion='" + descripcion + "'" +
                ", genero='" + genero + "'" +
                ", duracion=" + duracion +
                "}";
    }
}
