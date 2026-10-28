package com.mediacore.controller;

import com.mediacore.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class ViewController {

    // Método auxiliar de validación de acceso
    private boolean tieneAcceso(HttpSession session, Integer... rolesPermitidos) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        // Si no hay sesión o no tiene rol en BD, rechaza
        if (usuario == null || usuario.getIdRol() == null) {
            return false;
        }

        // Si no especificamos roles, cualquier usuario con rol ingresa
        if (rolesPermitidos.length == 0) {
            return true;
        }

        // Si especificamos roles (ej. 1), comprueba que coincida
        return Arrays.asList(rolesPermitidos).contains(usuario.getIdRol());
    }

    // --- VISTAS PÚBLICAS Y REDIRECCIÓN ---

    @GetMapping("/registro")
    public String mostrarPaginaRegistro() {
        return "registro";
    }

    @GetMapping("/login")
    public String mostrarPaginaLogin() {
        return "login";
    }

    @GetMapping("/pagina-inexistente")
    public String mostrarPaginaInexistente() {
        return "404"; // Carga templates/404.html
    }

    // --- VISTAS PROTEGIDAS ---

    // Vista de Panel
    @GetMapping("/panel")
    public String mostrarPanel(HttpSession session, Model model) {
        if (!tieneAcceso(session)) {
            return "redirect:/pagina-inexistente";
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuarioLogueado);
        return "panel"; // Carga templates/panel.html
    }

    // Vista de Administración
    @GetMapping("/administracion")
    public String mostrarAdministracion(HttpSession session, Model model) {
        if (!tieneAcceso(session, 1)) {
            return "redirect:/pagina-inexistente";
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuarioLogueado);
        return "administracion"; // Carga templates/administracion.html
    }

    // Vista de Programas (roles 1, 2 y 6)
    @GetMapping("/programas")
    public String mostrarProgramas(HttpSession session, Model model) {
        if (!tieneAcceso(session, 1, 2, 6)) {
            return "redirect:/pagina-inexistente";
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuarioLogueado);
        return "programas"; // Carga templates/programas.html
    }

    // Vista de Vivos (roles 1, 2 y 3)
    @GetMapping("/vivos")
    public String mostrarVivos(HttpSession session, Model model) {
        if (!tieneAcceso(session, 1, 2, 3)) {
            return "redirect:/pagina-inexistente";
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuario", usuarioLogueado);
        return "vivos"; // Carga templates/vivos.html
    }
}
