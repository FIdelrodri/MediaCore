package com.mediacore.modelo;

import java.util.Objects;

/** Un bloque que ya salio al aire (procedure ver_historial_emision). */
public class EmisionHistorial {

    private int idHistorial;
    private int idBloque;
    private String tipoContenido;
    private String titulo;
    private String horaInicio;
    private String horaFinReal;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public EmisionHistorial() {
    }

    // Constructor completo
    public EmisionHistorial(int idHistorial, int idBloque, String tipoContenido, String titulo, String horaInicio, String horaFinReal) {
        this.idHistorial = idHistorial;
        this.idBloque = idBloque;
        this.tipoContenido = tipoContenido;
        this.titulo = titulo;
        this.horaInicio = horaInicio;
        this.horaFinReal = horaFinReal;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(int idHistorial) {
        this.idHistorial = idHistorial;
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

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFinReal() {
        return horaFinReal;
    }

    public void setHoraFinReal(String horaFinReal) {
        this.horaFinReal = horaFinReal;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmisionHistorial that = (EmisionHistorial) o;
        return idHistorial == that.idHistorial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idHistorial);
    }

    @Override
    public String toString() {
        return "EmisionHistorial{" +
                "idHistorial=" + idHistorial +
                ", idBloque=" + idBloque +
                ", tipoContenido='" + tipoContenido + "'" +
                ", titulo='" + titulo + "'" +
                ", horaInicio='" + horaInicio + "'" +
                ", horaFinReal='" + horaFinReal + "'" +
                "}";
    }
}
