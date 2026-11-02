package com.mediacore.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Entrega las paginas HTML. Las vistas estan en WEB-INF/vistas (el navegador no puede pedirlas directo),
 * asi que la unica forma de verlas es pasar por aca. El control de quien puede ver cada pagina privada
 * lo hace AccesoVistasFilter ANTES de llegar a este servlet.
 */
@WebServlet(urlPatterns = { "", "/login", "/registro", "/vivos-retransmitidos", "/pagina-inexistente",
        "/panel", "/administracion", "/programas", "/vivos", "/anuncios", "/grilla", "/perfil" })
public class VistaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // URL -> archivo dentro de WEB-INF/vistas
    private static final Map<String, String> VISTAS = new HashMap<>();

    static {
        VISTAS.put("", "vivos_retransmitidos"); // "" = raiz de la aplicacion (OJO: "/" seria el servlet por defecto)
        VISTAS.put("/vivos-retransmitidos", "vivos_retransmitidos");
        VISTAS.put("/login", "login");
        VISTAS.put("/registro", "registro");
        VISTAS.put("/pagina-inexistente", "404");
        VISTAS.put("/panel", "panel");
        VISTAS.put("/administracion", "administracion");
        VISTAS.put("/programas", "programas");
        VISTAS.put("/vivos", "vivos");
        VISTAS.put("/anuncios", "anuncios");
        VISTAS.put("/grilla", "grilla");
        VISTAS.put("/perfil", "perfil");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ruta = req.getServletPath();
        String vista = VISTAS.get(ruta);
        if (vista == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (ruta.equals("/pagina-inexistente")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        req.getRequestDispatcher("/WEB-INF/vistas/" + vista + ".html").forward(req, resp);
    }
}
