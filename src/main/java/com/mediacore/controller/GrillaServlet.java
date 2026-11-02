package com.mediacore.controller;

import com.mediacore.dao.DaoException;
import com.mediacore.dao.IGrillaDao;
import com.mediacore.dto.AudioRequest;
import com.mediacore.dto.IntercambiarPosicionesRequest;
import com.mediacore.dto.MoverBloqueRequest;
import com.mediacore.factory.DaoFactory;
import com.mediacore.modelo.BloqueEnAire;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * La grilla (cola de bloques). Ver los roles de cada ruta en las constantes.
 *   GET    /api/grilla, /duracion-total, /en-aire, /historial?limite=   (consulta)
 *   POST   /api/grilla/bloques/{idBloque}                               (planilla)
 *   POST   /api/grilla/avanzar, /saltar                                 (planilla)
 *   POST   /api/grilla/posiciones/{pos}/bloques/{idBloque}              (planilla)
 *   POST   /api/grilla/posiciones/{pos}/duplicar                        (planilla)
 *   PUT    /api/grilla/intercambiar, /mover                             (planilla)
 *   PUT    /api/grilla/posiciones/{pos}/bloques/{idBloque}              (planilla)
 *   PUT    /api/grilla/audio                                            (audio)
 *   DELETE /api/grilla/posiciones/{pos}                                 (planilla)
 *   DELETE /api/grilla                                                  (admin)
 */
@WebServlet("/api/grilla/*")
public class GrillaServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private static final int[] ROLES_ADMIN = {1, 2};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_AUDIO = {1, 2, 4};
    private static final int[] ROLES_CONSULTA = {1, 2, 4, 5, 6};

    private final IGrillaDao grillaDao = DaoFactory.getGrillaDao();

    // ---------- GET ----------

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
                enviar(resp, HttpServletResponse.SC_OK, grillaDao.verConHorariosProyectados());

            } else if (ruta[0].equals("duracion-total")) {
                enviar(resp, HttpServletResponse.SC_OK, grillaDao.duracionTotal());

            } else if (ruta[0].equals("en-aire")) {
                BloqueEnAire bloque = grillaDao.bloqueEnAire();
                if (bloque == null) {
                    error(resp, HttpServletResponse.SC_NOT_FOUND, "No hay ningún bloque al aire.");
                } else {
                    enviar(resp, HttpServletResponse.SC_OK, bloque);
                }

            } else if (ruta[0].equals("historial")) {
                Integer limite = parametroEntero(req, "limite", 20);
                if (limite == null || limite < 1 || limite > 200) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "El límite debe estar entre 1 y 200.");
                    return;
                }
                enviar(resp, HttpServletResponse.SC_OK, grillaDao.verHistorial(limite));

            } else {
                noEncontrada(resp);
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // ---------- POST ----------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        boolean agregar = ruta.length == 2 && ruta[0].equals("bloques");
        boolean avanzar = ruta.length == 1 && ruta[0].equals("avanzar");
        boolean saltar = ruta.length == 1 && ruta[0].equals("saltar");
        boolean insertar = ruta.length == 4 && ruta[0].equals("posiciones") && ruta[2].equals("bloques");
        boolean duplicar = ruta.length == 3 && ruta[0].equals("posiciones") && ruta[2].equals("duplicar");
        if (!agregar && !avanzar && !saltar && !insertar && !duplicar) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, ROLES_PLANILLA)) {
            return;
        }

        try {
            if (avanzar) {
                grillaDao.avanzar();
                ok(resp, "La grilla avanzó al siguiente bloque.");

            } else if (saltar) {
                grillaDao.saltarBloqueActual();
                ok(resp, "Bloque actual saltado.");

            } else if (agregar) {
                Integer idBloque = aEntero(ruta[1]);
                if (idBloque == null) {
                    idInvalido(resp);
                    return;
                }
                grillaDao.agregarBloque(idBloque);
                responder(resp, HttpServletResponse.SC_CREATED, "ok", "Bloque agregado a la grilla.");

            } else if (insertar) {
                Integer posicion = aEntero(ruta[1]);
                Integer idBloque = aEntero(ruta[3]);
                if (posicion == null || idBloque == null) {
                    idInvalido(resp);
                    return;
                }
                int idPosicion = grillaDao.insertarEnPosicion(idBloque, posicion);
                Map<String, Object> cuerpo = mensaje("ok", "Bloque insertado en la grilla.");
                cuerpo.put("idPosicion", idPosicion);
                enviar(resp, HttpServletResponse.SC_CREATED, cuerpo);

            } else {
                Integer posicion = aEntero(ruta[1]);
                if (posicion == null) {
                    idInvalido(resp);
                    return;
                }
                grillaDao.duplicar(posicion);
                responder(resp, HttpServletResponse.SC_CREATED, "ok", "Posición duplicada.");
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // ---------- PUT ----------

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        boolean intercambiar = ruta.length == 1 && ruta[0].equals("intercambiar");
        boolean mover = ruta.length == 1 && ruta[0].equals("mover");
        boolean audio = ruta.length == 1 && ruta[0].equals("audio");
        boolean reemplazar = ruta.length == 4 && ruta[0].equals("posiciones") && ruta[2].equals("bloques");
        if (!intercambiar && !mover && !audio && !reemplazar) {
            noEncontrada(resp);
            return;
        }
        if (!verificarAcceso(req, resp, audio ? ROLES_AUDIO : ROLES_PLANILLA)) {
            return;
        }

        try {
            if (intercambiar) {
                IntercambiarPosicionesRequest datos = leerJson(req, resp, IntercambiarPosicionesRequest.class);
                if (datos == null) {
                    return;
                }
                if (datos.getPosicionA() == null || datos.getPosicionB() == null) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "Las posiciones A y B son obligatorias.");
                    return;
                }
                grillaDao.intercambiar(datos.getPosicionA(), datos.getPosicionB());
                ok(resp, "Posiciones intercambiadas.");

            } else if (mover) {
                MoverBloqueRequest datos = leerJson(req, resp, MoverBloqueRequest.class);
                if (datos == null) {
                    return;
                }
                if (datos.getOrigen() == null || datos.getDestino() == null) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "El origen y el destino son obligatorios.");
                    return;
                }
                grillaDao.mover(datos.getOrigen(), datos.getDestino());
                ok(resp, "Bloque movido.");

            } else if (audio) {
                AudioRequest datos = leerJson(req, resp, AudioRequest.class);
                if (datos == null) {
                    return;
                }
                if (datos.getAudio() == null) {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, "El audio es obligatorio.");
                    return;
                }
                grillaDao.cambiarAudio(datos.getAudio());
                ok(resp, "Audio actualizado en toda la grilla.");

            } else {
                Integer posicion = aEntero(ruta[1]);
                Integer idBloque = aEntero(ruta[3]);
                if (posicion == null || idBloque == null) {
                    idInvalido(resp);
                    return;
                }
                grillaDao.reemplazar(posicion, idBloque);
                ok(resp, "Bloque reemplazado.");
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }

    // ---------- DELETE ----------

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] ruta = segmentos(req);

        try {
            if (ruta.length == 0) {
                if (verificarAcceso(req, resp, ROLES_ADMIN)) {
                    grillaDao.vaciar();
                    ok(resp, "Grilla vaciada.");
                }

            } else if (ruta.length == 2 && ruta[0].equals("posiciones")) {
                if (!verificarAcceso(req, resp, ROLES_PLANILLA)) {
                    return;
                }
                Integer posicion = aEntero(ruta[1]);
                if (posicion == null) {
                    idInvalido(resp);
                    return;
                }
                grillaDao.quitarPosicion(posicion);
                ok(resp, "Posición quitada de la grilla.");

            } else {
                noEncontrada(resp);
            }
        } catch (DaoException e) {
            responderError(resp, e);
        }
    }
}
