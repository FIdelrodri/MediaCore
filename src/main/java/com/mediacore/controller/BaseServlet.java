package com.mediacore.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mediacore.dao.DaoException;
import com.mediacore.modelo.Usuario;
import com.mediacore.util.Roles;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet base de la API: concentra lo que comparten todos los controladores (leer el JSON del body,
 * escribir respuestas JSON, leer la sesion y verificar el rol del usuario).
 * Los servlets concretos solo se ocupan de decidir que operacion del DAO corresponde a cada peticion.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // serializeNulls: los campos null viajan como null en el JSON (igual que antes)
    protected final Gson gson = new GsonBuilder().serializeNulls().create();

    // Toda peticion a la API se lee y se responde en UTF-8 / JSON
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        super.service(req, resp);
    }

    // =========================
    // RUTA
    // =========================

    // /api/programas/5/programar  ->  ["5", "programar"]   |   /api/programas  ->  []
    protected String[] segmentos(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) {
            return new String[0];
        }
        return info.substring(1).split("/");
    }

    // Devuelve el entero o null si el texto no es un numero
    protected Integer aEntero(String texto) {
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Parametro de query (?limite=20) como entero; si no viene devuelve el valor por defecto, si es invalido null
    protected Integer parametroEntero(HttpServletRequest req, String nombre, int porDefecto) {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            return porDefecto;
        }
        return aEntero(valor.trim());
    }

    // =========================
    // BODY JSON
    // =========================

    private String leerBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea);
            }
        }
        return sb.toString();
    }

    // Convierte el body JSON en un objeto; si es invalido responde 400 y devuelve null
    protected <T> T leerJson(HttpServletRequest req, HttpServletResponse resp, Class<T> clase) throws IOException {
        try {
            T objeto = gson.fromJson(leerBody(req), clase);
            if (objeto != null) {
                return objeto;
            }
        } catch (JsonParseException e) {
            // cae al error de abajo
        }
        responder(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "El cuerpo de la petición no es un JSON válido.");
        return null;
    }

    // =========================
    // RESPUESTAS
    // =========================

    protected void enviar(HttpServletResponse resp, int estado, Object cuerpo) throws IOException {
        resp.setStatus(estado);
        resp.getWriter().write(gson.toJson(cuerpo));
    }

    protected Map<String, Object> mensaje(String estado, String texto) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("status", estado);
        cuerpo.put("mensaje", texto);
        return cuerpo;
    }

    protected void responder(HttpServletResponse resp, int estado, String estadoJson, String texto) throws IOException {
        enviar(resp, estado, mensaje(estadoJson, texto));
    }

    protected void ok(HttpServletResponse resp, String texto) throws IOException {
        responder(resp, HttpServletResponse.SC_OK, "ok", texto);
    }

    protected void error(HttpServletResponse resp, int estado, String texto) throws IOException {
        responder(resp, estado, "error", texto);
    }

    protected void noEncontrada(HttpServletResponse resp) throws IOException {
        error(resp, HttpServletResponse.SC_NOT_FOUND, "Recurso no encontrado.");
    }

    protected void metodoNoPermitido(HttpServletResponse resp) throws IOException {
        error(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Método no permitido para esta ruta.");
    }

    protected void idInvalido(HttpServletResponse resp) throws IOException {
        error(resp, HttpServletResponse.SC_BAD_REQUEST, "El identificador debe ser un número.");
    }

    // Traduce un error del DAO a una respuesta HTTP.
    //  - mensajeNoExiste != null: si el registro no existe responde 404 con ese mensaje.
    //  - mensajeNoExiste == null: los errores de los procedures se responden como 400 con su propio mensaje.
    protected void responderError(HttpServletResponse resp, DaoException e, String mensajeNoExiste) throws IOException {
        switch (e.getTipo()) {
            case NO_ENCONTRADO:
                if (mensajeNoExiste != null) {
                    error(resp, HttpServletResponse.SC_NOT_FOUND, mensajeNoExiste);
                } else {
                    error(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                }
                break;
            case TECNICO:
                error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                break;
            default:
                error(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    protected void responderError(HttpServletResponse resp, DaoException e) throws IOException {
        responderError(resp, e, null);
    }

    // =========================
    // SESION Y ROLES
    // =========================

    protected Usuario usuarioEnSesion(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        if (sesion == null) {
            return null;
        }
        return (Usuario) sesion.getAttribute(Roles.ATRIBUTO_SESION);
    }

    // Devuelve true si el usuario tiene sesion y uno de los roles; si no, responde 401 o 403 y devuelve false
    protected boolean verificarAcceso(HttpServletRequest req, HttpServletResponse resp, int... rolesPermitidos)
            throws IOException {
        Usuario logueado = usuarioEnSesion(req);
        if (logueado == null) {
            error(resp, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión para realizar esta acción.");
            return false;
        }
        for (int rol : rolesPermitidos) {
            if (logueado.getIdRol() == rol) {
                return true;
            }
        }
        error(resp, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: tu rol no tiene permisos para esta acción.");
        return false;
    }
}
