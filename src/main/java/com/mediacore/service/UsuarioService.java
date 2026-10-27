package com.mediacore.service;

import com.mediacore.model.Usuario;
import com.mediacore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    public boolean existePorId(Integer idUsuario) {
        return usuarioRepository.existsById(idUsuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            return null;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getContrasena().equals(contrasena)) {
                return usuario;
            }
        }

        return null;
    }

    public String validarDatosUsuario(Usuario usuario) {
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            return "El nombre de usuario no puede estar vacío.";
        }
        if (usuario.getNombreUsuario().length() > 25) {
            return "El nombre de usuario no puede tener más de 25 caracteres.";
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return "El nombre no puede estar vacío.";
        }
        if (usuario.getNombre().length() > 25) {
            return "El nombre no puede tener más de 25 caracteres.";
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return "El apellido no puede estar vacío.";
        }
        if (usuario.getApellido().length() > 25) {
            return "El apellido no puede tener más de 25 caracteres.";
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return "El email no puede estar vacío.";
        }
        if (usuario.getEmail().length() > 50) {
            return "El email no puede tener más de 50 caracteres.";
        }
        return validarContrasena(usuario.getContrasena());
    }

    public String validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }

        char[] caracteres = contrasena.toCharArray();
        boolean tieneMayuscula = false;
        boolean tieneLetra = false;
        boolean tieneNumero = false;

        for (char c : caracteres) {
            if (c >= 'A' && c <= 'Z') tieneMayuscula = true;
            if (c >= 'a' && c <= 'z') tieneLetra = true;
            if (c >= '0' && c <= '9') tieneNumero = true;
        }

        if (!tieneMayuscula) return "La contraseña debe contener al menos una letra mayúscula.";
        if (!tieneLetra) return "La contraseña debe contener al menos una letra minúscula.";
        if (!tieneNumero) return "La contraseña debe contener al menos un número.";

        return null;
    }

    public void registrarUsuarioConProcedure(Usuario usuario) {
        usuarioRepository.registrarUsuarioProcedure(
            usuario.getNombreUsuario(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getContrasena()
        );
    }

    // Método de servicio para asignar rol
    public void asignarRolConProcedure(Integer idUsuario, Integer idRol) {
        usuarioRepository.asignarRolUsuarioProcedure(idUsuario, idRol);
    }

    // Método de servicio para eliminar usuario
    public void eliminarUsuarioConProcedure(Integer idUsuario) {
        usuarioRepository.eliminarUsuarioProcedure(idUsuario);
    }

    // Cambia la contraseña del usuario (la contraseña actual se compara con equals porque
    // la comparacion de MySQL no distingue mayusculas de minusculas)
    public void cambiarContrasena(Integer idUsuario, String contrasenaActual, String contrasenaNueva) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        if (contrasenaActual == null || !usuario.getContrasena().equals(contrasenaActual)) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        String error = validarContrasena(contrasenaNueva);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        try {
            usuarioRepository.cambiarContrasenaProcedure(idUsuario, contrasenaActual, contrasenaNueva);
        } catch (Exception e) {
            throw new IllegalArgumentException(mensajeDeError(e));
        }
    }

    public void modificarDatos(Integer idUsuario, String nombre, String apellido, String email) {
        if (nombre == null || nombre.trim().isEmpty() || nombre.length() > 25) {
            throw new IllegalArgumentException("El nombre es obligatorio y no puede tener más de 25 caracteres.");
        }
        if (apellido == null || apellido.trim().isEmpty() || apellido.length() > 25) {
            throw new IllegalArgumentException("El apellido es obligatorio y no puede tener más de 25 caracteres.");
        }
        if (email == null || email.trim().isEmpty() || email.length() > 50) {
            throw new IllegalArgumentException("El email es obligatorio y no puede tener más de 50 caracteres.");
        }

        Optional<Usuario> otro = usuarioRepository.findByEmail(email);
        if (otro.isPresent() && !otro.get().getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

        try {
            usuarioRepository.modificarDatosUsuarioProcedure(idUsuario, nombre, apellido, email);
        } catch (Exception e) {
            throw new IllegalArgumentException(mensajeDeError(e));
        }
    }

    public void degradarAUsuario(Integer idUsuario) {
        try {
            usuarioRepository.degradarAUsuarioProcedure(idUsuario);
        } catch (Exception e) {
            throw new IllegalArgumentException(mensajeDeError(e));
        }
    }

    public List<Map<String, Object>> listarUsuarios() {
        return filasAUsuarios(usuarioRepository.listarUsuariosProcedure());
    }

    public List<Map<String, Object>> buscarUsuarios(String texto) {
        return filasAUsuarios(usuarioRepository.buscarUsuarioProcedure(texto == null ? "" : texto.trim()));
    }

    public List<Map<String, Object>> listarUsuariosRecientes(Integer dias) {
        return filasAUsuarios(usuarioRepository.listarUsuariosRecientesProcedure(dias));
    }

    public List<Map<String, Object>> verLogCambiosRol(Integer limite) {
        List<Map<String, Object>> log = new ArrayList<>();
        for (Object[] fila : usuarioRepository.verLogCambiosRolProcedure(limite)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idLog", fila[0]);
            item.put("idUsuario", fila[1]);
            item.put("nombreUsuario", fila[2]);
            item.put("rolAnterior", fila[3]);
            item.put("rolNuevo", fila[4]);
            item.put("fecha", fila[5] == null ? null : fila[5].toString());
            log.add(item);
        }
        return log;
    }

    // Convierte las filas de los procedures de usuarios (sin la contraseña) en mapas para el JSON
    private List<Map<String, Object>> filasAUsuarios(List<Object[]> filas) {
        List<Map<String, Object>> usuarios = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> usuario = new LinkedHashMap<>();
            usuario.put("idUsuario", fila[0]);
            usuario.put("nombreUsuario", fila[1]);
            usuario.put("nombre", fila[2]);
            usuario.put("apellido", fila[3]);
            usuario.put("email", fila[4]);
            usuario.put("fechaRegistro", fila[5] == null ? null : fila[5].toString());
            usuario.put("idRol", fila[6]);
            if (fila.length > 7) {
                usuario.put("nombreRol", fila[7]);
            }
            usuarios.add(usuario);
        }
        return usuarios;
    }

    // Obtiene el MESSAGE_TEXT del SIGNAL de MySQL (la causa mas profunda de la excepcion)
    private String mensajeDeError(Exception e) {
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }
}
