package com.mediacore.service;

import com.mediacore.repository.BloqueRepository;
import com.mediacore.util.ErrorBD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BloqueService {

    @Autowired
    private BloqueRepository bloqueRepository;

    public boolean existePorId(Integer idBloque) {
        return idBloque != null && bloqueRepository.existsById(idBloque);
    }

    // Los tipos validos son los del enum bloque.tipo_contenido
    public boolean tipoValido(String tipo) {
        return "programa".equals(tipo) || "vivo".equals(tipo) || "anuncio".equals(tipo);
    }

    public Integer obtenerId(String tipo, Integer idReferencia) {
        try {
            return bloqueRepository.obtenerBloqueProcedure(tipo, idReferencia);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public List<Map<String, Object>> listar() {
        return filasABloques(bloqueRepository.listarTodosLosBloquesProcedure());
    }

    public List<Map<String, Object>> listarPorTipo(String tipo) {
        return filasABloques(bloqueRepository.listarBloquesPorTipoProcedure(tipo));
    }

    public List<Map<String, Object>> listarNoUsados() {
        return filasABloques(bloqueRepository.listarBloquesNoUsadosProcedure());
    }

    public List<Map<String, Object>> duracionTotalPorTipo() {
        List<Map<String, Object>> totales = new ArrayList<>();
        for (Object[] fila : bloqueRepository.duracionTotalPorTipoProcedure()) {
            Map<String, Object> total = new LinkedHashMap<>();
            total.put("tipoContenido", fila[0]);
            total.put("cantidadBloques", fila[1]);
            total.put("segundosTotales", fila[2]);
            totales.add(total);
        }
        return totales;
    }

    public void recalcularDuracion(Integer idBloque) {
        try {
            bloqueRepository.recalcularDuracionBloqueProcedure(idBloque);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public int limpiarHuerfanos() {
        try {
            List<Object> resultado = bloqueRepository.limpiarBloquesHuerfanosProcedure();
            if (resultado.isEmpty() || !(resultado.get(0) instanceof Number)) {
                return 0;
            }
            return ((Number) resultado.get(0)).intValue();
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    // Convierte las filas de vista_bloques en mapas con nombres en camelCase para el JSON
    private List<Map<String, Object>> filasABloques(List<Object[]> filas) {
        List<Map<String, Object>> bloques = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> bloque = new LinkedHashMap<>();
            bloque.put("idBloque", fila[0]);
            bloque.put("tipoContenido", fila[1]);
            bloque.put("idReferencia", fila[2]);
            bloque.put("titulo", fila[3]);
            bloque.put("duracion", fila[4]);
            bloques.add(bloque);
        }
        return bloques;
    }
}
