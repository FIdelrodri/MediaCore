package com.mediacore.dao;

/**
 * Error de la capa de persistencia. El DAO traduce las SQLException tecnicas a este tipo de excepcion de
 * negocio, con un mensaje apto para mostrar al usuario (por ejemplo el texto de un SIGNAL de un procedure).
 */
public class DaoException extends Exception {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        VALIDACION,      // un procedure o trigger rechazo la operacion (SIGNAL)
        NO_ENCONTRADO,   // el registro indicado no existe
        DUPLICADO,       // violacion de una clave unica (email o usuario repetido)
        TECNICO          // fallo de conexion, driver, etc.
    }

    private final Tipo tipo;

    public DaoException(Tipo tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    public DaoException(Tipo tipo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
