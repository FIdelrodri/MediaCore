// 1. Anuncio.java
package proyecto_mediacore.model;

public class Anuncio {
    private Integer idAnuncio;
    private String nombre;
    private String descripcion;
    private String genero;
    private Integer duracion;
    private String empresa;

    public Anuncio() {}

    public Integer getIdAnuncio() { return idAnuncio; }
    public void setIdAnuncio(Integer idAnuncio) { this.idAnuncio = idAnuncio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
}