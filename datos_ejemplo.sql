-- =====================================================================
-- MEDIACORE - Datos de ejemplo
-- Ejecutar DESPUES de sql.sql (phpMyAdmin: pestana Importar).
-- Crea programas, anuncios y vivos (los triggers generan sus bloques) y
-- arma una grilla de ejemplo con varias de las operaciones de posiciones.
-- No hace falta DELIMITER: son solo INSERT y CALL.
-- =====================================================================

USE mediacore;

-- Empezar con la grilla vacia (borra solo la cola, no el contenido)
CALL vaciar_grilla();

-- =====================================================================
-- 1. PROGRAMAS (duracion en segundos)
-- =====================================================================
CALL crear_programa('Buenos Dias Argentina', 'Magazine matutino con noticias y entrevistas', 'Magazine', 3600, @p_buenos_dias);
CALL crear_programa('El Noticiero del Mediodia', 'Informativo con las noticias del dia', 'Noticias', 2700, @p_noticiero);
CALL crear_programa('Cocina en Casa', 'Recetas faciles para toda la familia', 'Cocina', 1800, @p_cocina);
CALL crear_programa('Documentales del Mundo', 'Ciclo de documentales de naturaleza', 'Documental', 3000, @p_documental);
CALL crear_programa('Dibujos Animados', 'Bloque infantil de la tarde', 'Infantil', 2400, @p_dibujos);
CALL crear_programa('Cine de Medianoche', 'Pelicula de estreno', 'Cine', 5400, @p_cine);
CALL crear_programa('Musica y Mas', 'Videoclips y entrevistas a artistas', 'Musical', 1800, @p_musica);

-- =====================================================================
-- 2. ANUNCIOS (duracion en segundos)
-- =====================================================================
CALL crear_anuncio('Gaseosa Cola Fresca', 'Spot de verano', 'Bebidas', 30, 'Cola Fresca SA', @a_gaseosa);
CALL crear_anuncio('Auto Nuevo 0km', 'Lanzamiento del modelo 2027', 'Automotor', 45, 'Motors Argentina', @a_auto);
CALL crear_anuncio('Banco del Sur', 'Prestamos con tasa baja', 'Finanzas', 20, 'Banco del Sur', @a_banco);
CALL crear_anuncio('Supermercado Don Pepe', 'Ofertas de la semana', 'Comercio', 25, 'Don Pepe SRL', @a_super);
CALL crear_anuncio('Zapatillas Run', 'Nueva coleccion deportiva', 'Indumentaria', 15, 'Run Sports', @a_zapas);
CALL crear_anuncio('Seguro Hogar Plus', 'Cuida lo que mas queres', 'Seguros', 30, 'Aseguradora Norte', @a_seguro);
CALL crear_anuncio('Pizzeria La Napolitana', 'Dos por uno los martes', 'Gastronomia', 20, 'La Napolitana', @a_pizza);
CALL crear_anuncio('Campana Vacunacion', 'Mensaje de salud publica', 'Institucional', 40, 'Ministerio de Salud', @a_vacuna);

-- =====================================================================
-- 3. VIVOS (en directo)
-- =====================================================================
CALL crear_vivo('Partido de la Liga', 'Transmision en directo del clasico', 7200, @v_partido);
CALL crear_vivo('Sesion del Concejo', 'Sesion ordinaria del Concejo Deliberante', 5400, @v_concejo);
CALL crear_vivo('Recital en Vivo', 'Concierto desde el anfiteatro', 6000, @v_recital);
CALL crear_vivo('Entrevista Especial', 'Entrevista en estudio al intendente', 2400, @v_entrevista);

-- =====================================================================
-- 4. ARMADO DE LA GRILLA (se agregan al final, en este orden)
--    El primero queda al aire y arranca a correr su tiempo.
-- =====================================================================
CALL programar_programa(@p_buenos_dias);   -- posicion 1 (al aire)
CALL insertar_anuncio_en_grilla(@a_gaseosa);
CALL insertar_anuncio_en_grilla(@a_banco);
CALL programar_programa(@p_cocina);
CALL insertar_anuncio_en_grilla(@a_super);
CALL programar_programa(@p_noticiero);
CALL insertar_anuncio_en_grilla(@a_auto);
CALL programar_vivo(@v_entrevista);
CALL insertar_anuncio_en_grilla(@a_pizza);
CALL programar_programa(@p_documental);
CALL insertar_anuncio_en_grilla(@a_seguro);
CALL programar_vivo(@v_partido);
CALL insertar_anuncio_en_grilla(@a_zapas);
CALL programar_programa(@p_dibujos);

-- =====================================================================
-- 5. EJEMPLOS DE TRANSACCIONES SOBRE LA GRILLA
-- =====================================================================

-- Insertar un anuncio de salud en la posicion 3 (corre los demas)
CALL obtener_bloque('anuncio', @a_vacuna, @bloque_vacuna);
CALL insertar_bloque_en_posicion(@bloque_vacuna, 3, @id_pos_nueva);

-- Intercambiar la posicion 5 con la 7
CALL intercambiar_posiciones(5, 7);

-- Mover el vivo del partido (ahora en la posicion 13) a la posicion 8
CALL mover_bloque(13, 8);

-- Repetir un anuncio: duplicar la posicion 2 (queda justo despues)
CALL duplicar_posicion(2);

-- =====================================================================
-- 6. VER EL RESULTADO
--    Quedan sin programar: Musica y Mas, Cine de Medianoche,
--    Sesion del Concejo y Recital en Vivo (sirven para probar
--    agregar_bloque_a_grilla / reemplazar_bloque).
-- =====================================================================
CALL ver_grilla_con_horarios_proyectados();
CALL bloque_en_aire();
CALL duracion_total_grilla();
CALL listar_bloques_no_usados();
