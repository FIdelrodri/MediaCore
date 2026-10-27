package com.mediacore.repository;

import com.mediacore.model.Vivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VivoRepository extends JpaRepository<Vivo, Integer> {

    // Stored Procedure para crear un vivo (devuelve el id generado por el parametro OUT)
    @Transactional
    @Procedure(procedureName = "crear_vivo", outputParameterName = "p_id_envivo")
    Integer crearVivoProcedure(
        @Param("p_titulo") String titulo,
        @Param("p_descripcion") String descripcion,
        @Param("p_duracion") Integer duracion
    );

    // Stored Procedure para modificar un vivo (no permite cambiar la duracion si ya fue retransmitido)
    @Transactional
    @Modifying
    @Procedure(procedureName = "modificar_vivo")
    void modificarVivoProcedure(
        @Param("p_id_envivo") Integer idEnvivo,
        @Param("p_titulo") String titulo,
        @Param("p_descripcion") String descripcion,
        @Param("p_duracion") Integer duracion
    );

    // Stored Procedure para eliminar un vivo (el trigger borra tambien su bloque)
    @Transactional
    @Modifying
    @Procedure(procedureName = "eliminar_vivo")
    void eliminarVivoProcedure(@Param("p_id_envivo") Integer idEnvivo);

    // Stored Procedure que marca un vivo como retransmitido
    @Transactional
    @Modifying
    @Procedure(procedureName = "marcar_como_retransmitido")
    void marcarComoRetransmitidoProcedure(@Param("p_id_envivo") Integer idEnvivo);

    // Stored Procedure que agrega el bloque del vivo al final de la grilla
    @Transactional
    @Modifying
    @Procedure(procedureName = "programar_vivo")
    void programarVivoProcedure(@Param("p_id_envivo") Integer idEnvivo);

    // Stored Procedure que lista todos los vivos
    // columnas: id_envivo, titulo, descripcion, duracion, retransmitido
    @Query(value = "CALL listar_vivos()", nativeQuery = true)
    List<Object[]> listarVivosProcedure();

    // Stored Procedure que lista los vivos que todavia no fueron retransmitidos
    // columnas: id_envivo, titulo, descripcion, duracion, retransmitido
    @Query(value = "CALL listar_vivos_pendientes()", nativeQuery = true)
    List<Object[]> listarVivosPendientesProcedure();
}
