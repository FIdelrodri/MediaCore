package com.mediacore.dao;

import com.mediacore.modelo.Vivo;
import com.mediacore.modelo.VivoPublico;

import java.util.List;

public interface IVivoDao {

    // Devuelve el id generado
    int crear(Vivo vivo) throws DaoException;

    // El trigger before_update_vivo rechaza cambiar la duracion de un vivo ya retransmitido
    void modificar(Vivo vivo) throws DaoException;

    void eliminar(int idEnvivo) throws DaoException;

    void marcarComoRetransmitido(int idEnvivo) throws DaoException;

    // Agrega el bloque del vivo al final de la grilla
    void programar(int idEnvivo) throws DaoException;

    List<Vivo> listar() throws DaoException;

    List<Vivo> listarPendientes() throws DaoException;

    List<VivoPublico> listarRetransmitidos() throws DaoException;
}
