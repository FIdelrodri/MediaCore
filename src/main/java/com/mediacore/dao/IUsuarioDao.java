package com.mediacore.dao;

import com.mediacore.modelo.LogCambioRol;
import com.mediacore.modelo.Usuario;

import java.util.List;

public interface IUsuarioDao {

    // Devuelve el usuario si las credenciales son correctas, o null si no lo son (procedure login_usuario)
    Usuario login(String nombreUsuario, String contrasena) throws DaoException;

    // Alta de un usuario nuevo con rol "usuario" (procedure registrar_usuario)
    void registrar(Usuario usuario) throws DaoException;

    void asignarRol(int idUsuario, int idRol) throws DaoException;

    void eliminar(int idUsuario) throws DaoException;

    void cambiarContrasena(int idUsuario, String contrasenaActual, String contrasenaNueva) throws DaoException;

    void modificarDatos(int idUsuario, String nombre, String apellido, String email) throws DaoException;

    // Devuelve al usuario al rol "usuario" (id 7)
    void degradarAUsuario(int idUsuario) throws DaoException;

    List<Usuario> listar() throws DaoException;

    List<Usuario> buscar(String texto) throws DaoException;

    List<Usuario> listarRecientes(int dias) throws DaoException;

    List<LogCambioRol> verLogCambiosRol(int limite) throws DaoException;
}
