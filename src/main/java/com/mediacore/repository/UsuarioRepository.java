package com.mediacore.repository;

import com.mediacore.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmail(String email);
    boolean existsByNombreUsuario(String nombreUsuario);

    // Métodos para buscar el usuario al iniciar sesión
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByEmail(String email);

    @Transactional
    @Modifying
    @Procedure(procedureName = "registrar_usuario")
    void registrarUsuarioProcedure(
        @Param("p_nombre_usuario") String nombreUsuario,
        @Param("p_nombre") String nombre,
        @Param("p_apellido") String apellido,
        @Param("p_email") String email,
        @Param("p_contrasena") String contrasena
    );
}