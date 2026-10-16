package com.mediacore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/registro")
    public String mostrarPaginaRegistro() {
        // Devuelve la vista registro.html desde src/main/resources/templates/
        return "registro";
    }
}