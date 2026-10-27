package com.mediacore.service;

import com.mediacore.dto.AnuncioRequest;
import com.mediacore.repository.AnuncioRepository;
import com.mediacore.util.ErrorBD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnuncioService {

    @Autowired
    private AnuncioRepository anuncioRepository;

    public boolean existePorId(Integer idAnuncio) {
        return idAnuncio != null && anuncioRepository.existsById(idAnuncio);
    }

    // Validaciones de los datos recibidos (mismos limites que las columnas de la tabla)
    public String validarDatos(AnuncioRequest anuncio) {
        if (anuncio.getNombre() == null || anuncio.getNombre().trim().isEmpty()) {
            return "El nombre del anuncio no puede estar vacío.";
        }
        if (anuncio.getNombre().length() > 100) {
            return "El nombre no puede tener más de 100 caracteres.";
        }
        if (anuncio.getDescripcion() != null && anuncio.getDescripcion().length() > 500) {
            return "La descripción no puede tener más de 500 caracteres.";
        }
        if (anuncio.getGenero() != null && anuncio.getGenero().length() > 50) {
            return "El género no puede tener más de 50 caracteres.";
        }
        if (anuncio.getEmpresa() != null && anuncio.getEmpresa().length() > 100) {
            return "La empresa no puede tener más de 100 caracteres.";
        }
        if (anuncio.getDuracion() == null || anuncio.getDuracion() <= 0) {
            return "La duración (en segundos) debe ser mayor a 0.";
        }
        return null;
    }

    public Integer crear(AnuncioRequest anuncio) {
        try {
            return anuncioRepository.crearAnuncioProcedure(anuncio.getNombre().trim(), anuncio.getDescripcion(),
                anuncio.getGenero(), anuncio.getDuracion(), anuncio.getEmpresa());
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void modificar(Integer idAnuncio, AnuncioRequest anuncio) {
        try {
            anuncioRepository.modificarAnuncioProcedure(idAnuncio, anuncio.getNombre().trim(),
                anuncio.getDescripcion(), anuncio.getGenero(), anuncio.getDuracion(), anuncio.getEmpresa());
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void eliminar(Integer idAnuncio) {
        try {
            anuncioRepository.eliminarAnuncioProcedure(idAnuncio);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void programar(Integer idAnuncio) {
        try {
            anuncioRepository.insertarAnuncioEnGrillaProcedure(idAnuncio);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public List<Map<String, Object>> listar() {
        return filasAAnuncios(anuncioRepository.listarAnunciosProcedure());
    }

    public List<Map<String, Object>> listarPorEmpresa(String empresa) {
        return filasAAnuncios(anuncioRepository.listarAnunciosPorEmpresaProcedure(empresa.trim()));
    }

    public List<Map<String, Object>> listarPorDuracion(Integer min, Integer max) {
        return filasAAnuncios(anuncioRepository.anunciosPorDuracionProcedure(min, max));
    }

    // Convierte las filas de los procedures en mapas con nombres en camelCase para el JSON
    private List<Map<String, Object>> filasAAnuncios(List<Object[]> filas) {
        List<Map<String, Object>> anuncios = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> anuncio = new LinkedHashMap<>();
            anuncio.put("idAnuncio", fila[0]);
            anuncio.put("nombre", fila[1]);
            anuncio.put("descripcion", fila[2]);
            anuncio.put("genero", fila[3]);
            anuncio.put("duracion", fila[4]);
            anuncio.put("empresa", fila[5]);
            anuncios.add(anuncio);
        }
        return anuncios;
    }
}
