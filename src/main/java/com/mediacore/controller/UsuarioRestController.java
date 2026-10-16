package com.mediacore.controller;

import com.mediacore.model.Usuario;
import com.mediacore.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody Usuario usuario) {
        Map<String, String> respuesta = new HashMap<>();

        if (usuarioService.existeEmail(usuario.getEmail())) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El correo ya está registrado.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        usuarioService.registrarUsuario(usuario);
        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Usuario registrado exitosamente desde la API.");
        return ResponseEntity.ok(respuesta);
    }
}