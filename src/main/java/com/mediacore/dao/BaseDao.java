package com.mediacore.dao;

import com.mediacore.util.ErrorBD;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades comunes a todos los DAO: traduccion de errores de MySQL y lectura de columnas.
 * Aca NO hay SQL: toda la persistencia se hace llamando a stored procedures con CallableStatement.
 */
public abstract class BaseDao {

    private static final Logger LOG = Logger.getLogger(BaseDao.class.getName());

    // Convierte una SQLException en una DaoException con un mensaje entendible
    protected DaoException traducir(SQLException e) {
        String mensaje = ErrorBD.mensaje(e);

        // SQLSTATE 45000 = SIGNAL escrito a mano en un procedure o trigger (regla de negocio)
        if ("45000".equals(e.getSQLState())) {
            if (mensaje != null && mensaje.toLowerCase().contains("no existe")) {
                return new DaoException(DaoException.Tipo.NO_ENCONTRADO, mensaje, e);
            }
            return new DaoException(DaoException.Tipo.VALIDACION, mensaje, e);
        }

        // 1062 = Duplicate entry (clave unica repetida)
        if (e.getErrorCode() == 1062) {
            return new DaoException(DaoException.Tipo.DUPLICADO, mensaje, e);
        }

        LOG.log(Level.SEVERE, "Error tecnico en la base de datos", e);
        return new DaoException(DaoException.Tipo.TECNICO, "Error técnico al acceder a la base de datos.", e);
    }

    // getInt devuelve 0 si la columna es NULL, que es lo que queremos para los enteros del modelo
    protected int entero(ResultSet rs, String columna) throws SQLException {
        return rs.getInt(columna);
    }

    // Entero que puede ser NULL (por ejemplo rol_anterior en el log de roles)
    protected Integer enteroONulo(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }
}
