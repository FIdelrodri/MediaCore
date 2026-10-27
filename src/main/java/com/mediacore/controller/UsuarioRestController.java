package com.mediacore.controller;

import com.mediacore.dto.CambiarContrasenaRequest;
import com.mediacore.dto.ModificarDatosRequest;
import com.mediacore.model.Usuario;
import com.mediacore.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // Errores lanzados por los procedures (SIGNAL) y por las validaciones del service
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(IllegalArgumentException e) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "error");
        respuesta.put("mensaje", e.getMessage());
        return ResponseEntity.badRequest().body(respuesta);
    }

    private ResponseEntity<Map<String, String>> sinSesion() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "error");
        respuesta.put("mensaje", "Debes iniciar sesión para realizar esta acción.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }

    // API: cambiar la contraseña del usuario logueado (procedure cambiar_contrasena)
    @PutMapping("/contrasena")
    public ResponseEntity<Map<String, String>> cambiarContrasena(@RequestBody CambiarContrasenaRequest request, HttpSession session) {
        Usuario logueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (logueado == null) {
            return sinSesion();
        }

        usuarioService.cambiarContrasena(logueado.getIdUsuario(), request.getContrasenaActual(), request.getContrasenaNueva());

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Contraseña actualizada exitosamente.");
        return ResponseEntity.ok(respuesta);
    }

    // API: modificar los datos del usuario logueado (procedure modificar_datos_usuario)
    @PutMapping("/datos")
    public ResponseEntity<Map<String, String>> modificarDatos(@RequestBody ModificarDatosRequest request, HttpSession session) {
        Usuario logueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (logueado == null) {
            return sinSesion();
        }

        usuarioService.modificarDatos(logueado.getIdUsuario(), request.getNombre(), request.getApellido(), request.getEmail());

        // mantener actualizada la copia guardada en la sesión
        logueado.setNombre(request.getNombre());
        logueado.setApellido(request.getApellido());
        logueado.setEmail(request.getEmail());

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Datos actualizados exitosamente.");
        return ResponseEntity.ok(respuesta);
    }
}
