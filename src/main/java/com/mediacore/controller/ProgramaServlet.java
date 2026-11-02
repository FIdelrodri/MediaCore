package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IProgramaDao;
import com.mediacore.dto.ProgramaRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.Programa;
import com.mediacore.util.Validaciones;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Programas.
 *   GET    /api/programas               (1, 2, 6)
 *   GET    /api/programas/buscar?genero (1, 2, 6)
 *   GET    /api/programas/mas-largos?cantidad (1, 2, 6)
 *   POST   /api/programas               (1, 2, 6)
 *   PUT    /api/programas/{id}          (1, 2, 6)
 *   DELETE /api/programas/{id}          (1, 2, 6)
 *   POST   /api/programas/{id}/programar (1, 2, 5)
 */
@WebServlet("/api/programas/*")
public class ProgramaServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private static final int[] ROLES_CONTENIDO = {1, 2, 6};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_CONSULTA = {1, 2, 6};

    private static final String NO_EXISTE = "El programa no existe.";

    private final IProgramaDao programaDao = DaoFactory.getProgramaDao();

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
                enviar(resp, HttpServletResponse.SC_OK, programaDao.listar());

            } else if (ruta[0].equals("buscar")) {
                String genero = req.getParameter("genero");
                enviar(resp, HttpServletResponse.SC_OK, programaDao.buscarPorGenero(genero == null ? "" : genero));

            } else if (ruta[0].equals("mas-largos")) {
                Integer cantidad = parametroEntero(req, "cantidad", 5);
                if (cantidad == null || cantidad < 1) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "La cantidad debe ser mayor a 0.");
                    return;
                }
                enviar(resp, HttpServletResponse.SC_OK, programaDao.masLargos(cantidad));

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
                programaDao.programar(id);
                ok(resp, "Programa agregado a la grilla.");
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

        ProgramaRequest datos = leerJson(req, resp, ProgramaRequest.class);
        if (datos == null) {
            return;
        }
        String errorValidacion = Validaciones.programa(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        try {
            programaDao.modificar(aModelo(id, datos));
            ok(resp, "Programa actualizado exitosamente.");
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
            programaDao.eliminar(id);
            ok(resp, "Programa eliminado exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e, NO_EXISTE);
        }
    }

    // POST /api/programas
    private void crear(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!verificarAcceso(req, resp, ROLES_CONTENIDO)) {
            return;
        }
        ProgramaRequest datos = leerJson(req, resp, ProgramaRequest.class);
        if (datos == null) {
            return;
        }
        String errorValidacion = Validaciones.programa(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }

        try {
            int idPrograma = programaDao.crear(aModelo(0, datos));
            Map<String, Object> cuerpo = mensaje("ok", "Programa creado exitosamente.");
            cuerpo.put("idPrograma", idPrograma);
            enviar(resp, HttpServletResponse.SC_CREATED, cuerpo);
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // DTO -> Modelo
    private Programa aModelo(int id, ProgramaRequest d) {
        return new Programa(id, d.getNombre(), d.getDescripcion(), d.getGenero(), d.getDuracion());
    }
}
