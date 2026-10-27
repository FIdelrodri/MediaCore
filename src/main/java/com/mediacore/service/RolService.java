package com.mediacore.service;

import com.mediacore.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public boolean existePorId(Integer idRol) {
        return idRol != null && rolRepository.existsById(idRol);
    }

    public List<Map<String, Object>> listar() {
        List<Map<String, Object>> roles = new ArrayList<>();
        for (Object[] fila : rolRepository.listarRolesProcedure()) {
            Map<String, Object> rol = new LinkedHashMap<>();
            rol.put("idRol", fila[0]);
            rol.put("nombreRol", fila[1]);
            roles.add(rol);
        }
        return roles;
    }

    public List<Map<String, Object>> listarUsuariosPorRol(Integer idRol) {
        List<Map<String, Object>> usuarios = new ArrayList<>();
        for (Object[] fila : rolRepository.listarUsuariosPorRolProcedure(idRol)) {
            Map<String, Object> usuario = new LinkedHashMap<>();
            usuario.put("idUsuario", fila[0]);
            usuario.put("nombreUsuario", fila[1]);
            usuario.put("nombre", fila[2]);
            usuario.put("apellido", fila[3]);
            usuario.put("email", fila[4]);
            usuario.put("fechaRegistro", fila[5] == null ? null : fila[5].toString());
            usuario.put("idRol", fila[6]);
            usuarios.add(usuario);
        }
        return usuarios;
    }

    public List<Map<String, Object>> contarUsuariosPorRol() {
        List<Map<String, Object>> conteo = new ArrayList<>();
        for (Object[] fila : rolRepository.contarUsuariosPorRolProcedure()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idRol", fila[0]);
            item.put("nombreRol", fila[1]);
            item.put("cantidadUsuarios", fila[2]);
            conteo.add(item);
        }
        return conteo;
    }
}
