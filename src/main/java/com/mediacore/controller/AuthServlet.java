package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IUsuarioDao;
import com.mediacore.dto.LoginRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.Usuario;
import com.mediacore.util.Roles;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inicio y cierre de sesion.
 *   POST /api/auth/login
 *   POST /api/auth/logout
 *   GET  /api/auth/sesion-actual
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final IUsuarioDao usuarioDao = DaoFactory.getUsuarioDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        if (ruta.length == 1 && ruta[0].equals("login")) {
            login(req, resp);
        } else if (ruta.length == 1 && ruta[0].equals("logout")) {
            logout(req, resp);
        } else {
            noEncontrada(resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        if (ruta.length == 1 && ruta[0].equals("sesion-actual")) {
            sesionActual(req, resp);
        } else {
            noEncontrada(resp);
        }
    }

    // POST /api/auth/login
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest datos = leerJson(req, resp, LoginRequest.class);
        if (datos == null) {
            return;
        }

        String nombreUsuario = datos.getNombreUsuario();
        String contrasena = datos.getContrasena();
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "Usuario o contraseña incorrectos.");
            return;
        }

        try {
            Usuario autenticado = usuarioDao.login(nombreUsuario, contrasena);
            if (autenticado == null) {
                error(resp, HttpServletResponse.SC_BAD_REQUEST, "Usuario o contraseña incorrectos.");
                return;
            }

            // Si ya habia una sesion, se le cambia el id para evitar el robo de sesion (session fixation)
            if (req.getSession(false) != null) {
                req.changeSessionId();
            }
            HttpSession sesion = req.getSession(true);
            sesion.setAttribute(Roles.ATRIBUTO_SESION, autenticado);

            Map<String, Object> respuesta = mensaje("ok", "Inicio de sesión exitoso.");
            respuesta.put("idRol", autenticado.getIdRol());
            respuesta.put("nombreUsuario", autenticado.getNombreUsuario());
            enviar(resp, HttpServletResponse.SC_OK, respuesta);

        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // POST /api/auth/logout
    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        ok(resp, "Sesión cerrada correctamente.");
    }

    // GET /api/auth/sesion-actual
    private void sesionActual(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario logueado = usuarioEnSesion(req);
        Map<String, Object> respuesta = new LinkedHashMap<>();

        if (logueado == null) {
            respuesta.put("autenticado", false);
        } else {
            respuesta.put("autenticado", true);
            respuesta.put("idUsuario", logueado.getIdUsuario());
            respuesta.put("nombreUsuario", logueado.getNombreUsuario());
            respuesta.put("nombre", logueado.getNombre());
            respuesta.put("apellido", logueado.getApellido());
            respuesta.put("email", logueado.getEmail());
            respuesta.put("idRol", logueado.getIdRol());
        }
        enviar(resp, HttpServletResponse.SC_OK, respuesta);
    }
}
