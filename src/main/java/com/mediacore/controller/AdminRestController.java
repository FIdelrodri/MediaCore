package com.mediacore.controller;

import com.mediacore.dto.AsignarRolRequest;
import com.mediacore.dto.DegradarUsuarioRequest;
import com.mediacore.dto.EliminarUsuarioRequest;
import com.mediacore.model.Usuario;
import com.mediacore.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private UsuarioService usuarioService;

    // método auxiliar para validar si quien realiza la petición es Administrador (Rol 1)
    private boolean esAdministrador(HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        return usuarioLogueado != null && Integer.valueOf(1).equals(usuarioLogueado.getIdRol());
    }

    // API 1: Cambiar/Asignar Rol
    @PutMapping("/asignar-rol")
    public ResponseEntity<Map<String, String>> asignarRol(@RequestBody AsignarRolRequest request, HttpSession session) {
        Map<String, String> respuesta = new HashMap<>();

        // verificar si tiene sesión activa de Administrador
        if (!esAdministrador(session)) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "Acceso denegado: Se requieren permisos de Administrador.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
        }

        // validar datos recibidos
        if (request.getIdUsuario() == null || request.getIdRol() == null) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El ID de usuario y el ID de rol son obligatorios.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        // validar si el usuario objetivo existe
        if (!usuarioService.existePorId(request.getIdUsuario())) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El usuario especificado no existe.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        // ejecutar el Stored Procedure
        usuarioService.asignarRolConProcedure(request.getIdUsuario(), request.getIdRol());

        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Rol actualizado exitosamente.");
        return ResponseEntity.ok(respuesta);
    }

    // API 2: eliminar Usuario
    @DeleteMapping("/eliminar-usuario")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@RequestBody EliminarUsuarioRequest request, HttpSession session) {
        Map<String, String> respuesta = new HashMap<>();

        // verificar si tiene sesión activa de Administrador
        if (!esAdministrador(session)) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "Acceso denegado: Se requieren permisos de Administrador.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
        }

        // validar datos recibidos
        if (request.getIdUsuario() == null) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El ID de usuario a eliminar es obligatorio.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        // validar si el usuario objetivo existe
        if (!usuarioService.existePorId(request.getIdUsuario())) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El usuario especificado no existe.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        // ejecutar el Stored Procedure
        usuarioService.eliminarUsuarioConProcedure(request.getIdUsuario());

        respuesta.put("status", "ok");
        respuesta.put("mensaje", "Usuario eliminado exitosamente.");
        return ResponseEntity.ok(respuesta);
    }

    // API 3: Listar Usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(HttpSession session) {
        if (!esAdministrador(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("mensaje", "Acceso denegado."));
        }

        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // API 4: Buscar usuarios por texto (procedure buscar_usuario)
    @GetMapping("/usuarios/buscar")
    public ResponseEntity<?> buscarUsuarios(@RequestParam(defaultValue = "") String texto, HttpSession session) {
        if (!esAdministrador(session)) {
            return accesoDenegado();
        }
        return ResponseEntity.ok(usuarioService.buscarUsuarios(texto));
    }

    // API 5: Usuarios registrados en los ultimos N dias (procedure listar_usuarios_recientes)
    @GetMapping("/usuarios/recientes")
    public ResponseEntity<?> usuariosRecientes(@RequestParam(defaultValue = "7") Integer dias, HttpSession session) {
        if (!esAdministrador(session)) {
            return accesoDenegado();
        }
        if (dias == null || dias < 1) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", "La cantidad de días debe ser mayor a 0."));
        }
        return ResponseEntity.ok(usuarioService.listarUsuariosRecientes(dias));
    }

    // API 6: Devolver un usuario al rol "usuario" (procedure degradar_a_usuario)
    @PutMapping("/degradar-usuario")
    public ResponseEntity<Map<String, String>> degradarUsuario(@RequestBody DegradarUsuarioRequest request, HttpSession session) {
        Map<String, String> respuesta = new HashMap<>();

        if (!esAdministrador(session)) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "Acceso denegado: Se requieren permisos de Administrador.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
        }

        if (request.getIdUsuario() == null) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El ID de usuario es obligatorio.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        if (!usuarioService.existePorId(request.getIdUsuario())) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El usuario especificado no existe.");
            return ResponseEntity.badRequest().body(respuesta);
        }

        usuarioService.degradarAUsuario(request.getIdUsuario());

        respuesta.put("status", "ok");
        respuesta.put("mensaje", "El usuario volvió al rol de Usuario.");
        return ResponseEntity.ok(respuesta);
    }

    // API 7: Historial de cambios de rol (procedure ver_log_cambios_rol)
    @GetMapping("/log-cambios-rol")
    public ResponseEntity<?> logCambiosRol(@RequestParam(defaultValue = "50") Integer limite, HttpSession session) {
        if (!esAdministrador(session)) {
            return accesoDenegado();
        }
        if (limite == null || limite < 1) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", "El límite debe ser mayor a 0."));
        }
        return ResponseEntity.ok(usuarioService.verLogCambiosRol(limite));
    }

    private ResponseEntity<Map<String, String>> accesoDenegado() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "error");
        respuesta.put("mensaje", "Acceso denegado: Se requieren permisos de Administrador.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
    }

    // Errores lanzados por los procedures (SIGNAL), por ejemplo al quitar el rol al ultimo Administrador General
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(IllegalArgumentException e) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("status", "error");
        respuesta.put("mensaje", e.getMessage());
        return ResponseEntity.badRequest().body(respuesta);
    }
}
