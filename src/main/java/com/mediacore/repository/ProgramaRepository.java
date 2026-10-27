package com.mediacore.repository;

import com.mediacore.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Integer> {

    // Stored Procedure para crear un programa (devuelve el id generado por el parametro OUT)
    @Transactional
    @Procedure(procedureName = "crear_programa", outputParameterName = "p_id_programa")
    Integer crearProgramaProcedure(
        @Param("p_nombre") String nombre,
        @Param("p_descripcion") String descripcion,
        @Param("p_genero") String genero,
        @Param("p_duracion") Integer duracion
    );

    // Stored Procedure para modificar un programa
    @Transactional
    @Modifying
    @Procedure(procedureName = "modificar_programa")
    void modificarProgramaProcedure(
        @Param("p_id_programa") Integer idPrograma,
        @Param("p_nombre") String nombre,
        @Param("p_descripcion") String descripcion,
        @Param("p_genero") String genero,
        @Param("p_duracion") Integer duracion
    );

    // Stored Procedure para eliminar un programa (el trigger borra tambien su bloque)
    @Transactional
    @Modifying
    @Procedure(procedureName = "eliminar_programa")
    void eliminarProgramaProcedure(@Param("p_id_programa") Integer idPrograma);

    // Stored Procedure que agrega el bloque del programa al final de la grilla
    @Transactional
    @Modifying
    @Procedure(procedureName = "programar_programa")
    void programarProgramaProcedure(@Param("p_id_programa") Integer idPrograma);

    // Stored Procedure que lista todos los programas
    // columnas: id_programa, nombre, descripcion, genero, duracion
    @Query(value = "CALL listar_programas()", nativeQuery = true)
    List<Object[]> listarProgramasProcedure();

    // Stored Procedure que busca programas por genero
    @Query(value = "CALL buscar_programas_por_genero(:genero)", nativeQuery = true)
    List<Object[]> buscarProgramasPorGeneroProcedure(@Param("genero") String genero);

    // Stored Procedure que devuelve los N programas de mayor duracion
    @Query(value = "CALL programas_mas_largos(:n)", nativeQuery = true)
    List<Object[]> programasMasLargosProcedure(@Param("n") Integer n);
}
