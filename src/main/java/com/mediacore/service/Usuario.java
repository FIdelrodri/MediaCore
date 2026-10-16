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

    public void registrarUsuarioConProcedure(com.mediacore.model.Usuario usuario) {
        usuarioRepository.registrarUsuarioProcedure(
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getContrasena()
        );
    }
}