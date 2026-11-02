package com.mediacore.dao;

import com.mediacore.modelo.Bloque;
import com.mediacore.modelo.DuracionPorTipo;

import java.util.List;

public interface IBloqueDao {

    // Id del bloque de un contenido, o null si no existe
    Integer obtenerId(String tipo, int idReferencia) throws DaoException;

    List<Bloque> listar() throws DaoException;

    List<Bloque> listarPorTipo(String tipo) throws DaoException;

    // Bloques que no estan en la grilla
    List<Bloque> listarNoUsados() throws DaoException;

    List<DuracionPorTipo> duracionTotalPorTipo() throws DaoException;

    // Vuelve a leer la duracion desde el contenido original
    void recalcularDuracion(int idBloque) throws DaoException;

    // Borra los bloques cuyo contenido ya no existe y devuelve cuantos borro
    int limpiarHuerfanos() throws DaoException;
}
