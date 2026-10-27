package com.mediacore.repository;

import com.mediacore.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmail(String email);
    boolean existsByNombreUsuario(String nombreUsuario);

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

    // Stored Procedure para modificar el rol de un usuario
    @Transactional
    @Modifying
    @Procedure(procedureName = "asignar_rol_usuario")
    void asignarRolUsuarioProcedure(
        @Param("p_id_usuario") Integer idUsuario,
        @Param("p_id_rol") Integer idRol
    );

    // Stored Procedure para eliminar un usuario
    @Transactional
    @Modifying
    @Procedure(procedureName = "eliminar_usuario")
    void eliminarUsuarioProcedure(
        @Param("p_id_usuario") Integer idUsuario
    );

    // Stored Procedure para cambiar la contraseña de un usuario
    @Transactional
    @Modifying
    @Procedure(procedureName = "cambiar_contrasena")
    void cambiarContrasenaProcedure(
        @Param("p_id_usuario") Integer idUsuario,
        @Param("p_vieja") String contrasenaActual,
        @Param("p_nueva") String contrasenaNueva
    );

    // Stored Procedure para modificar nombre, apellido y email
    @Transactional
    @Modifying
    @Procedure(procedureName = "modificar_datos_usuario")
    void modificarDatosUsuarioProcedure(
        @Param("p_id_usuario") Integer idUsuario,
        @Param("p_nombre") String nombre,
        @Param("p_apellido") String apellido,
        @Param("p_email") String email
    );

    // Stored Procedure para devolver a un usuario al rol "usuario" (id 7)
    @Transactional
    @Modifying
    @Procedure(procedureName = "degradar_a_usuario")
    void degradarAUsuarioProcedure(@Param("p_id_usuario") Integer idUsuario);

    // Stored Procedure que lista todos los usuarios con el nombre de su rol
    // columnas: id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol, nombre_rol
    @Query(value = "CALL listar_usuarios()", nativeQuery = true)
    List<Object[]> listarUsuariosProcedure();

    // Stored Procedure que busca por nombre de usuario, nombre, apellido o email
    // columnas: id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol
    @Query(value = "CALL buscar_usuario(:texto)", nativeQuery = true)
    List<Object[]> buscarUsuarioProcedure(@Param("texto") String texto);

    // Stored Procedure que lista los usuarios registrados en los ultimos N dias
    // columnas: id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol
    @Query(value = "CALL listar_usuarios_recientes(:dias)", nativeQuery = true)
    List<Object[]> listarUsuariosRecientesProcedure(@Param("dias") Integer dias);

    // Stored Procedure que devuelve el historial de cambios de rol (auditoria)
    // columnas: id_log, id_usuario, nombre_usuario, rol_anterior, rol_nuevo, fecha
    @Query(value = "CALL ver_log_cambios_rol(:limite)", nativeQuery = true)
    List<Object[]> verLogCambiosRolProcedure(@Param("limite") Integer limite);
}
