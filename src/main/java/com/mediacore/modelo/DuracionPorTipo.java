package com.mediacore.modelo;

/** Cantidad y duracion total de los bloques de un tipo (procedure duracion_total_por_tipo). */
public class DuracionPorTipo {

    private String tipoContenido;
    private long cantidadBloques;
    private long segundosTotales;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public DuracionPorTipo() {
    }

    // Constructor completo
    public DuracionPorTipo(String tipoContenido, long cantidadBloques, long segundosTotales) {
        this.tipoContenido = tipoContenido;
        this.cantidadBloques = cantidadBloques;
        this.segundosTotales = segundosTotales;
    }

    // --- GETTERS Y SETTERS ---

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

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

    @Override
    public String toString() {
        return "DuracionPorTipo{" +
                "tipoContenido='" + tipoContenido + "'" +
                ", cantidadBloques=" + cantidadBloques +
                ", segundosTotales=" + segundosTotales +
                "}";
    }
}
