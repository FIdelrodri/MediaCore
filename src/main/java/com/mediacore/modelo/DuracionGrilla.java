package com.mediacore.modelo;

/** Totales de la grilla (procedure duracion_total_grilla). */
public class DuracionGrilla {

    private long cantidadBloques;
    private long segundosTotales;
    private long segundosRestantes;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public DuracionGrilla() {
    }

    // Constructor completo
    public DuracionGrilla(long cantidadBloques, long segundosTotales, long segundosRestantes) {
        this.cantidadBloques = cantidadBloques;
        this.segundosTotales = segundosTotales;
        this.segundosRestantes = segundosRestantes;
    }

    // --- GETTERS Y SETTERS ---

    public long getCantidadBloques() {
        return cantidadBloques;
    }

    public void setCantidadBloques(long cantidadBloques) {
        this.cantidadBloques = cantidadBloques;
    }

    public long getSegundosTotales() {
        return segundosTotales;
    }

    public void setSegundosTotales(long segundosTotales) {
        this.segundosTotales = segundosTotales;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }

    public void setSegundosRestantes(long segundosRestantes) {
        this.segundosRestantes = segundosRestantes;
    }

    @Override
    public String toString() {
        return "DuracionGrilla{" +
                "cantidadBloques=" + cantidadBloques +
                ", segundosTotales=" + segundosTotales +
                ", segundosRestantes=" + segundosRestantes +
                "}";
    }
}
