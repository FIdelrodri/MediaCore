package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IRolDao;
import com.mediacore.factory.DaoFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Consulta de roles (publica, como en la API original).
 *   GET /api/roles
 *   GET /api/roles/conteo-usuarios
 *   GET /api/roles/{id}/usuarios
 */
@WebServlet("/api/roles/*")
public class RolServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final IRolDao rolDao = DaoFactory.getRolDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        try {
            if (ruta.length == 0) {
                enviar(resp, HttpServletResponse.SC_OK, rolDao.listar());

            } else if (ruta.length == 1 && ruta[0].equals("conteo-usuarios")) {
                enviar(resp, HttpServletResponse.SC_OK, rolDao.contarUsuariosPorRol());

            } else if (ruta.length == 2 && ruta[1].equals("usuarios")) {
                Integer id = aEntero(ruta[0]);
                if (id == null) {
                    idInvalido(resp);
                    return;
                }
                if (!rolDao.existe(id)) {
                    error(resp, HttpServletResponse.SC_NOT_FOUND, "El rol no existe.");
                    return;
                }
                enviar(resp, HttpServletResponse.SC_OK, rolDao.listarUsuariosPorRol(id));

            } else {
                noEncontrada(resp);
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getMethod().equals("GET")) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            metodoNoPermitido(resp);
            return;
        }
        super.service(req, resp);
    }
}
