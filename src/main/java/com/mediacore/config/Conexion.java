package com.mediacore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PATRON SINGLETON: existe una unica instancia de Conexion en toda la aplicacion.
 *
 * - El constructor es privado, nadie puede hacer "new Conexion()".
 * - getInstancia() es synchronized: aunque dos peticiones lleguen a la vez, la instancia se crea una sola vez.
 * - La instancia registra el driver de MySQL una sola vez y entrega las conexiones a la base.
 *
 * Cada llamada a getConexion() abre una conexion nueva. Asi dos peticiones simultaneas nunca comparten
 * (ni se cierran entre si) la misma conexion, y el try-with-resources de los DAOs puede cerrarla tranquilo.
 */
public class Conexion {

    // Configuracion de la base de datos (la misma que usaba application.properties)
    private static final String URL =
            "jdbc:mysql://localhost:3307/mediacore"
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            + "&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASS = "";

    // Instancia unica del Singleton
    private static Conexion instancia;

    // Constructor privado para evitar la instanciacion directa
    private Conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver de MySQL no encontrado: " + e.getMessage(), e);
        }
    }

    // Metodo de acceso global sincronizado
    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    // Devuelve una conexion activa a MySQL (quien la pide la cierra con try-with-resources)
    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
