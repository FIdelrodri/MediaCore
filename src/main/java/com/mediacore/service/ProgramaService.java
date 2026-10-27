package com.mediacore.service;

import com.mediacore.dto.ProgramaRequest;
import com.mediacore.repository.ProgramaRepository;
import com.mediacore.util.ErrorBD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgramaService {

    @Autowired
    private ProgramaRepository programaRepository;

    public boolean existePorId(Integer idPrograma) {
        return idPrograma != null && programaRepository.existsById(idPrograma);
    }

    // Validaciones de los datos recibidos (mismos limites que las columnas de la tabla)
    public String validarDatos(ProgramaRequest programa) {
        if (programa.getNombre() == null || programa.getNombre().trim().isEmpty()) {
            return "El nombre del programa no puede estar vacío.";
        }
        if (programa.getNombre().length() > 100) {
            return "El nombre no puede tener más de 100 caracteres.";
        }
        if (programa.getDescripcion() != null && programa.getDescripcion().length() > 500) {
            return "La descripción no puede tener más de 500 caracteres.";
        }
        if (programa.getGenero() != null && programa.getGenero().length() > 50) {
            return "El género no puede tener más de 50 caracteres.";
        }
        if (programa.getDuracion() == null || programa.getDuracion() <= 0) {
            return "La duración (en segundos) debe ser mayor a 0.";
        }
        return null;
    }

    public Integer crear(ProgramaRequest programa) {
        try {
            return programaRepository.crearProgramaProcedure(
                programa.getNombre().trim(), programa.getDescripcion(), programa.getGenero(), programa.getDuracion());
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void modificar(Integer idPrograma, ProgramaRequest programa) {
        try {
            programaRepository.modificarProgramaProcedure(
                idPrograma, programa.getNombre().trim(), programa.getDescripcion(), programa.getGenero(), programa.getDuracion());
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void eliminar(Integer idPrograma) {
        try {
            programaRepository.eliminarProgramaProcedure(idPrograma);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void programar(Integer idPrograma) {
        try {
            programaRepository.programarProgramaProcedure(idPrograma);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public List<Map<String, Object>> listar() {
        return filasAProgramas(programaRepository.listarProgramasProcedure());
    }

    public List<Map<String, Object>> buscarPorGenero(String genero) {
        return filasAProgramas(programaRepository.buscarProgramasPorGeneroProcedure(genero == null ? "" : genero.trim()));
    }

    public List<Map<String, Object>> masLargos(Integer cantidad) {
        return filasAProgramas(programaRepository.programasMasLargosProcedure(cantidad));
    }

    // Convierte las filas de los procedures en mapas con nombres en camelCase para el JSON
    private List<Map<String, Object>> filasAProgramas(List<Object[]> filas) {
        List<Map<String, Object>> programas = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> programa = new LinkedHashMap<>();
            programa.put("idPrograma", fila[0]);
            programa.put("nombre", fila[1]);
            programa.put("descripcion", fila[2]);
            programa.put("genero", fila[3]);
            programa.put("duracion", fila[4]);
            programas.add(programa);
        }
        return programas;
    }
}
