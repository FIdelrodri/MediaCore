package com.mediacore.controller;

import com.mediacore.dto.AsignarRolRequest;
import com.mediacore.dto.EliminarUsuarioRequest;
import com.mediacore.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private com.mediacore.service.Usuario usuarioService;

    // metodo auxiliar para validar si quien realiza la peticion es Administrador (Rol 1)
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
}