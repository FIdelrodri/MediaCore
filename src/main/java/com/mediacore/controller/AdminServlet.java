package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IUsuarioDao;
import com.mediacore.dto.AsignarRolRequest;
import com.mediacore.dto.DegradarUsuarioRequest;
import com.mediacore.dto.EliminarUsuarioRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.Usuario;
import com.mediacore.util.Roles;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Administracion de usuarios. Solo el rol 1 (Administrador General).
 *   PUT    /api/admin/asignar-rol
 *   DELETE /api/admin/eliminar-usuario
 *   GET    /api/admin/usuarios
 *   GET    /api/admin/usuarios/buscar?texto=
 *   GET    /api/admin/usuarios/recientes?dias=
 *   PUT    /api/admin/degradar-usuario
 *   GET    /api/admin/log-cambios-rol?limite=
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final IUsuarioDao usuarioDao = DaoFactory.getUsuarioDao();

    // Solo el Administrador General; sin sesion tambien es 403 (igual que la API original)
    private boolean esAdministrador(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario logueado = usuarioEnSesion(req);
        if (logueado != null && logueado.getIdRol() == Roles.ADMINISTRADOR_GENERAL) {
            return true;
        }
        error(resp, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: Se requieren permisos de Administrador.");
        return false;
    }

    // Los procedures avisan "el usuario no existe" con un SIGNAL: aca se responde 400 con un mensaje fijo
    private void responderErrorUsuario(HttpServletResponse resp, DaoException e) throws IOException {
        if (e.getTipo() == DaoException.Tipo.NO_ENCONTRADO) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "El usuario especificado no existe.");
        } else {
            responderError(resp, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (!esAdministrador(req, resp)) {
            return;
        }

        try {
            if (ruta.length == 1 && ruta[0].equals("usuarios")) {
                enviar(resp, HttpServletResponse.SC_OK, usuarioDao.listar());

            } else if (ruta.length == 2 && ruta[0].equals("usuarios") && ruta[1].equals("buscar")) {
                String texto = req.getParameter("texto");
                enviar(resp, HttpServletResponse.SC_OK, usuarioDao.buscar(texto == null ? "" : texto));

            } else if (ruta.length == 2 && ruta[0].equals("usuarios") && ruta[1].equals("recientes")) {
                Integer dias = parametroEntero(req, "dias", 7);
                if (dias == null || dias < 1) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "La cantidad de días debe ser mayor a 0.");
                    return;
                }
                enviar(resp, HttpServletResponse.SC_OK, usuarioDao.listarRecientes(dias));

            } else if (ruta.length == 1 && ruta[0].equals("log-cambios-rol")) {
                Integer limite = parametroEntero(req, "limite", 50);
                if (limite == null || limite < 1) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "El límite debe ser mayor a 0.");
                    return;
                }
                enviar(resp, HttpServletResponse.SC_OK, usuarioDao.verLogCambiosRol(limite));

            } else {
                noEncontrada(resp);
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length != 1 || !(ruta[0].equals("asignar-rol") || ruta[0].equals("degradar-usuario"))) {
            noEncontrada(resp);
            return;
        }
        if (!esAdministrador(req, resp)) {
            return;
        }

        if (ruta[0].equals("asignar-rol")) {
            AsignarRolRequest datos = leerJson(req, resp, AsignarRolRequest.class);
            if (datos == null) {
                return;
            }
            if (datos.getIdUsuario() == null || datos.getIdRol() == null) {
                error(resp, HttpServletResponse.SC_BAD_REQUEST, "El ID de usuario y el ID de rol son obligatorios.");
                return;
            }
            try {
                usuarioDao.asignarRol(datos.getIdUsuario(), datos.getIdRol());
                ok(resp, "Rol actualizado exitosamente.");
            } catch (DaoException e) {
                responderErrorUsuario(resp, e);
            }

        } else {
            DegradarUsuarioRequest datos = leerJson(req, resp, DegradarUsuarioRequest.class);
            if (datos == null) {
                return;
            }
            if (datos.getIdUsuario() == null) {
                error(resp, HttpServletResponse.SC_BAD_REQUEST, "El ID de usuario es obligatorio.");
                return;
            }
            try {
                usuarioDao.degradarAUsuario(datos.getIdUsuario());
                ok(resp, "El usuario volvió al rol de Usuario.");
            } catch (DaoException e) {
                responderErrorUsuario(resp, e);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length != 1 || !ruta[0].equals("eliminar-usuario")) {
            noEncontrada(resp);
            return;
        }
        if (!esAdministrador(req, resp)) {
            return;
        }

        EliminarUsuarioRequest datos = leerJson(req, resp, EliminarUsuarioRequest.class);
        if (datos == null) {
            return;
        }
        if (datos.getIdUsuario() == null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "El ID de usuario a eliminar es obligatorio.");
            return;
        }
        try {
            usuarioDao.eliminar(datos.getIdUsuario());
            ok(resp, "Usuario eliminado exitosamente.");
        } catch (DaoException e) {
            responderErrorUsuario(resp, e);
        }
    }
}
