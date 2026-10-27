package com.mediacore.controller;

import com.mediacore.dto.ProgramaRequest;
import com.mediacore.model.Usuario;
import com.mediacore.service.ProgramaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/programas")
public class ProgramaRestController {

    @Autowired
    private ProgramaService programaService;

    // Roles segun la carpeta de campo (20/8):
    //  1 Administrador General, 2 Administrador, 6 Personal de postproduccion (administran el contenido)
    //  1 Administrador General, 2 Administrador, 5 Administrador de planilla (modifican la planilla; se usa desde la vista de la grilla)
    private static final int[] ROLES_CONTENIDO = {1, 2, 6};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_CONSULTA = {1, 2, 6};

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

    // API: listar todos los programas (procedure listar_programas)
    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(programaService.listar());
    }

    // API: buscar programas por genero (procedure buscar_programas_por_genero)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorGenero(@RequestParam(defaultValue = "") String genero, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(programaService.buscarPorGenero(genero));
    }

    // API: los N programas mas largos (procedure programas_mas_largos)
    @GetMapping("/mas-largos")
    public ResponseEntity<?> masLargos(@RequestParam(defaultValue = "5") Integer cantidad, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        if (cantidad == null || cantidad < 1) {
            return ResponseEntity.badRequest().body(respuesta("error", "La cantidad debe ser mayor a 0."));
        }
        return ResponseEntity.ok(programaService.masLargos(cantidad));
    }

    // API: crear programa (procedure crear_programa)
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody ProgramaRequest request, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }

        String errorValidacion = programaService.validarDatos(request);
        if (errorValidacion != null) {
            return ResponseEntity.badRequest().body(respuesta("error", errorValidacion));
        }

        Integer idPrograma = programaService.crear(request);

        Map<String, Object> respuesta = respuesta("ok", "Programa creado exitosamente.");
        respuesta.put("idPrograma", idPrograma);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // API: modificar programa (procedure modificar_programa)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modificar(@PathVariable Integer id,
                                                         @RequestBody ProgramaRequest request,
                                                         HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!programaService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El programa no existe."));
        }

        String errorValidacion = programaService.validarDatos(request);
        if (errorValidacion != null) {
            return ResponseEntity.badRequest().body(respuesta("error", errorValidacion));
        }

        programaService.modificar(id, request);
        return ResponseEntity.ok(respuesta("ok", "Programa actualizado exitosamente."));
    }

    // API: eliminar programa (procedure eliminar_programa)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!programaService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El programa no existe."));
        }

        programaService.eliminar(id);
        return ResponseEntity.ok(respuesta("ok", "Programa eliminado exitosamente."));
    }

    // API: agregar el programa al final de la grilla (procedure programar_programa)
    @PostMapping("/{id}/programar")
    public ResponseEntity<Map<String, Object>> programar(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        if (!programaService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El programa no existe."));
        }

        programaService.programar(id);
        return ResponseEntity.ok(respuesta("ok", "Programa agregado a la grilla."));
    }
}
