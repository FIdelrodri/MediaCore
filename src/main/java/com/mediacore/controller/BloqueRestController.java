package com.mediacore.controller;

import com.mediacore.model.Usuario;
import com.mediacore.service.BloqueService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bloques")
public class BloqueRestController {

    @Autowired
    private BloqueService bloqueService;

    // Los bloques se crean solos con los triggers de programa, vivo y anuncio,
    // por eso la API solo consulta y mantiene los bloques existentes.
    //  1 Administrador General, 2 Administrador, 5 Administrador de planilla, 6 Personal de postproduccion
    private static final int[] ROLES_CONSULTA = {1, 2, 5, 6};
    private static final int[] ROLES_MANTENIMIENTO = {1, 6};

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarError(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(respuesta("error", e.getMessage()));
    }

    // API: listar bloques; con ?tipo= filtra (procedures listar_todos_los_bloques y listar_bloques_por_tipo)
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String tipo, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            return ResponseEntity.ok(bloqueService.listar());
        }
        if (!bloqueService.tipoValido(tipo.trim())) {
            return ResponseEntity.badRequest()
                    .body(respuesta("error", "El tipo debe ser programa, vivo o anuncio."));
        }
        return ResponseEntity.ok(bloqueService.listarPorTipo(tipo.trim()));
    }

    // API: bloques que no estan en la grilla (procedure listar_bloques_no_usados)
    @GetMapping("/no-usados")
    public ResponseEntity<?> listarNoUsados(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(bloqueService.listarNoUsados());
    }

    // API: cantidad y duracion total por tipo (procedure duracion_total_por_tipo)
    @GetMapping("/duracion-por-tipo")
    public ResponseEntity<?> duracionPorTipo(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(bloqueService.duracionTotalPorTipo());
    }

    // API: id del bloque de un contenido (procedure obtener_bloque)
    @GetMapping("/buscar")
    public ResponseEntity<?> obtenerBloque(@RequestParam String tipo, @RequestParam Integer idReferencia,
                                           HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        if (!bloqueService.tipoValido(tipo)) {
            return ResponseEntity.badRequest()
                    .body(respuesta("error", "El tipo debe ser programa, vivo o anuncio."));
        }

        Integer idBloque = bloqueService.obtenerId(tipo, idReferencia);
        if (idBloque == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(respuesta("error", "No existe un bloque para ese contenido."));
        }

        Map<String, Object> respuesta = respuesta("ok", "Bloque encontrado.");
        respuesta.put("idBloque", idBloque);
        return ResponseEntity.ok(respuesta);
    }

    // API: recalcular la duracion desde el contenido original (procedure recalcular_duracion_bloque)
    @PutMapping("/{id}/recalcular-duracion")
    public ResponseEntity<Map<String, Object>> recalcularDuracion(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_MANTENIMIENTO);
        if (error != null) {
            return error;
        }
        if (!bloqueService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El bloque no existe."));
        }

        bloqueService.recalcularDuracion(id);
        return ResponseEntity.ok(respuesta("ok", "Duración del bloque recalculada exitosamente."));
    }

    // API: borrar bloques cuyo contenido ya no existe (procedure limpiar_bloques_huerfanos)
    @DeleteMapping("/huerfanos")
    public ResponseEntity<Map<String, Object>> limpiarHuerfanos(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_MANTENIMIENTO);
        if (error != null) {
            return error;
        }

        int eliminados = bloqueService.limpiarHuerfanos();
        Map<String, Object> respuesta = respuesta("ok", "Limpieza de bloques huérfanos finalizada.");
        respuesta.put("bloquesEliminados", eliminados);
        return ResponseEntity.ok(respuesta);
    }
}
