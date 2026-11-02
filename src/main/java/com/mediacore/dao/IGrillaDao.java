package com.mediacore.dao;

import com.mediacore.modelo.BloqueEnAire;
import com.mediacore.modelo.DuracionGrilla;
import com.mediacore.modelo.EmisionHistorial;
import com.mediacore.modelo.PosicionGrilla;

import java.util.List;

/**
 * La grilla es una cola: la posicion 1 esta al aire y no se puede mover, intercambiar ni reemplazar.
 * Cada procedure maneja su propia transaccion en la base de datos.
 */
public interface IGrillaDao {

    // ---- Consultas ----
    List<PosicionGrilla> verConHorariosProyectados() throws DaoException;

    DuracionGrilla duracionTotal() throws DaoException;

    // Devuelve null si la grilla esta vacia
    BloqueEnAire bloqueEnAire() throws DaoException;

    List<EmisionHistorial> verHistorial(int limite) throws DaoException;

    // ---- Operaciones sobre la cola ----
    void agregarBloque(int idBloque) throws DaoException;

    // Termina el bloque al aire y sube el siguiente
    void avanzar() throws DaoException;

    void saltarBloqueActual() throws DaoException;

    void quitarPosicion(int posicion) throws DaoException;

    void vaciar() throws DaoException;

    // ---- Reordenamiento de la cola ----
    // Devuelve el id de la nueva fila
    int insertarEnPosicion(int idBloque, int posicion) throws DaoException;

    void intercambiar(int posicionA, int posicionB) throws DaoException;

    void mover(int origen, int destino) throws DaoException;

    void reemplazar(int posicion, int idBloqueNuevo) throws DaoException;

    void duplicar(int posicion) throws DaoException;

    // Cambia el audio (1 a 100) de toda la grilla
    void cambiarAudio(int audio) throws DaoException;
}
