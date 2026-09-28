// 4. Grilla.java
package proyecto_mediacore.model;

import java.sql.Date;

public class Grilla {
    private Integer idGrilla;
    private Date fecha;
    private Integer idUsuario;

    public Grilla() {}

    public Integer getIdGrilla() { return idGrilla; }
    public void setIdGrilla(Integer idGrilla) { this.idGrilla = idGrilla; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}