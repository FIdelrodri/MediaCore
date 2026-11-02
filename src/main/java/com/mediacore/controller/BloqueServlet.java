package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IBloqueDao;
import com.mediacore.factory.DaoFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Bloques de contenido.
 *   GET    /api/bloques?tipo=                    (1, 2, 5, 6)
 *   GET    /api/bloques/no-usados                (1, 2, 5, 6)
 *   GET    /api/bloques/duracion-por-tipo        (1, 2, 5, 6)
 *   GET    /api/bloques/buscar?tipo&idReferencia (1, 2, 5, 6)
 *   PUT    /api/bloques/{id}/recalcular-duracion (1, 6)
 *   DELETE /api/bloques/huerfanos                (1, 6)
 */
@WebServlet("/api/bloques/*")
public class BloqueServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private static final int[] ROLES_CONSULTA = {1, 2, 5, 6};
    private static final int[] ROLES_MANTENIMIENTO = {1, 6};

    private static final String TIPO_INVALIDO = "El tipo debe ser programa, vivo o anuncio.";

    private final IBloqueDao bloqueDao = DaoFactory.getBloqueDao();

    private boolean tipoValido(String tipo) {
        return "programa".equals(tipo) || "vivo".equals(tipo) || "anuncio".equals(tipo);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length > 1) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, ROLES_CONSULTA)) {
            return;
        }

        try {
            if (ruta.length == 0) {
                String tipo = req.getParameter("tipo");
                if (tipo == null || tipo.trim().isEmpty()) {
                    enviar(resp, HttpServletResponse.SC_OK, bloqueDao.listar());
                } else if (!tipoValido(tipo.trim())) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, TIPO_INVALIDO);
                } else {
                    enviar(resp, HttpServletResponse.SC_OK, bloqueDao.listarPorTipo(tipo.trim()));
                }

            } else if (ruta[0].equals("no-usados")) {
                enviar(resp, HttpServletResponse.SC_OK, bloqueDao.listarNoUsados());

            } else if (ruta[0].equals("duracion-por-tipo")) {
                enviar(resp, HttpServletResponse.SC_OK, bloqueDao.duracionTotalPorTipo());

            } else if (ruta[0].equals("buscar")) {
                buscar(req, resp);

            } else {
                noEncontrada(resp);
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // GET /api/bloques/buscar?tipo=programa&idReferencia=3
    private void buscar(HttpServletRequest req, HttpServletResponse resp) throws IOException, DaoException {
        String tipo = req.getParameter("tipo");
        Integer idReferencia = aEntero(String.valueOf(req.getParameter("idReferencia")).trim());
        if (tipo == null || !tipoValido(tipo)) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, TIPO_INVALIDO);
            return;
        }
        if (idReferencia == null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, "El parámetro idReferencia es obligatorio y debe ser un número.");
            return;
        }

        Integer idBloque = bloqueDao.obtenerId(tipo, idReferencia);
        if (idBloque == null) {
            error(resp, HttpServletResponse.SC_NOT_FOUND, "No existe un bloque para ese contenido.");
            return;
        }
        Map<String, Object> cuerpo = mensaje("ok", "Bloque encontrado.");
        cuerpo.put("idBloque", idBloque);
        enviar(resp, HttpServletResponse.SC_OK, cuerpo);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length != 2 || !ruta[1].equals("recalcular-duracion")) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, ROLES_MANTENIMIENTO)) {
            return;
        }
        Integer id = aEntero(ruta[0]);
        if (id == null) {
            idInvalido(resp);
            return;
        }
        try {
            bloqueDao.recalcularDuracion(id);
            ok(resp, "Duración del bloque recalculada exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e, "El bloque no existe.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length != 1 || !ruta[0].equals("huerfanos")) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, ROLES_MANTENIMIENTO)) {
            return;
        }
        try {
            int eliminados = bloqueDao.limpiarHuerfanos();
            Map<String, Object> cuerpo = mensaje("ok", "Limpieza de bloques huérfanos finalizada.");
            cuerpo.put("bloquesEliminados", eliminados);
            enviar(resp, HttpServletResponse.SC_OK, cuerpo);
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }
}
