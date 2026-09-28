// 7. RelRolPermisos.java
package proyecto_mediacore.model;

public class RelRolPermiso {
    private Integer idRolPermiso;
    private Integer idRol;
    private Integer idPermiso;

    public RelRolPermiso() {}

    public Integer getIdRolPermiso() { return idRolPermiso; }
    public void setIdRolPermiso(Integer idRolPermiso) { this.idRolPermiso = idRolPermiso; }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }

    public Integer getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Integer idPermiso) { this.idPermiso = idPermiso; }
}