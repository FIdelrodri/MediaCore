package com.mediacore.modelo;

import java.util.Objects;

/** Bloque que esta al aire en este momento (procedure bloque_en_aire). */
public class BloqueEnAire {

    private int orden;
    private int idPosicion;
    private int idBloque;
    private String tipoContenido;
    private String titulo;
    private int duracionSegundos;
    private String horaInicio;
    private String horaFin;
    private long segundosRestantes;
    private int audio;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public BloqueEnAire() {
    }

    // Constructor completo
    public BloqueEnAire(int orden, int idPosicion, int idBloque, String tipoContenido, String titulo, int duracionSegundos, String horaInicio, String horaFin, long segundosRestantes, int audio) {
        this.orden = orden;
        this.idPosicion = idPosicion;
        this.idBloque = idBloque;
        this.tipoContenido = tipoContenido;
        this.titulo = titulo;
        this.duracionSegundos = duracionSegundos;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.segundosRestantes = segundosRestantes;
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

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }

    public void setSegundosRestantes(long segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
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
        BloqueEnAire that = (BloqueEnAire) o;
        return idPosicion == that.idPosicion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPosicion);
    }

    @Override
    public String toString() {
        return "BloqueEnAire{" +
                "orden=" + orden +
                ", idPosicion=" + idPosicion +
                ", idBloque=" + idBloque +
                ", tipoContenido='" + tipoContenido + "'" +
                ", titulo='" + titulo + "'" +
                ", duracionSegundos=" + duracionSegundos +
                ", horaInicio='" + horaInicio + "'" +
                ", horaFin='" + horaFin + "'" +
                ", segundosRestantes=" + segundosRestantes +
                ", audio=" + audio +
                "}";
    }
}
