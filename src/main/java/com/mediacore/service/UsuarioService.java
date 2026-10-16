package com.mediacore.service;

import com.mediacore.model.Usuario;
import com.mediacore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Guarda directamente la cuenta en la tabla usuario
        return usuarioRepository.save(usuario);
    }
}