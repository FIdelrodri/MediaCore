// 5. Permiso.java
package proyecto_mediacore.model;

public class Permiso {
    private Integer idPermiso;
    private String nombre;
    private String descripcion;

    public Permiso() {}

    public Integer getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Integer idPermiso) { this.idPermiso = idPermiso; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}