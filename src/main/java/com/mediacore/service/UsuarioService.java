package com.mediacore.service;

import com.mediacore.model.Usuario;
import com.mediacore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (usuario.getContrasena() == null || usuario.getContrasena().length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }

        char[] caracteres = usuario.getContrasena().toCharArray();
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
}