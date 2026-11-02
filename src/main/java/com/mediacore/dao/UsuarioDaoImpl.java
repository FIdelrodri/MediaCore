package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.LogCambioRol;
import com.mediacore.modelo.Usuario;
import com.mediacore.util.ErrorBD;
import com.mediacore.util.Fechas;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDaoImpl extends BaseDao implements IUsuarioDao {

    @Override
    public Usuario login(String nombreUsuario, String contrasena) throws DaoException {
        String sql = "{CALL login_usuario(?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, nombreUsuario);
            cs.setString(2, contrasena);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setEmail(rs.getString("email"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setNombreRol(rs.getString("nombre_rol"));
                    return u;
                }
                return null;
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void registrar(Usuario usuario) throws DaoException {
        String sql = "{CALL registrar_usuario(?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, usuario.getNombreUsuario());
            cs.setString(2, usuario.getNombre());
            cs.setString(3, usuario.getApellido());
            cs.setString(4, usuario.getEmail());
            cs.setString(5, usuario.getContrasena());
            cs.execute();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                // La clave repetida puede ser el nombre de usuario o el email
                String mensaje = ErrorBD.mensaje(e);
                int posicion = mensaje == null ? -1 : mensaje.indexOf("for key");
                String clave = posicion >= 0 ? mensaje.substring(posicion) : "";
                if (clave.contains("email")) {
                    throw new DaoException(DaoException.Tipo.DUPLICADO, "El correo ya está registrado.", e);
                }
                throw new DaoException(DaoException.Tipo.DUPLICADO, "El nombre de usuario ya está registrado.", e);
            }
            throw traducir(e);
        }
    }

    @Override
    public void asignarRol(int idUsuario, int idRol) throws DaoException {
        ejecutar("{CALL asignar_rol_usuario(?, ?)}", idUsuario, idRol);
    }

    @Override
    public void eliminar(int idUsuario) throws DaoException {
        ejecutar("{CALL eliminar_usuario(?)}", idUsuario);
    }

    @Override
    public void cambiarContrasena(int idUsuario, String contrasenaActual, String contrasenaNueva) throws DaoException {
        String sql = "{CALL cambiar_contrasena(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setString(2, contrasenaActual);
            cs.setString(3, contrasenaNueva);
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void modificarDatos(int idUsuario, String nombre, String apellido, String email) throws DaoException {
        String sql = "{CALL modificar_datos_usuario(?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setString(2, nombre);
            cs.setString(3, apellido);
            cs.setString(4, email);
            cs.execute();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DaoException(DaoException.Tipo.DUPLICADO, "El correo ya está registrado.", e);
            }
            throw traducir(e);
        }
    }

    @Override
    public void degradarAUsuario(int idUsuario) throws DaoException {
        ejecutar("{CALL degradar_a_usuario(?)}", idUsuario);
    }

    @Override
    public List<Usuario> listar() throws DaoException {
        // listar_usuarios es el unico que trae tambien el nombre del rol
        return consultar("{CALL listar_usuarios()}", true, null);
    }

    @Override
    public List<Usuario> buscar(String texto) throws DaoException {
        return consultar("{CALL buscar_usuario(?)}", false, texto == null ? "" : texto.trim());
    }

    @Override
    public List<Usuario> listarRecientes(int dias) throws DaoException {
        return consultar("{CALL listar_usuarios_recientes(?)}", false, dias);
    }

    @Override
    public List<LogCambioRol> verLogCambiosRol(int limite) throws DaoException {
        List<LogCambioRol> log = new ArrayList<>();
        String sql = "{CALL ver_log_cambios_rol(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, limite);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    LogCambioRol item = new LogCambioRol();
                    item.setIdLog(rs.getInt("id_log"));
                    item.setIdUsuario(rs.getInt("id_usuario"));
                    item.setNombreUsuario(rs.getString("nombre_usuario"));
                    item.setRolAnterior(enteroONulo(rs, "rol_anterior"));
                    item.setRolNuevo(enteroONulo(rs, "rol_nuevo"));
                    item.setFecha(Fechas.iso(rs.getString("fecha")));
                    log.add(item);
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return log;
    }

    // ---- metodos privados de apoyo ----

    // Ejecuta un procedure que solo recibe enteros y no devuelve filas
    private void ejecutar(String sql, int... parametros) throws DaoException {
        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                cs.setInt(i + 1, parametros[i]);
            }
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    // Ejecuta un procedure de consulta de usuarios; parametro puede ser null, String o Integer
    private List<Usuario> consultar(String sql, boolean conRol, Object parametro) throws DaoException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            if (parametro instanceof String) {
                cs.setString(1, (String) parametro);
            } else if (parametro instanceof Integer) {
                cs.setInt(1, (Integer) parametro);
            }

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setEmail(rs.getString("email"));
                    u.setFechaRegistro(Fechas.iso(rs.getString("fecha_registro")));
                    u.setIdRol(rs.getInt("id_rol"));
                    if (conRol) {
                        u.setNombreRol(rs.getString("nombre_rol"));
                    }
                    usuarios.add(u);
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return usuarios;
    }
}
