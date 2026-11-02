package com.mediacore.dao;

import com.mediacore.modelo.Programa;

import java.util.List;

public interface IProgramaDao {

    // Devuelve el id generado
    int crear(Programa programa) throws DaoException;

    void modificar(Programa programa) throws DaoException;

    // El trigger de la base borra tambien el bloque del programa
    void eliminar(int idPrograma) throws DaoException;

    // Agrega el bloque del programa al final de la grilla
    void programar(int idPrograma) throws DaoException;

    List<Programa> listar() throws DaoException;

    List<Programa> buscarPorGenero(String genero) throws DaoException;

    List<Programa> masLargos(int cantidad) throws DaoException;
}
