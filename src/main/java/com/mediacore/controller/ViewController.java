package com.mediacore.controller;

import com.mediacore.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/registro")
    public String mostrarPaginaRegistro() {
        return "registro";
    }

    @GetMapping("/login")
    public String mostrarPaginaLogin() {
        return "login";
    }

    // Ejemplo de ruta protegida desde el servidor Java por rol/sesión
    @GetMapping("/panel")
    public String mostrarPanelPrivado(HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");

        // Si no está logueado, se redirige al login
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        return "panel";
    }
}