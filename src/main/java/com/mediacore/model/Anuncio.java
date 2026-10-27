package com.mediacore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "anuncio")
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anuncio")
    private Integer idAnuncio;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "genero", length = 50)
    private String genero;

    @Column(name = "duracion")
    private Integer duracion;

    @Column(name = "empresa", length = 100)
    private String empresa;

    public Anuncio() {}

    // Getters y Setters
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