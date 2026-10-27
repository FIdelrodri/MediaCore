package com.mediacore.controller;

import com.mediacore.model.Usuario;
import com.mediacore.service.GrillaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/grilla")
public class GrillaRestController {

    @Autowired
    private GrillaService grillaService;

    // Roles de la API:
    //  1 Administrador General, 2 Administrador (vaciar la grilla)
    //  1 Administrador General, 2 Administrador, 5 Administrador de planilla (modifican la planilla)
    private static final int[] ROLES_ADMIN = {1, 2};
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

    // API: grilla completa con el inicio y fin proyectados de cada posicion
    // (procedure ver_grilla_con_horarios_proyectados)
    @GetMapping
    public ResponseEntity<?> ver(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(grillaService.verConHorariosProyectados());
    }

    // API: cantidad de bloques y segundos totales y restantes (procedure duracion_total_grilla)
    @GetMapping("/duracion-total")
    public ResponseEntity<?> duracionTotal(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        return ResponseEntity.ok(grillaService.duracionTotal());
    }

    // API: bloque que esta al aire (procedure bloque_en_aire)
    @GetMapping("/en-aire")
    public ResponseEntity<?> enAire(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        Map<String, Object> bloque = grillaService.bloqueEnAire();
        if (bloque == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(respuesta("error", "No hay ningún bloque al aire."));
        }
        return ResponseEntity.ok(bloque);
    }

    // API: historial de lo que ya salio al aire (procedure ver_historial_emision)
    @GetMapping("/historial")
    public ResponseEntity<?> historial(@RequestParam(defaultValue = "20") Integer limite, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_CONSULTA);
        if (error != null) {
            return error;
        }
        if (limite == null || limite < 1 || limite > 200) {
            return ResponseEntity.badRequest()
                    .body(respuesta("error", "El límite debe estar entre 1 y 200."));
        }
        return ResponseEntity.ok(grillaService.verHistorial(limite));
    }

    // API: agregar un bloque al final de la cola (procedure agregar_bloque_a_grilla)
    @PostMapping("/bloques/{idBloque}")
    public ResponseEntity<Map<String, Object>> agregarBloque(@PathVariable Integer idBloque, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        grillaService.agregarBloque(idBloque);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta("ok", "Bloque agregado a la grilla."));
    }

    // API: terminar el bloque al aire y subir el siguiente (procedure avanzar_grilla)
    @PostMapping("/avanzar")
    public ResponseEntity<Map<String, Object>> avanzar(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        grillaService.avanzar();
        return ResponseEntity.ok(respuesta("ok", "La grilla avanzó al siguiente bloque."));
    }

    // API: saltar el bloque que esta al aire (procedure saltar_bloque_actual)
    @PostMapping("/saltar")
    public ResponseEntity<Map<String, Object>> saltar(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        grillaService.saltarBloqueActual();
        return ResponseEntity.ok(respuesta("ok", "Bloque actual saltado."));
    }

    // API: quitar una posicion de la cola (procedure quitar_de_grilla)
    @DeleteMapping("/posiciones/{posicion}")
    public ResponseEntity<Map<String, Object>> quitar(@PathVariable Integer posicion, HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_PLANILLA);
        if (error != null) {
            return error;
        }
        grillaService.quitarPosicion(posicion);
        return ResponseEntity.ok(respuesta("ok", "Posición quitada de la grilla."));
    }

    // API: vaciar toda la grilla (procedure vaciar_grilla); solo administradores
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> vaciar(HttpSession session) {
        ResponseEntity<Map<String, Object>> error = verificarAcceso(session, ROLES_ADMIN);
        if (error != null) {
            return error;
        }
        grillaService.vaciar();
        return ResponseEntity.ok(respuesta("ok", "Grilla vaciada."));
    }
}
