package com.mediacore.modelo;

import java.util.Objects;

/** Una posicion de la cola con su horario proyectado (procedure ver_grilla_con_horarios_proyectados). */
public class PosicionGrilla {

    private int orden;
    private int idPosicion;
    private int idBloque;
    private String tipoContenido;
    private String titulo;
    private int duracionSegundos;
    private String inicioProyectado;
    private String finProyectado;
    private int audio;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public PosicionGrilla() {
    }

    // Constructor completo
    public PosicionGrilla(int orden, int idPosicion, int idBloque, String tipoContenido, String titulo, int duracionSegundos, String inicioProyectado, String finProyectado, int audio) {
        this.orden = orden;
        this.idPosicion = idPosicion;
        this.idBloque = idBloque;
        this.tipoContenido = tipoContenido;
        this.titulo = titulo;
        this.duracionSegundos = duracionSegundos;
        this.inicioProyectado = inicioProyectado;
        this.finProyectado = finProyectado;
        this.audio = audio;
    }

    // --- GETTERS Y SETTERS ---

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public int getIdPosicion() {
        return idPosicion;
    }

    public void setIdPosicion(int idPosicion) {
        this.idPosicion = idPosicion;
    }

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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(int duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public String getInicioProyectado() {
        return inicioProyectado;
    }

    public void setInicioProyectado(String inicioProyectado) {
        this.inicioProyectado = inicioProyectado;
    }

    public String getFinProyectado() {
        return finProyectado;
    }

    public void setFinProyectado(String finProyectado) {
        this.finProyectado = finProyectado;
    }

    public int getAudio() {
        return audio;
    }

    public void setAudio(int audio) {
        this.audio = audio;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PosicionGrilla that = (PosicionGrilla) o;
        return idPosicion == that.idPosicion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPosicion);
    }

    @Override
    public String toString() {
        return "PosicionGrilla{" +
                "orden=" + orden +
                ", idPosicion=" + idPosicion +
                ", idBloque=" + idBloque +
                ", tipoContenido='" + tipoContenido + "'" +
                ", titulo='" + titulo + "'" +
                ", duracionSegundos=" + duracionSegundos +
                ", inicioProyectado='" + inicioProyectado + "'" +
                ", finProyectado='" + finProyectado + "'" +
                ", audio=" + audio +
                "}";
    }
}
