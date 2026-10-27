package com.mediacore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vista_grilla_activa")
public class VistaGrillaActiva {

    @Id
    @Column(name = "id_posicion")
    private Integer idPosicion;

    @Column(name = "id_bloque")
    private Integer idBloque;

    @Column(name = "tipo_contenido")
    private String tipoContenido;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    @Column(name = "hora_inicio")
    private LocalDateTime horaInicio;

    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    public VistaGrillaActiva() {}

    // Getters y Setters
    public Integer getIdPosicion() { return idPosicion; }
    public void setIdPosicion(Integer idPosicion) { this.idPosicion = idPosicion; }

    public Integer getIdBloque() { return idBloque; }
    public void setIdBloque(Integer idBloque) { this.idBloque = idBloque; }

    public String getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Integer getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(Integer duracionSegundos) { this.duracionSegundos = duracionSegundos; }

    public LocalDateTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalDateTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalDateTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalDateTime horaFin) { this.horaFin = horaFin; }
}