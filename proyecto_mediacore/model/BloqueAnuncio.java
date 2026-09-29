// 2. BloqueAnuncio.java
package proyecto_mediacore.model;

import java.sql.Time;

public class BloqueAnuncio {
    private Integer idBloqueAnuncio;
    private Integer idAnuncio;
    private Integer idGrilla;
    private Time horaInicio;
    private Time horaFin;
    private Integer posicion;

    public BloqueAnuncio() {}

    public Integer getIdBloqueAnuncio() { return idBloqueAnuncio; }
    public void setIdBloqueAnuncio(Integer idBloqueAnuncio) { this.idBloqueAnuncio = idBloqueAnuncio; }

    public Integer getIdAnuncio() { return idAnuncio; }
    public void setIdAnuncio(Integer idAnuncio) { this.idAnuncio = idAnuncio; }

    public Integer getIdGrilla() { return idGrilla; }
    public void setIdGrilla(Integer idGrilla) { this.idGrilla = idGrilla; }

    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }

    public Time getHoraFin() { return horaFin; }
    public void setHoraFin(Time horaFin) { this.horaFin = horaFin; }

    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer posicion) { this.posicion = posicion; }
}