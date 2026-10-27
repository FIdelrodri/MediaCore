package com.mediacore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grilla")
public class Grilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_posicion")
    private Integer idPosicion;

    @Column(name = "id_bloque", nullable = false)
    private Integer idBloque;

    @Column(name = "duracion", nullable = false)
    private Integer duracion;

    @Column(name = "hora_inicio")
    private LocalDateTime horaInicio;

    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    public Grilla() {}

    // Getters y Setters
    public Integer getIdPosicion() { return idPosicion; }
    public void setIdPosicion(Integer idPosicion) { this.idPosicion = idPosicion; }

    public Integer getIdBloque() { return idBloque; }
    public void setIdBloque(Integer idBloque) { this.idBloque = idBloque; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public LocalDateTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalDateTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalDateTime horaFin) { this.horaFin = horaFin; }
}