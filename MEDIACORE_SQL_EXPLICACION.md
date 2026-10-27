# MediaCore - Explicación de la base de datos (para una IA)

Este documento resume cómo funciona `sql.sql` para poder trabajar sobre el proyecto sin leer todo el script.
Motor: MySQL 5.7/8 o MariaDB 10.x (probado en MariaDB 10.11). Base: `mediacore`.
Archivos: `sql.sql` (esquema, triggers, procedures, vista, evento) y `datos_ejemplo.sql` (datos de prueba; se ejecuta después).

## 1. Idea general

MediaCore es una **grilla de programación en vivo tipo playlist**. Hay tres tipos de contenido (programa, vivo, anuncio).
Cada contenido tiene un **bloque** que lo representa. La **grilla** es una cola de bloques ordenada por la columna `orden`:
el bloque con `orden = 1` está **al aire**; cuando termina, se elimina de la cola y sube el siguiente.
Todo el comportamiento vive en la base (triggers, procedures y un evento); la aplicación solo llama a procedures.

```
programa / vivo / anuncio --(trigger AFTER INSERT)--> bloque --(procedure)--> grilla (cola ordenada)
                                                                               |
                                    evento cada 1 s -> tick_grilla() -> avanza el bloque vencido
                                                                               |
                                    historial_emision <--(trigger AFTER DELETE en grilla)
```

## 2. Tablas

| Tabla | Columnas clave | Notas |
|---|---|---|
| `programa` | id_programa, nombre, descripcion, genero, duracion (seg) | |
| `anuncio` | id_anuncio, nombre, descripcion, genero, duracion, empresa | |
| `vivo` | id_envivo, titulo (100), descripcion, duracion, retransmitido (bool, default FALSE) | antes `retrasmitido` |
| `rol` | id_rol, nombre_rol (UNIQUE) | **Tabla fija**: 1 Administrador General, 2 Administrador, 3 escenografo, 4 administrador de audio, 5 Administrador de planilla, 6 personal de postproduccion, 7 usuario. No hay procedures para modificarla. |
| `usuario` | id_usuario, nombre_usuario (UNIQUE), nombre, apellido, email (UNIQUE), contrasena, fecha_registro, id_rol (default 7) | Contraseña en texto plano. Usuario inicial: `a` / `a` (rol 1). |
| `bloque` | id_bloque, tipo_contenido ENUM('programa','vivo','anuncio'), id_referencia, duracion | UNIQUE(tipo_contenido, id_referencia): un contenido = un bloque. `id_referencia` apunta al id del contenido según el tipo (sin FK, es polimórfico). |
| `grilla` | id_posicion (PK autoincrement), id_bloque (FK, ON DELETE CASCADE), **orden**, duracion, hora_inicio, hora_fin | `orden` es la posición real (1 = al aire). `id_posicion` solo identifica la fila. Un mismo bloque puede aparecer varias veces en la grilla. |
| `historial_emision` | id_historial, id_bloque, tipo_contenido, titulo, hora_inicio, hora_fin_real | Lo llena un trigger. |
| `log_cambios_rol` | id_log, id_usuario, rol_anterior, rol_nuevo, fecha | Lo llena un trigger. |

Vistas: `vista_bloques` (bloque + título + duración del contenido) y `vista_grilla_activa` (grilla ordenada con título).

## 3. Reglas e invariantes (importante)

1. **Posición = valor de `orden`.** Después de cualquier operación de grilla `orden` queda 1..n sin huecos
   (`sp_compactar_orden`). Puede haber huecos momentáneos si un contenido se borra y la cascada elimina filas de grilla;
   la siguiente operación los compacta.
2. **La posición 1 está al aire** y `hora_inicio`/`hora_fin` solo existen para ella. Las demás tienen NULL.
   No se puede intercambiar, mover, reemplazar ni insertar sobre la posición 1 (sí quitarla, que equivale a terminarla).
3. Al agregar a una grilla vacía (o cuando el primero no tiene hora), el bloque arranca ya: `hora_inicio = NOW()`,
   `hora_fin = NOW() + duracion`.
4. **Duración > 0** para programar. Contenidos con duración 0 no entran a la grilla.
5. `grilla.duracion` siempre coincide con `bloque.duracion`; si se edita la duración de un contenido, triggers sincronizan
   bloque y grilla (y recalculan `hora_fin` si estaba al aire).
6. Los errores de reglas de negocio se lanzan con `SIGNAL SQLSTATE '45000'` y mensaje en español legible. Ante cualquier
   error las transacciones hacen `ROLLBACK` y re-lanzan (`RESIGNAL`).
7. Las operaciones de grilla bloquean la cola (`SELECT COUNT(*) FROM grilla FOR UPDATE`), así son seguras si el evento
   dispara a la vez. Muy raramente puede ocurrir un deadlock (error 1213): la aplicación debería reintentar.

## 4. Procedures

Convención: parámetros `p_*`; los `OUT` devuelven el id creado. Los de listado devuelven filas (`SELECT`).

**Internos (no llamar desde la app):** `sp_compactar_orden`, `sp_arrancar_primero`, `sp_sync_duracion_bloque(id_bloque, duracion)`,
`sp_avanzar_interno(forzar)`.

**Grilla (transaccionales):**
- `agregar_bloque_a_grilla(id_bloque)` agrega al final.
- `insertar_bloque_en_posicion(id_bloque, posicion, OUT id_posicion)` inserta en el medio (posicion 2..n+1; 1 solo si la grilla está vacía).
- `intercambiar_posiciones(pos_a, pos_b)` intercambia dos posiciones (ambas >= 2, distintas).
- `mover_bloque(pos_origen, pos_destino)` mueve y corre los del medio (ambas >= 2).
- `reemplazar_bloque(posicion, id_bloque_nuevo)` cambia el contenido de una posición >= 2.
- `duplicar_posicion(posicion)` repite el bloque justo después.
- `quitar_de_grilla(posicion)` quita y compacta; posición 1 = terminar el bloque al aire.
- `vaciar_grilla()`, `avanzar_grilla()` (termina el actual, sube el siguiente), `saltar_bloque_actual()` (igual que avanzar).
- `tick_grilla()` lo ejecuta el evento: avanza solo si el actual venció o si el primero no estaba arrancado.

**Grilla (lectura):** `ver_grilla_con_horarios_proyectados()` (inicio/fin estimados de cada posición acumulando duraciones),
`bloque_en_aire()` (con segundos restantes), `duracion_total_grilla()`, `ver_historial_emision(limite)`.

**Programa:** `crear_programa(nombre, descripcion, genero, duracion, OUT id)`, `modificar_programa(id, nombre, descripcion, genero, duracion)`,
`eliminar_programa(id)`, `listar_programas()`, `buscar_programas_por_genero(genero)`, `programas_mas_largos(n)`, `programar_programa(id)` (lo agrega a la grilla).

**Anuncio:** `crear_anuncio(nombre, descripcion, genero, duracion, empresa, OUT id)`, `modificar_anuncio(...)`, `eliminar_anuncio(id)`,
`listar_anuncios()`, `listar_anuncios_por_empresa(empresa)`, `anuncios_por_duracion(min, max)`, `insertar_anuncio_en_grilla(id)`.

**Vivo:** `crear_vivo(titulo, descripcion, duracion, OUT id)`, `modificar_vivo(id, titulo, descripcion, duracion)`, `eliminar_vivo(id)`,
`marcar_como_retransmitido(id)`, `listar_vivos()`, `listar_vivos_pendientes()`, `programar_vivo(id)`.

**Bloque:** `obtener_bloque(tipo, id_referencia, OUT id_bloque)`, `listar_todos_los_bloques()`, `listar_bloques_por_tipo(tipo)`,
`listar_bloques_no_usados()`, `recalcular_duracion_bloque(id_bloque)`, `duracion_total_por_tipo()`, `limpiar_bloques_huerfanos()`.

**Rol (tabla fija, solo consultas):** `listar_usuarios_por_rol(id_rol)`, `contar_usuarios_por_rol()`. También `listar_roles()`.

**Usuario:** `registrar_usuario(nombre_usuario, nombre, apellido, email, contrasena)` (rol 7), `login_usuario(nombre_usuario, contrasena)`
(devuelve el usuario sin contraseña o ninguna fila), `asignar_rol_usuario(id_usuario, id_rol)`, `degradar_a_usuario(id_usuario)`,
`eliminar_usuario(id)`, `cambiar_contrasena(id, vieja, nueva)`, `modificar_datos_usuario(id, nombre, apellido, email)`,
`buscar_usuario(texto)`, `listar_usuarios()`, `listar_usuarios_recientes(dias)`, `ver_log_cambios_rol(limite)`.

Flujo típico para programar algo nuevo: `crear_programa(..., @id)` -> (el trigger crea el bloque) -> `programar_programa(@id)`.
O con bloque conocido: `obtener_bloque('anuncio', @id, @b)` -> `agregar_bloque_a_grilla(@b)`.

## 5. Triggers

| Trigger | Qué hace |
|---|---|
| `after_insert_programa/vivo/anuncio` | Crean el bloque del contenido (duración `IFNULL(duracion,0)`). |
| `after_update_programa/vivo/anuncio` | Si cambia la duración, llaman a `sp_sync_duracion_bloque` (bloque + grilla + hora_fin del que está al aire). |
| `after_delete_programa/vivo/anuncio` | Borran el bloque; la FK en cascada lo saca de la grilla. |
| `before_update_vivo` | Impide cambiar la duración de un vivo ya retransmitido. |
| `before_insert_grilla` | Valida que el bloque exista, que la duración sea > 0 y coincida con la del bloque; si `orden` no viene, usa MAX+1. |
| `after_delete_grilla` | Si la fila ya había arrancado (`hora_inicio` no nulo) la guarda en `historial_emision`. |
| `before_insert_usuario` / `before_update_usuario` | Validan formato de email; `id_rol` NULL pasa a 7; no permiten quitar el rol al último Administrador General. |
| `after_update_usuario` | Registra en `log_cambios_rol` cuando cambia el rol. |
| `before_delete_usuario` | Impide borrar al último Administrador General. |

Limitación de MySQL: los borrados por cascada (FK) **no disparan triggers**; por eso borrar un contenido que está en la grilla
no genera fila en `historial_emision`.

## 6. Evento

`evt_limpiar_y_avanzar_grilla` corre cada 1 segundo y hace `CALL tick_grilla()`. Requiere `event_scheduler = ON`
(el script lo activa con `SET GLOBAL`, pero XAMPP lo apaga al reiniciar; hay que volver a activarlo).
Si la grilla se queda quieta, revisar `SHOW VARIABLES LIKE 'event_scheduler'`.

## 7. Cómo extender sin romper nada

- Nuevo procedure sobre la grilla: iniciar `START TRANSACTION`, `SELECT COUNT(*) FROM grilla FOR UPDATE`, llamar `sp_compactar_orden()`,
  validar posiciones (rango y que no sea la 1), modificar `orden`, llamar `sp_arrancar_primero()` si pudo cambiar el primero, `COMMIT`.
  Incluir el `DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;` al inicio.
- Nunca modificar `id_posicion` para reordenar; usar siempre `orden`.
- Un tipo de contenido nuevo requiere: tabla, valor en el ENUM de `bloque`, triggers insert/update/delete, y un join en `vista_bloques`.
- En phpMyAdmin: importar con la pestaña **Importar** (el script usa `DELIMITER`).
