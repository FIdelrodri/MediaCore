package com.mediacore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vivo")
public class Vivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envivo")
    private Integer idEnvivo;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "duracion", nullable = false)
    private Integer duracion;

    @Column(name = "retransmitido", nullable = false)
    private Boolean retrasmitido;

    public Vivo() {}

    // Getters y Setters
    public Integer getIdEnvivo() { return idEnvivo; }
    public void setIdEnvivo(Integer idEnvivo) { this.idEnvivo = idEnvivo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public Boolean getRetrasmitido() { return retrasmitido; }
    public void setRetrasmitido(Boolean retrasmitido) { this.retrasmitido = retrasmitido; }
}