package com.mediacore.controller;

import com.mediacore.dto.LoginRequest;
import com.mediacore.model.Usuario;
import com.mediacore.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Map<String, Object> respuesta = new HashMap<>();

        Usuario usuarioAutenticado = usuarioService.autenticar(
            loginRequest.getNombreUsuario(), 
            loginRequest.getContrasena()
        );

        if (usuarioAutenticado == null) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "Usuario o contraseña incorrectos.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        session.setAttribute("usuarioLogueado", usuarioAutenticado);

        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Inicio de sesión exitoso.");
        respuesta.put("idRol", usuarioAutenticado.getIdRol());
        respuesta.put("nombreUsuario", usuarioAutenticado.getNombreUsuario());

        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Sesión cerrada correctamente.");
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/sesion-actual")
    public ResponseEntity<Map<String, Object>> obtenerSesionActual(HttpSession session) {
        Map<String, Object> respuesta = new HashMap<>();
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuarioLogueado == null) {
            respuesta.put("autenticado", false);
            return ResponseEntity.ok(respuesta);
        }

        respuesta.put("autenticado", true);
        respuesta.put("idUsuario", usuarioLogueado.getIdUsuario());
        respuesta.put("nombreUsuario", usuarioLogueado.getNombreUsuario());
        respuesta.put("nombre", usuarioLogueado.getNombre());
        respuesta.put("idRol", usuarioLogueado.getIdRol());

        return ResponseEntity.ok(respuesta);
    }
}