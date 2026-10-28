package com.mediacore.service;

import com.mediacore.dto.VivoRequest;
import com.mediacore.repository.VivoRepository;
import com.mediacore.util.ErrorBD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VivoService {

    @Autowired
    private VivoRepository vivoRepository;

    public boolean existePorId(Integer idEnvivo) {
        return idEnvivo != null && vivoRepository.existsById(idEnvivo);
    }

    // Validaciones de los datos recibidos (mismos limites que las columnas de la tabla)
    public String validarDatos(VivoRequest vivo) {
        if (vivo.getTitulo() == null || vivo.getTitulo().trim().isEmpty()) {
            return "El título del vivo no puede estar vacío.";
        }
        if (vivo.getTitulo().length() > 100) {
            return "El título no puede tener más de 100 caracteres.";
        }
        if (vivo.getDescripcion() == null || vivo.getDescripcion().trim().isEmpty()) {
            return "La descripción del vivo no puede estar vacía.";
        }
        if (vivo.getDescripcion().length() > 500) {
            return "La descripción no puede tener más de 500 caracteres.";
        }
        if (vivo.getDuracion() == null || vivo.getDuracion() <= 0) {
            return "La duración (en segundos) debe ser mayor a 0.";
        }
        return null;
    }

    public Integer crear(VivoRequest vivo) {
        try {
            return vivoRepository.crearVivoProcedure(
                vivo.getTitulo().trim(), vivo.getDescripcion().trim(), vivo.getDuracion());
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    // El trigger before_update_vivo rechaza cambiar la duracion de un vivo ya retransmitido
    public void modificar(Integer idEnvivo, VivoRequest vivo) {
        try {
            vivoRepository.modificarVivoProcedure(
                idEnvivo, vivo.getTitulo().trim(), vivo.getDescripcion().trim(), vivo.getDuracion());
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void eliminar(Integer idEnvivo) {
        try {
            vivoRepository.eliminarVivoProcedure(idEnvivo);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void marcarComoRetransmitido(Integer idEnvivo) {
        try {
            vivoRepository.marcarComoRetransmitidoProcedure(idEnvivo);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public void programar(Integer idEnvivo) {
        try {
            vivoRepository.programarVivoProcedure(idEnvivo);
        } catch (Exception e) {
            throw new IllegalArgumentException(ErrorBD.mensaje(e));
        }
    }

    public List<Map<String, Object>> listar() {
        return filasAVivos(vivoRepository.listarVivosProcedure());
    }

    public List<Map<String, Object>> listarPendientes() {
        return filasAVivos(vivoRepository.listarVivosPendientesProcedure());
    }

    // Lista publica: solo expone los datos del vivo, sin el estado
    public List<Map<String, Object>> listarRetransmitidos() {
        List<Map<String, Object>> vivos = new ArrayList<>();
        for (Object[] fila : vivoRepository.listarVivosRetransmitidosProcedure()) {
            Map<String, Object> vivo = new LinkedHashMap<>();
            vivo.put("idEnvivo", fila[0]);
            vivo.put("titulo", fila[1]);
            vivo.put("descripcion", fila[2]);
            vivo.put("duracion", fila[3]);
            vivos.add(vivo);
        }
        return vivos;
    }

    // Convierte las filas de los procedures en mapas con nombres en camelCase para el JSON
    private List<Map<String, Object>> filasAVivos(List<Object[]> filas) {
        List<Map<String, Object>> vivos = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> vivo = new LinkedHashMap<>();
            vivo.put("idEnvivo", fila[0]);
            vivo.put("titulo", fila[1]);
            vivo.put("descripcion", fila[2]);
            vivo.put("duracion", fila[3]);
            vivo.put("retransmitido", aBoolean(fila[4]));
            vivos.add(vivo);
        }
        return vivos;
    }

    // El driver puede devolver el BOOLEAN de MySQL como Boolean o como numero (0/1)
    private boolean aBoolean(Object valor) {
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return valor instanceof Number && ((Number) valor).intValue() != 0;
    }
}
