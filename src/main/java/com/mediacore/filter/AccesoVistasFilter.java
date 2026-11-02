package com.mediacore.filter;

import com.mediacore.modelo.Usuario;
import com.mediacore.util.Roles;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Vistas privadas: deja pasar la pagina solo si hay sesion iniciada y el rol del usuario esta permitido.
 * Si no, redirige a /pagina-inexistente (la misma pagina 404 que usaba la app original, para no
 * revelar que la pagina existe).
 *
 *   /panel, /perfil  -> cualquier usuario con sesion
 *   /administracion  -> rol 1
 *   /programas       -> roles 1, 2, 6
 *   /vivos           -> roles 1, 2, 3
 *   /anuncios        -> roles 1, 2
 *   /grilla          -> roles 1, 2, 4, 5, 6
 */
@WebFilter(urlPatterns = { "/panel", "/administracion", "/programas", "/vivos", "/anuncios", "/grilla", "/perfil" })
public class AccesoVistasFilter implements Filter {

    private static final int[] CUALQUIERA = {};

    private static final Map<String, int[]> ROLES_POR_VISTA = new HashMap<>();

    static {
        ROLES_POR_VISTA.put("/panel", CUALQUIERA);
        ROLES_POR_VISTA.put("/perfil", CUALQUIERA);
        ROLES_POR_VISTA.put("/administracion", new int[] { 1 });
        ROLES_POR_VISTA.put("/programas", new int[] { 1, 2, 6 });
        ROLES_POR_VISTA.put("/vivos", new int[] { 1, 2, 3 });
        ROLES_POR_VISTA.put("/anuncios", new int[] { 1, 2 });
        ROLES_POR_VISTA.put("/grilla", new int[] { 1, 2, 4, 5, 6 });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Una pagina privada no se guarda en cache: si se cierra sesion, el boton "atras" no debe mostrarla
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        if (tieneAcceso(req)) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/pagina-inexistente");
        }
    }

    private boolean tieneAcceso(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        if (sesion == null) {
            return false;
        }
        Usuario usuario = (Usuario) sesion.getAttribute(Roles.ATRIBUTO_SESION);
        if (usuario == null) {
            return false;
        }

        int[] permitidos = ROLES_POR_VISTA.get(req.getServletPath());
        if (permitidos == null) {
            return false;
        }
        if (permitidos.length == 0) {
            return true;
        }
        for (int rol : permitidos) {
            if (usuario.getIdRol() == rol) {
                return true;
            }
        }
        return false;
    }
}
