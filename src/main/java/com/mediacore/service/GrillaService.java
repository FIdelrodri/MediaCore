package com.mediacore.service;

import com.mediacore.repository.GrillaRepository;
import com.mediacore.util.ErrorBD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GrillaService {

    @Autowired
    private GrillaRepository grillaRepository;

    // ---- Consultas ----

    public List<Map<String, Object>> verConHorariosProyectados() {
        List<Map<String, Object>> posiciones = new ArrayList<>();
        for (Object[] fila : grillaRepository.verGrillaConHorariosProyectadosProcedure()) {
            Map<String, Object> posicion = new LinkedHashMap<>();
            posicion.put("orden", fila[0]);
            posicion.put("idPosicion", fila[1]);
            posicion.put("idBloque", fila[2]);
            posicion.put("tipoContenido", fila[3]);
            posicion.put("titulo", fila[4]);
            posicion.put("duracionSegundos", fila[5]);
            posicion.put("inicioProyectado", fecha(fila[6]));
            posicion.put("finProyectado", fecha(fila[7]));
            posiciones.add(posicion);
        }
        return posiciones;
    }

    public Map<String, Object> duracionTotal() {
        Map<String, Object> total = new LinkedHashMap<>();
        List<Object[]> filas = grillaRepository.duracionTotalGrillaProcedure();
        Object[] fila = filas.isEmpty() ? new Object[] {0, 0, 0} : filas.get(0);
        total.put("cantidadBloques", fila[0]);
        total.put("segundosTotales", fila[1]);
        total.put("segundosRestantes", fila[2]);
        return total;
    }

    // Devuelve null cuando la grilla esta vacia (no hay nada al aire)
    public Map<String, Object> bloqueEnAire() {
        List<Object[]> filas = grillaRepository.bloqueEnAireProcedure();
        if (filas.isEmpty()) {
            return null;
        }
        Object[] fila = filas.get(0);
        Map<String, Object> bloque = new LinkedHashMap<>();
        bloque.put("orden", fila[0]);
        bloque.put("idPosicion", fila[1]);
        bloque.put("idBloque", fila[2]);
        bloque.put("tipoContenido", fila[3]);
        bloque.put("titulo", fila[4]);
        bloque.put("duracionSegundos", fila[5]);
        bloque.put("horaInicio", fecha(fila[6]));
        bloque.put("horaFin", fecha(fila[7]));
        bloque.put("segundosRestantes", fila[8]);
        return bloque;
    }

    public List<Map<String, Object>> verHistorial(Integer limite) {
        List<Map<String, Object>> historial = new ArrayList<>();
        for (Object[] fila : grillaRepository.verHistorialEmisionProcedure(limite)) {
            Map<String, Object> emision = new LinkedHashMap<>();
            emision.put("idHistorial", fila[0]);
            emision.put("idBloque", fila[1]);
            emision.put("tipoContenido", fila[2]);
            emision.put("titulo", fila[3]);
            emision.put("horaInicio", fecha(fila[4]));
            emision.put("horaFinReal", fecha(fila[5]));
            historial.add(emision);
        }
        return historial;
    }

    // ---- Operaciones sobre la cola ----

    public void agregarBloque(Integer idBloque) {
        try {
            grillaRepository.agregarBloqueAGrillaProcedure(idBloque);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void avanzar() {
        try {
            grillaRepository.avanzarGrillaProcedure();
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void saltarBloqueActual() {
        try {
            grillaRepository.saltarBloqueActualProcedure();
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void quitarPosicion(Integer posicion) {
        try {
            grillaRepository.quitarDeGrillaProcedure(posicion);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void vaciar() {
        try {
            grillaRepository.vaciarGrillaProcedure();
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    // Las fechas de los procedures llegan como Timestamp; se devuelven en formato ISO sin zona
    private Object fecha(Object valor) {
        if (valor instanceof Timestamp) {
            return ((Timestamp) valor).toLocalDateTime().toString();
        }
        return valor;
    }

    // ---- Reordenamiento de la cola ----

    public Integer insertarEnPosicion(Integer idBloque, Integer posicion) {
        try {
            return grillaRepository.insertarBloqueEnPosicionProcedure(idBloque, posicion);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void intercambiar(Integer posicionA, Integer posicionB) {
        try {
            grillaRepository.intercambiarPosicionesProcedure(posicionA, posicionB);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void mover(Integer origen, Integer destino) {
        try {
            grillaRepository.moverBloqueProcedure(origen, destino);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void reemplazar(Integer posicion, Integer idBloqueNuevo) {
        try {
            grillaRepository.reemplazarBloqueProcedure(posicion, idBloqueNuevo);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void duplicar(Integer posicion) {
        try {
            grillaRepository.duplicarPosicionProcedure(posicion);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }
}
