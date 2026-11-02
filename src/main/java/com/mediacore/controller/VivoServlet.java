package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IVivoDao;
import com.mediacore.dto.VivoRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.Vivo;
import com.mediacore.util.Validaciones;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Transmisiones en vivo.
 *   GET    /api/vivos                      (1, 2, 3)
 *   GET    /api/vivos/retransmitidos       (publico)
 *   GET    /api/vivos/pendientes           (1, 2, 3)
 *   POST   /api/vivos                      (1, 2, 3)
 *   PUT    /api/vivos/{id}                 (1, 2, 3)
 *   DELETE /api/vivos/{id}                 (1, 2, 3)
 *   PUT    /api/vivos/{id}/retransmitido   (1, 2, 3)
 *   POST   /api/vivos/{id}/programar       (1, 2, 5)
 */
@WebServlet("/api/vivos/*")
public class VivoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private static final int[] ROLES_CONTENIDO = {1, 2, 3};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_CONSULTA = {1, 2, 3};

    private static final String NO_EXISTE = "El vivo no existe.";

    private final IVivoDao vivoDao = DaoFactory.getVivoDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);
        if (ruta.length > 1) {
            noEncontrada(resp);
            return;
        }

        try {
            if (ruta.length == 1 && ruta[0].equals("retransmitidos")) {
                // unica ruta publica
                enviar(resp, HttpServletResponse.SC_OK, vivoDao.listarRetransmitidos());
                return;
            }
            if (ruta.length == 0) {
                if (verificarAcceso(req, resp, ROLES_CONSULTA)) {
                    enviar(resp, HttpServletResponse.SC_OK, vivoDao.listar());
                }
            } else if (ruta[0].equals("pendientes")) {
                if (verificarAcceso(req, resp, ROLES_CONSULTA)) {
                    enviar(resp, HttpServletResponse.SC_OK, vivoDao.listarPendientes());
                }
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
                vivoDao.programar(id);
                ok(resp, "Vivo agregado a la grilla.");
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
        boolean modificar = ruta.length == 1;
        boolean retransmitido = ruta.length == 2 && ruta[1].equals("retransmitido");
        if (!modificar && !retransmitido) {
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

        if (retransmitido) {
            try {
                vivoDao.marcarComoRetransmitido(id);
                ok(resp, "Vivo marcado como retransmitido.");
            } catch (DaoException e) {
                responderError(resp, e, NO_EXISTE);
            }
            return;
        }

        VivoRequest datos = leerJson(req, resp, VivoRequest.class);
        if (datos == null) {
            return;
        }
        String errorValidacion = Validaciones.vivo(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }
        try {
            vivoDao.modificar(aModelo(id, datos));
            ok(resp, "Vivo actualizado exitosamente.");
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
            vivoDao.eliminar(id);
            ok(resp, "Vivo eliminado exitosamente.");
        } catch (DaoException e) {
            responderError(resp, e, NO_EXISTE);
        }
    }

    // POST /api/vivos
    private void crear(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!verificarAcceso(req, resp, ROLES_CONTENIDO)) {
            return;
        }
        VivoRequest datos = leerJson(req, resp, VivoRequest.class);
        if (datos == null) {
            return;
        }
        String errorValidacion = Validaciones.vivo(datos);
        if (errorValidacion != null) {
            error(resp, HttpServletResponse.SC_BAD_REQUEST, errorValidacion);
            return;
        }
        try {
            int idEnvivo = vivoDao.crear(aModelo(0, datos));
            Map<String, Object> cuerpo = mensaje("ok", "Vivo creado exitosamente.");
            cuerpo.put("idEnvivo", idEnvivo);
            enviar(resp, HttpServletResponse.SC_CREATED, cuerpo);
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // DTO -> Modelo
    private Vivo aModelo(int id, VivoRequest d) {
        return new Vivo(id, d.getTitulo(), d.getDescripcion(), d.getDuracion(), false);
    }
}
