package com.mediacore.controller;

import com.mediacore.dto.AnuncioRequest;
import com.mediacore.model.Usuario;
import com.mediacore.service.AnuncioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioRestController {

    @Autowired
    private AnuncioService anuncioService;

    // Mismos roles que Programa:
    //  1 Administrador General, 6 Personal de postproduccion (administran el contenido)
    //  1 Administrador General, 2 Administrador, 5 Administrador de planilla (modifican la planilla)
    private static final int[] ROLES_CONTENIDO = {1, 6};
    private static final int[] ROLES_PLANILLA = {1, 2, 5};
    private static final int[] ROLES_CONSULTA = {1, 2, 5, 6};

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

    // API: listar anuncios; con ?empresa= filtra (procedure listar_anuncios_por_empresa)
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "") String empresa, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(anuncioService.listarPorEmpresa(empresa));
    }

    // API: anuncios con duracion entre un minimo y un maximo (procedure anuncios_por_duracion)
    @GetMapping("/por-duracion")
    public ResponseEntity<?> porDuracion(@RequestParam Integer min, @RequestParam Integer max, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        if (min < 0 || max < min) {
            return ResponseEntity.badRequest()
                    .body(respuesta("error", "El rango de duración no es válido."));
        }
        return ResponseEntity.ok(anuncioService.listarPorDuracion(min, max));
    }

    // API: crear anuncio (procedure crear_anuncio)
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody AnuncioRequest request, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }

        String errorValidacion = anuncioService.validarDatos(request);
        if (errorValidacion != null) {
            return ResponseEntity.badRequest().body(respuesta("error", errorValidacion));
        }

        Integer idAnuncio = anuncioService.crear(request);

        Map<String, Object> respuesta = respuesta("ok", "Anuncio creado exitosamente.");
        respuesta.put("idAnuncio", idAnuncio);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // API: modificar anuncio (procedure modificar_anuncio)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modificar(@PathVariable Integer id,
                                                         @RequestBody AnuncioRequest request,
                                                         HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!anuncioService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El anuncio no existe."));
        }

        String errorValidacion = anuncioService.validarDatos(request);
        if (errorValidacion != null) {
            return ResponseEntity.badRequest().body(respuesta("error", errorValidacion));
        }

        anuncioService.modificar(id, request);
        return ResponseEntity.ok(respuesta("ok", "Anuncio actualizado exitosamente."));
    }

    // API: eliminar anuncio (procedure eliminar_anuncio)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONTENIDO);
        if (error != null) {
            return error;
        }
        if (!anuncioService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El anuncio no existe."));
        }

        anuncioService.eliminar(id);
        return ResponseEntity.ok(respuesta("ok", "Anuncio eliminado exitosamente."));
    }

    // API: agregar el anuncio al final de la grilla (procedure insertar_anuncio_en_grilla)
    @PostMapping("/{id}/programar")
    public ResponseEntity<Map<String, Object>> programar(@PathVariable Integer id, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        if (!anuncioService.existePorId(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta("error", "El anuncio no existe."));
        }

        anuncioService.programar(id);
        return ResponseEntity.ok(respuesta("ok", "Anuncio agregado a la grilla."));
    }
}
