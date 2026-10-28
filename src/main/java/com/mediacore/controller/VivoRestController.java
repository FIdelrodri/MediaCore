package com.mediacore.controller;

import com.mediacore.dto.VivoRequest;
import com.mediacore.model.Usuario;
import com.mediacore.service.VivoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vivos")
public class VivoRestController {

    @Autowired
    private VivoService vivoService;

    // Roles:
    //  1 Administrador General, 2 Administrador, 3 Escenografo (administran los vivos)
    //  1 Administrador General, 2 Administrador, 5 Administrador de planilla (programan desde la vista de la grilla)
    private static final int[] ROLES_CONTENIDO = {1, 2, 3};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_CONSULTA = {1, 2, 3};

    private Map<String, Object> respuesta(String status, String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", status);
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }

    // Devuelve null si el usuario tiene sesion y uno de los roles; si no, la respuesta de error (401 o 403)
    private ResponseEntity<Map<String, Object>> verificarAcceso(HttpSession session, int[] rolesPermitidos) {
        Usuario logueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (logueado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(respuesta("error", "Debes iniciar sesión para realizar esta acción."));
        }
        for (int rol : rolesPermitidos) {
            if (logueado.getIdRol() != null && logueado.getIdRol() == rol) {
                return null;
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(respuesta("error", "Acceso denegado: tu rol no tiene permisos para esta acción."));
    }

    // Errores lanzados por los procedures (SIGNAL) y por las validaciones del service
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(respuesta("error", e.getMessage()));
    }

    // API: listar todos los vivos (procedure listar_vivos)
    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(vivoService.listar());
    }

    // API: listar los vivos que todavia no fueron retransmitidos (procedure listar_vivos_pendientes)
    @GetMapping("/pendientes")
    public ResponseEntity<?> listarPendientes(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(vivoService.listarPendientes());
    }

    // API: crear vivo (procedure crear_vivo)
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody VivoRequest request, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }

        String errorValidacion = vivoService.validarDatos(request);
        if (errorValidacion != null) {
            return ResponseEntity.badRequest().body(respuesta("error", errorValidacion));
        }

        Integer idEnvivo = vivoService.crear(request);

        Map<String, Object> respuesta = respuesta("ok", "Vivo creado exitosamente.");
        respuesta.put("idEnvivo", idEnvivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // API: modificar vivo (procedure modificar_vivo)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modificar(@PathVariable Integer id,
                                                         @RequestBody VivoRequest request,
                                                         HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!vivoService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El vivo no existe."));
        }

        String errorValidacion = vivoService.validarDatos(request);
        if (errorValidacion != null) {
            return ResponseEntity.badRequest().body(respuesta("error", errorValidacion));
        }

        vivoService.modificar(id, request);
        return ResponseEntity.ok(respuesta("ok", "Vivo actualizado exitosamente."));
    }

    // API: eliminar vivo (procedure eliminar_vivo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!vivoService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El vivo no existe."));
        }

        vivoService.eliminar(id);
        return ResponseEntity.ok(respuesta("ok", "Vivo eliminado exitosamente."));
    }

    // API: marcar el vivo como retransmitido (procedure marcar_como_retransmitido)
    @PutMapping("/{id}/retransmitido")
    public ResponseEntity<Map<String, Object>> marcarComoRetransmitido(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!vivoService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El vivo no existe."));
        }

        vivoService.marcarComoRetransmitido(id);
        return ResponseEntity.ok(respuesta("ok", "Vivo marcado como retransmitido."));
    }

    // API: agregar el vivo al final de la grilla (procedure programar_vivo)
    @PostMapping("/{id}/programar")
    public ResponseEntity<Map<String, Object>> programar(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        if (!vivoService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El vivo no existe."));
        }

        vivoService.programar(id);
        return ResponseEntity.ok(respuesta("ok", "Vivo agregado a la grilla."));
    }
}
