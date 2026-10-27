package com.mediacore.repository;

import com.mediacore.model.Bloque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BloqueRepository extends JpaRepository<Bloque, Integer> {

    // Devuelve el id del bloque de un contenido (parametro OUT), o null si no existe
    @Transactional
    @Procedure(procedureName = "obtener_bloque", outputParameterName = "p_id_bloque")
    Integer obtenerBloqueProcedure(
        @Param("p_tipo") String tipo,
        @Param("p_id_referencia") Integer idReferencia
    );

    @Query(value = "CALL listar_todos_los_bloques()", nativeQuery = true)
    List<Object[]> listarTodosLosBloquesProcedure();

    @Query(value = "CALL listar_bloques_por_tipo(:tipo)", nativeQuery = true)
    List<Object[]> listarBloquesPorTipoProcedure(@Param("tipo") String tipo);

    @Query(value = "CALL listar_bloques_no_usados()", nativeQuery = true)
    List<Object[]> listarBloquesNoUsadosProcedure();

    @Query(value = "CALL duracion_total_por_tipo()", nativeQuery = true)
    List<Object[]> duracionTotalPorTipoProcedure();

    // Vuelve a leer la duracion desde el contenido original
    @Transactional
    @Procedure(procedureName = "recalcular_duracion_bloque")
    void recalcularDuracionBloqueProcedure(@Param("p_id_bloque") Integer idBloque);

    // Borra los bloques sin contenido y devuelve la cantidad eliminada
    @Transactional
    @Query(value = "CALL limpiar_bloques_huerfanos()", nativeQuery = true)
    List<Object> limpiarBloquesHuerfanosProcedure();
}
