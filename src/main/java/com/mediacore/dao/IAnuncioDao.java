package com.mediacore.dao;

import com.mediacore.modelo.Anuncio;

import java.util.List;

public interface IAnuncioDao {

    // Devuelve el id generado
    int crear(Anuncio anuncio) throws DaoException;

    void modificar(Anuncio anuncio) throws DaoException;

    void eliminar(int idAnuncio) throws DaoException;

    // Agrega el bloque del anuncio al final de la grilla
    void programar(int idAnuncio) throws DaoException;

    List<Anuncio> listar() throws DaoException;

    List<Anuncio> listarPorEmpresa(String empresa) throws DaoException;

    List<Anuncio> listarPorDuracion(int minimo, int maximo) throws DaoException;
}
