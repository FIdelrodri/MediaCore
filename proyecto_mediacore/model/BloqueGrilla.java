// 3. BloqueGrilla.java
package proyecto_mediacore.model;

import java.sql.Time;

public class BloqueGrilla {
    private Integer idBloque;
    private Integer idGrilla;
    private Integer idPrograma;
    private Time horaInicio;
    private Time horaFin;
    private Integer posicion;
    private String tipo;

    public BloqueGrilla() {}

    public Integer getIdBloque() { return idBloque; }
    public void setIdBloque(Integer idBloque) { this.idBloque = idBloque; }

    public Integer getIdGrilla() { return idGrilla; }
    public void setIdGrilla(Integer idGrilla) { this.idGrilla = idGrilla; }

    public Integer getIdPrograma() { return idPrograma; }
    public void setIdPrograma(Integer idPrograma) { this.idPrograma = idPrograma; }

    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }

    public Time getHoraFin() { return horaFin; }
    public void setHoraFin(Time horaFin) { this.horaFin = horaFin; }

    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer posicion) { this.posicion = posicion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}