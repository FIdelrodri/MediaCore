package com.mediacore.controller;

import com.mediacore.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RolRestController {

    @Autowired
    private RolService rolService;

    // API: listar todos los roles (procedure listar_roles)
    @GetMapping
    public List<Map<String, Object>> listar() {
        return rolService.listar();
    }

    // API: cantidad de usuarios por rol (procedure contar_usuarios_por_rol)
    @GetMapping("/conteo-usuarios")
    public List<Map<String, Object>> contarUsuarios() {
        return rolService.contarUsuariosPorRol();
    }

    // API: usuarios de un rol (procedure listar_usuarios_por_rol)
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<?> usuariosPorRol(@PathVariable Integer id) {
        if (!rolService.existePorId(id)) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("status", "error");
            respuesta.put("mensaje", "El rol no existe.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }
        return ResponseEntity.ok(rolService.listarUsuariosPorRol(id));
    }
}
