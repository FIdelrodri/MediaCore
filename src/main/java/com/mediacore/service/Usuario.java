package com.mediacore.service;

import com.mediacore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Usuario {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    // Validación pura en Java imitando la lógica del frontend JS
    public String validarDatosUsuario(com.mediacore.model.Usuario usuario) {
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

        // Conversión a array de caracteres equivalente a contrasena.split('') en JS
        char[] caracteres = usuario.getContrasena().toCharArray();
        boolean tieneMayuscula = false;
        boolean tieneLetra = false;
        boolean tieneNumero = false;

        for (char c : caracteres) {
            if (c >= 'A' && c <= 'Z') {
                tieneMayuscula = true;
            }
            if (c >= 'a' && c <= 'z') {
                tieneLetra = true;
            }
            if (c >= '0' && c <= '9') {
                tieneNumero = true;
            }
        }

        if (!tieneMayuscula) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        if (!tieneLetra) {
            return "La contraseña debe contener al menos una letra minúscula.";
        }
        if (!tieneNumero) {
            return "La contraseña debe contener al menos un número.";
        }

        return null; // Retorna null si pasó todas las validaciones correctamente
    }

    public void registrarUsuarioConProcedure(com.mediacore.model.Usuario usuario) {
        usuarioRepository.registrarUsuarioProcedure(
            usuario.getNombreUsuario(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getContrasena()
        );
    }
}