package com.mediacore.util;

import com.mediacore.dto.AnuncioRequest;
import com.mediacore.dto.ProgramaRequest;
import com.mediacore.dto.RegistroRequest;
import com.mediacore.dto.VivoRequest;

/**
 * Validaciones de los datos que llegan desde el front-end (mismos limites que las columnas de las tablas).
 * Cada metodo devuelve el mensaje de error, o null si los datos son validos.
 */
public class Validaciones {

    private Validaciones() {
    }

    public static String usuario(RegistroRequest usuario) {
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
        return contrasena(usuario.getContrasena());
    }

    public static String contrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }

        boolean tieneMayuscula = false;
        boolean tieneLetra = false;
        boolean tieneNumero = false;

        for (char c : contrasena.toCharArray()) {
            if (c >= 'A' && c <= 'Z') tieneMayuscula = true;
            if (c >= 'a' && c <= 'z') tieneLetra = true;
            if (c >= '0' && c <= '9') tieneNumero = true;
        }

        if (!tieneMayuscula) return "La contraseña debe contener al menos una letra mayúscula.";
        if (!tieneLetra) return "La contraseña debe contener al menos una letra minúscula.";
        if (!tieneNumero) return "La contraseña debe contener al menos un número.";

        return null;
    }

    public static String datosPersonales(String nombre, String apellido, String email) {
        if (nombre == null || nombre.trim().isEmpty() || nombre.length() > 25) {
            return "El nombre es obligatorio y no puede tener más de 25 caracteres.";
        }
        if (apellido == null || apellido.trim().isEmpty() || apellido.length() > 25) {
            return "El apellido es obligatorio y no puede tener más de 25 caracteres.";
        }
        if (email == null || email.trim().isEmpty() || email.length() > 50) {
            return "El email es obligatorio y no puede tener más de 50 caracteres.";
        }
        return null;
    }

    public static String programa(ProgramaRequest programa) {
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

    public static String vivo(VivoRequest vivo) {
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

    public static String anuncio(AnuncioRequest anuncio) {
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
}
