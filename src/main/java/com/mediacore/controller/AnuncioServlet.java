package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IAnuncioDao;
import com.mediacore.dto.AnuncioRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.Anuncio;
import com.mediacore.util.Validaciones;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Anuncios.
 *   GET    /api/anuncios?empresa=           (1, 2)
 *   GET    /api/anuncios/por-duracion?min&max (1, 2)
 *   POST   /api/anuncios                    (1, 2)
 *   PUT    /api/anuncios/{id}               (1, 2)
 *   DELETE /api/anuncios/{id}               (1, 2)
 *   POST   /api/anuncios/{id}/programar     (1, 2, 5)
 */
@WebServlet("/api/anuncios/*")
public class AnuncioServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private static final int[] ROLES_CONTENIDO = {1, 2};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_CONSULTA = {1, 2};

    private static final String NO_EXISTE = "El anuncio no existe.";

    private final IAnuncioDao anuncioDao = DaoFactory.getAnuncioDao();

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
                String empresa = req.getParameter("empresa");
                if (empresa == null || empresa.trim().isEmpty()) {
                    enviar(resp, HttpServletResponse.SC_OK, anuncioDao.listar());
                } else {
                    enviar(resp, HttpServletResponse.SC_OK, anuncioDao.listarPorEmpresa(empresa));
                }

            } else if (ruta[0].equals("por-duracion")) {
                Integer min = aEntero(String.valueOf(req.getParameter("min")).trim());
                Integer max = aEntero(String.valueOf(req.getParameter("max")).trim());
                if (min == null || max == null) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "Los parámetros min y max son obligatorios y deben ser números.");
                    return;
                }
                if (min < 0 || max < min) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "El rango de duración no es válido.");
                    return;
                }
                enviar(resp, HttpServletResponse.SC_OK, anuncioDao.listarPorDuracion(min, max));

            } else {
                noEncontrada(resp);
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        if (ruta.length == 0) {
            crear(req, resp);

        } else if (ruta.length == 2 && ruta[1].equals("programar")) {
            if (!verificarAcceso(req, resp, ROLES_PLANILLA)) {
                return;
            }
            Integer id = aEntero(ruta[0]);
            if (id == null) {
                idInvalido(resp);
                return;
            }
            try {
                anuncioDao.programar(id);
                ok(resp, "Anuncio agregado a la grilla.");
            } catch (DaoException e) {
                responderError(resp, e, NO_EXISTE);
            }

        } else {
            noEncontrada(resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length != 1) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, ROLES_CONTENIDO)) {
            return;
        }
        Integer id = aEntero(ruta[0]);
        if (id == null) {
            idInvalido(resp);
            return;
        }

        AnuncioRequest datos = leerJson(req, resp, AnuncioRequest.class);
        if (datos == null) {
            return;
        }
        String errorValidacion = Validaciones.anuncio(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }
        try {
            anuncioDao.modificar(aModelo(id, datos));
            ok(resp, "Anuncio actualizado exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e, NO_EXISTE);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length != 1) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, ROLES_CONTENIDO)) {
            return;
        }
        Integer id = aEntero(ruta[0]);
        if (id == null) {
            idInvalido(resp);
            return;
        }
        try {
            anuncioDao.eliminar(id);
            ok(resp, "Anuncio eliminado exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e, NO_EXISTE);
        }
    }

    // POST /api/anuncios
    private void crear(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!verificarAcceso(req, resp, ROLES_CONTENIDO)) {
            return;
        }
        AnuncioRequest datos = leerJson(req, resp, AnuncioRequest.class);
        if (datos == null) {
            return;
        }
        String errorValidacion = Validaciones.anuncio(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }
        try {
            int idAnuncio = anuncioDao.crear(aModelo(0, datos));
            Map<String, Object> cuerpo = mensaje("ok", "Anuncio creado exitosamente.");
            cuerpo.put("idAnuncio", idAnuncio);
            enviar(resp, HttpServletResponse.SC_CREATED, cuerpo);
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // DTO -> Modelo
    private Anuncio aModelo(int id, AnuncioRequest d) {
        return new Anuncio(id, d.getNombre(), d.getDescripcion(), d.getGenero(), d.getDuracion(), d.getEmpresa());
    }
}
