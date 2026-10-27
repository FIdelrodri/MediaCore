package com.mediacore.repository;

import com.mediacore.model.Grilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GrillaRepository extends JpaRepository<Grilla, Integer> {

    // ---- Consultas ----

    // columnas: orden, id_posicion, id_bloque, tipo_contenido, titulo,
    //           duracion_segundos, inicio_proyectado, fin_proyectado
    @Query(value = "CALL ver_grilla_con_horarios_proyectados()", nativeQuery = true)
    List<Object[]> verGrillaConHorariosProyectadosProcedure();

    // columnas: cantidad_bloques, segundos_totales, segundos_restantes
    @Query(value = "CALL duracion_total_grilla()", nativeQuery = true)
    List<Object[]> duracionTotalGrillaProcedure();

    // columnas: orden, id_posicion, id_bloque, tipo_contenido, titulo,
    //           duracion_segundos, hora_inicio, hora_fin, segundos_restantes
    @Query(value = "CALL bloque_en_aire()", nativeQuery = true)
    List<Object[]> bloqueEnAireProcedure();

    // columnas: id_historial, id_bloque, tipo_contenido, titulo, hora_inicio, hora_fin_real
    @Query(value = "CALL ver_historial_emision(:limite)", nativeQuery = true)
    List<Object[]> verHistorialEmisionProcedure(@Param("limite") Integer limite);

    // ---- Operaciones sobre la cola (cada procedure maneja su propia transaccion) ----

    // Agrega un bloque al final de la cola
    @Transactional
    @Modifying
    @Procedure(procedureName = "agregar_bloque_a_grilla")
    void agregarBloqueAGrillaProcedure(@Param("p_id_bloque") Integer idBloque);

    // Termina el bloque al aire y sube el siguiente
    @Transactional
    @Modifying
    @Procedure(procedureName = "avanzar_grilla")
    void avanzarGrillaProcedure();

    // Igual que avanzar_grilla: termina el bloque actual sin importar la hora
    @Transactional
    @Modifying
    @Procedure(procedureName = "saltar_bloque_actual")
    void saltarBloqueActualProcedure();

    // Quita una posicion y compacta la cola
    @Transactional
    @Modifying
    @Procedure(procedureName = "quitar_de_grilla")
    void quitarDeGrillaProcedure(@Param("p_posicion") Integer posicion);

    @Transactional
    @Modifying
    @Procedure(procedureName = "vaciar_grilla")
    void vaciarGrillaProcedure();

    // ---- Reordenamiento de la cola (la posicion 1 esta al aire y no se puede tocar) ----

    // Inserta un bloque en una posicion y corre los demas (devuelve el id de la nueva fila por el parametro OUT)
    @Transactional
    @Procedure(procedureName = "insertar_bloque_en_posicion", outputParameterName = "p_id_posicion")
    Integer insertarBloqueEnPosicionProcedure(
        @Param("p_id_bloque") Integer idBloque,
        @Param("p_posicion") Integer posicion
    );

    // Intercambia dos posiciones de la cola
    @Transactional
    @Modifying
    @Procedure(procedureName = "intercambiar_posiciones")
    void intercambiarPosicionesProcedure(
        @Param("p_pos_a") Integer posicionA,
        @Param("p_pos_b") Integer posicionB
    );

    // Mueve un bloque a otra posicion y corre los del medio
    @Transactional
    @Modifying
    @Procedure(procedureName = "mover_bloque")
    void moverBloqueProcedure(
        @Param("p_pos_origen") Integer origen,
        @Param("p_pos_destino") Integer destino
    );

    // Cambia el contenido de una posicion sin alterar el orden
    @Transactional
    @Modifying
    @Procedure(procedureName = "reemplazar_bloque")
    void reemplazarBloqueProcedure(
        @Param("p_posicion") Integer posicion,
        @Param("p_id_bloque_nuevo") Integer idBloqueNuevo
    );

    // Repite un bloque justo despues de su posicion
    @Transactional
    @Modifying
    @Procedure(procedureName = "duplicar_posicion")
    void duplicarPosicionProcedure(@Param("p_posicion") Integer posicion);
}
