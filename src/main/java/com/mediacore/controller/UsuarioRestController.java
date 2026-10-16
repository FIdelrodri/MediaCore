package com.mediacore.controller;

import com.mediacore.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private com.mediacore.service.Usuario usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody Usuario usuario) {
        Map<String, String> respuesta = new HashMap<>();

        // 1. Validaciones manuales con código puro de Java (longitudes y caracteres de contraseña)
        String errorValidacion = usuarioService.validarDatosUsuario(usuario);
        if (errorValidacion != null) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", errorValidacion);
            return ResponseEntity.badRequest().body(respuesta);
        }

        // 2. Validaciones de existencia en la base de datos
        if (usuarioService.existeNombreUsuario(usuario.getNombreUsuario())) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El nombre de usuario ya está registrado.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        if (usuarioService.existeEmail(usuario.getEmail())) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El correo ya está registrado.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        // 3. Ejecución del Stored Procedure en MySQL
        usuarioService.registrarUsuarioConProcedure(usuario);

        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Usuario registrado exitosamente.");
        return ResponseEntity.ok(respuesta);
    }
}