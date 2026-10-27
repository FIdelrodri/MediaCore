package com.mediacore.repository;

import com.mediacore.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// La tabla rol es fija: solo se consulta (no hay procedures de alta, baja ni modificacion)
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Stored Procedure que lista los roles
    // columnas: id_rol, nombre_rol
    @Query(value = "CALL listar_roles()", nativeQuery = true)
    List<Object[]> listarRolesProcedure();

    // Stored Procedure que lista los usuarios de un rol
    // columnas: id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol
    @Query(value = "CALL listar_usuarios_por_rol(:idRol)", nativeQuery = true)
    List<Object[]> listarUsuariosPorRolProcedure(@Param("idRol") Integer idRol);

    // Stored Procedure que cuenta usuarios por rol
    // columnas: id_rol, nombre_rol, cantidad_usuarios
    @Query(value = "CALL contar_usuarios_por_rol()", nativeQuery = true)
    List<Object[]> contarUsuariosPorRolProcedure();
}
