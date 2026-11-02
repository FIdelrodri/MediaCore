package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IUsuarioDao;
import com.mediacore.dto.CambiarContrasenaRequest;
import com.mediacore.dto.ModificarDatosRequest;
import com.mediacore.dto.RegistroRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.Usuario;
import com.mediacore.util.Validaciones;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Registro de usuarios y datos del propio usuario logueado.
 *   POST /api/usuarios/registro     (publico)
 *   PUT  /api/usuarios/contrasena   (requiere sesion)
 *   PUT  /api/usuarios/datos        (requiere sesion)
 */
@WebServlet("/api/usuarios/*")
public class UsuarioServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final IUsuarioDao usuarioDao = DaoFactory.getUsuarioDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        if (ruta.length == 1 && ruta[0].equals("registro")) {
            registrar(req, resp);
        } else {
            noEncontrada(resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        if (ruta.length == 1 && ruta[0].equals("contrasena")) {
            cambiarContrasena(req, resp);
        } else if (ruta.length == 1 && ruta[0].equals("datos")) {
            modificarDatos(req, resp);
        } else {
            noEncontrada(resp);
        }
    }

    // POST /api/usuarios/registro
    private void registrar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegistroRequest datos = leerJson(req, resp, RegistroRequest.class);
        if (datos == null) {
            return;
        }

        String errorValidacion = Validaciones.usuario(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        // DTO -> Modelo
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(datos.getNombreUsuario());
        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setEmail(datos.getEmail());
        usuario.setContrasena(datos.getContrasena());

        try {
            usuarioDao.registrar(usuario);
            ok(resp, "Usuario registrado exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // PUT /api/usuarios/contrasena
    private void cambiarContrasena(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario logueado = usuarioEnSesion(req);
        if (logueado == null) {
            error(resp, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión para realizar esta acción.");
            return;
        }

        CambiarContrasenaRequest datos = leerJson(req, resp, CambiarContrasenaRequest.class);
        if (datos == null) {
            return;
        }

        if (datos.getContrasenaActual() == null || datos.getContrasenaActual().isEmpty()) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "La contraseña actual es incorrecta.");
            return;
        }
        String errorValidacion = Validaciones.contrasena(datos.getContrasenaNueva());
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        try {
            usuarioDao.cambiarContrasena(logueado.getIdUsuario(), datos.getContrasenaActual(), datos.getContrasenaNueva());
            ok(resp, "Contraseña actualizada exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // PUT /api/usuarios/datos
    private void modificarDatos(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario logueado = usuarioEnSesion(req);
        if (logueado == null) {
            error(resp, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión para realizar esta acción.");
            return;
        }

        ModificarDatosRequest datos = leerJson(req, resp, ModificarDatosRequest.class);
        if (datos == null) {
            return;
        }

        String errorValidacion = Validaciones.datosPersonales(datos.getNombre(), datos.getApellido(), datos.getEmail());
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        try {
            usuarioDao.modificarDatos(logueado.getIdUsuario(), datos.getNombre(), datos.getApellido(), datos.getEmail());

            // mantener actualizada la copia guardada en la sesion
            logueado.setNombre(datos.getNombre());
            logueado.setApellido(datos.getApellido());
            logueado.setEmail(datos.getEmail());

            ok(resp, "Datos actualizados exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }
}
