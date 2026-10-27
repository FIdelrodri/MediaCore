package com.mediacore.repository;

import com.mediacore.model.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, Integer> {

    // Stored Procedure para crear un anuncio (devuelve el id generado por el parametro OUT)
    @Transactional
    @Procedure(procedureName = "crear_anuncio", outputParameterName = "p_id_anuncio")
    Integer crearAnuncioProcedure(
        @Param("p_nombre") String nombre,
        @Param("p_descripcion") String descripcion,
        @Param("p_genero") String genero,
        @Param("p_duracion") Integer duracion,
        @Param("p_empresa") String empresa
    );

    // Stored Procedure para modificar un anuncio
    @Transactional
    @Modifying
    @Procedure(procedureName = "modificar_anuncio")
    void modificarAnuncioProcedure(
        @Param("p_id_anuncio") Integer idAnuncio,
        @Param("p_nombre") String nombre,
        @Param("p_descripcion") String descripcion,
        @Param("p_genero") String genero,
        @Param("p_duracion") Integer duracion,
        @Param("p_empresa") String empresa
    );

    // Stored Procedure para eliminar un anuncio (el trigger borra tambien su bloque)
    @Transactional
    @Modifying
    @Procedure(procedureName = "eliminar_anuncio")
    void eliminarAnuncioProcedure(@Param("p_id_anuncio") Integer idAnuncio);

    // Stored Procedure que agrega el bloque del anuncio al final de la grilla
    @Transactional
    @Modifying
    @Procedure(procedureName = "insertar_anuncio_en_grilla")
    void insertarAnuncioEnGrillaProcedure(@Param("p_id_anuncio") Integer idAnuncio);

    // Stored Procedure que busca anuncios por empresa (coincidencia parcial)
    // columnas: id_anuncio, nombre, descripcion, genero, duracion, empresa
    @Query(value = "CALL listar_anuncios_por_empresa(:empresa)", nativeQuery = true)
    List<Object[]> listarAnunciosPorEmpresaProcedure(@Param("empresa") String empresa);

    // Stored Procedure que busca anuncios con duracion entre un minimo y un maximo
    @Query(value = "CALL anuncios_por_duracion(:min, :max)", nativeQuery = true)
    List<Object[]> anunciosPorDuracionProcedure(@Param("min") Integer min, @Param("max") Integer max);
}
