-- =====================================================================
-- MEDIACORE - Base de datos completa
-- Grilla con columna "orden" (opcion B), triggers, procedures y
-- transacciones entre posiciones.
-- Compatible con MySQL 5.7/8 y MariaDB 10.x (XAMPP).
-- phpMyAdmin: pestana Importar -> elegir este archivo (usa DELIMITER).
-- =====================================================================

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS mediacore;
CREATE DATABASE mediacore CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mediacore;
SET GLOBAL event_scheduler = ON;

-- =====================================================================
-- 1. TABLAS PRINCIPALES DE CONTENIDO
-- =====================================================================

CREATE TABLE anuncio (
  id_anuncio INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  descripcion TEXT DEFAULT NULL,
  genero VARCHAR(50) DEFAULT NULL,
  duracion INT(20) DEFAULT NULL,
  empresa VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id_anuncio)
);

CREATE TABLE programa (
  id_programa INT(11) NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(500) DEFAULT NULL,
  genero VARCHAR(50) DEFAULT NULL,
  duracion INT(20) DEFAULT NULL,
  PRIMARY KEY (id_programa)
);

-- Cambios respecto a la version anterior:
--   titulo VARCHAR(20) -> VARCHAR(100)
--   retrasmitido       -> retransmitido (y con DEFAULT FALSE)
CREATE TABLE vivo (
  id_envivo INT(11) NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(100) NOT NULL,
  descripcion VARCHAR(500) NOT NULL,
  duracion INT(20) NOT NULL,
  retransmitido BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id_envivo)
);

-- =====================================================================
-- 2. ROLES Y USUARIOS
-- =====================================================================

CREATE TABLE rol (
  id_rol INT(11) NOT NULL AUTO_INCREMENT,
  nombre_rol VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_rol),
  UNIQUE KEY uq_rol_nombre (nombre_rol)
);

INSERT INTO rol (nombre_rol) VALUES
('Administrador General'),
('Administrador'),
('escenografo'),
('administrador de audio'),
('Administrador de planilla'),
('personal de postproduccion'),
('usuario');

CREATE TABLE usuario (
  id_usuario INT(11) NOT NULL AUTO_INCREMENT,
  nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  contrasena VARCHAR(255) NOT NULL,
  fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP(),
  id_rol INT(11) DEFAULT 7,
  PRIMARY KEY (id_usuario),
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
);

-- Auditoria de cambios de rol (la llena un trigger)
CREATE TABLE log_cambios_rol (
  id_log INT(11) NOT NULL AUTO_INCREMENT,
  id_usuario INT(11) NOT NULL,
  rol_anterior INT(11) DEFAULT NULL,
  rol_nuevo INT(11) DEFAULT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
  PRIMARY KEY (id_log)
);

-- =====================================================================
-- 3. BLOQUES Y GRILLA DINAMICA
-- =====================================================================

CREATE TABLE bloque (
  id_bloque INT(11) NOT NULL AUTO_INCREMENT,
  tipo_contenido ENUM('programa', 'vivo', 'anuncio') NOT NULL,
  id_referencia INT(11) NOT NULL,
  duracion INT(11) NOT NULL,
  PRIMARY KEY (id_bloque),
  UNIQUE KEY uq_bloque_contenido (tipo_contenido, id_referencia)
);

-- "orden" es la posicion en la cola (1 = al aire). id_posicion ya no
-- define el orden: solo identifica la fila.
CREATE TABLE grilla (
  id_posicion INT(11) NOT NULL AUTO_INCREMENT,
  id_bloque INT(11) NOT NULL,
  orden INT(11) NOT NULL DEFAULT 0,
  duracion INT(11) NOT NULL,
  hora_inicio DATETIME DEFAULT NULL,
  hora_fin DATETIME DEFAULT NULL,
  PRIMARY KEY (id_posicion),
  KEY idx_grilla_orden (orden),
  CONSTRAINT fk_grilla_bloque FOREIGN KEY (id_bloque) REFERENCES bloque (id_bloque) ON DELETE CASCADE
);

-- Historial de lo que ya salio al aire (lo llena un trigger)
CREATE TABLE historial_emision (
  id_historial INT(11) NOT NULL AUTO_INCREMENT,
  id_bloque INT(11) NOT NULL,
  tipo_contenido VARCHAR(20) DEFAULT NULL,
  titulo VARCHAR(100) DEFAULT NULL,
  hora_inicio DATETIME NOT NULL,
  hora_fin_real DATETIME NOT NULL,
  PRIMARY KEY (id_historial)
);

-- =====================================================================
-- 4. VISTAS
-- =====================================================================

CREATE VIEW vista_bloques AS
SELECT
  b.id_bloque,
  b.tipo_contenido,
  b.id_referencia,
  COALESCE(p.nombre, v.titulo, a.nombre) AS titulo,
  b.duracion
FROM bloque b
LEFT JOIN programa p ON b.tipo_contenido = 'programa' AND b.id_referencia = p.id_programa
LEFT JOIN vivo v     ON b.tipo_contenido = 'vivo'     AND b.id_referencia = v.id_envivo
LEFT JOIN anuncio a  ON b.tipo_contenido = 'anuncio'  AND b.id_referencia = a.id_anuncio;

CREATE VIEW vista_grilla_activa AS
SELECT
  g.orden,
  g.id_posicion,
  g.id_bloque,
  vb.tipo_contenido,
  vb.titulo,
  g.duracion AS duracion_segundos,
  g.hora_inicio,
  g.hora_fin
FROM grilla g
INNER JOIN vista_bloques vb ON g.id_bloque = vb.id_bloque
ORDER BY g.orden ASC, g.id_posicion ASC;

-- =====================================================================
-- 5. PROCEDIMIENTOS INTERNOS (los usan los demas; sin transacciones)
-- =====================================================================

DELIMITER $$

-- Deja "orden" como 1..n sin huecos (respeta el orden actual)
CREATE PROCEDURE sp_compactar_orden()
BEGIN
  SET @n_orden := 0;
  UPDATE grilla
  SET orden = (@n_orden := @n_orden + 1)
  ORDER BY orden ASC, id_posicion ASC;
END $$

-- Si el primero de la cola no tiene hora de inicio, lo pone al aire ahora
CREATE PROCEDURE sp_arrancar_primero()
BEGIN
  DECLARE v_id INT DEFAULT NULL;
  DECLARE v_inicio DATETIME DEFAULT NULL;
  DECLARE v_dur INT DEFAULT NULL;

  SELECT id_posicion, hora_inicio, duracion
    INTO v_id, v_inicio, v_dur
  FROM grilla
  ORDER BY orden ASC, id_posicion ASC
  LIMIT 1;

  IF v_id IS NOT NULL AND v_inicio IS NULL THEN
    UPDATE grilla
    SET hora_inicio = NOW(),
        hora_fin = DATE_ADD(NOW(), INTERVAL v_dur SECOND)
    WHERE id_posicion = v_id;
  END IF;
END $$

-- Cambia la duracion de un bloque y la propaga a la grilla.
-- Si el bloque esta al aire, recalcula tambien su hora_fin.
CREATE PROCEDURE sp_sync_duracion_bloque(IN p_id_bloque INT, IN p_duracion INT)
BEGIN
  UPDATE bloque SET duracion = p_duracion WHERE id_bloque = p_id_bloque;
  UPDATE grilla
  SET duracion = p_duracion,
      hora_fin = IF(hora_inicio IS NULL, NULL, DATE_ADD(hora_inicio, INTERVAL p_duracion SECOND))
  WHERE id_bloque = p_id_bloque;
END $$

-- Nucleo de avance de la grilla.
--   p_forzar = 1: termina el bloque actual sin importar la hora.
--   p_forzar = 0: solo avanza si el bloque actual ya vencio.
-- Siempre deja al aire al primero de la cola si todavia no tenia hora.
CREATE PROCEDURE sp_avanzar_interno(IN p_forzar TINYINT)
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE v_id INT DEFAULT NULL;
  DECLARE v_fin DATETIME DEFAULT NULL;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;

  SELECT id_posicion, hora_fin INTO v_id, v_fin
  FROM grilla
  ORDER BY orden ASC, id_posicion ASC
  LIMIT 1;

  IF v_id IS NOT NULL THEN
    IF p_forzar = 1 OR (v_fin IS NOT NULL AND v_fin <= NOW()) THEN
      DELETE FROM grilla WHERE id_posicion = v_id;
      CALL sp_compactar_orden();
    END IF;
    CALL sp_arrancar_primero();
  END IF;
  COMMIT;
END $$

-- =====================================================================
-- 6. TRIGGERS
-- =====================================================================
DELIMITER ;

DELIMITER $$

-- ---- 6.1 Alta de contenido -> crea su bloque ----
CREATE TRIGGER after_insert_programa
AFTER INSERT ON programa
FOR EACH ROW
BEGIN
  INSERT INTO bloque (tipo_contenido, id_referencia, duracion)
  VALUES ('programa', NEW.id_programa, IFNULL(NEW.duracion, 0));
END $$

CREATE TRIGGER after_insert_vivo
AFTER INSERT ON vivo
FOR EACH ROW
BEGIN
  INSERT INTO bloque (tipo_contenido, id_referencia, duracion)
  VALUES ('vivo', NEW.id_envivo, IFNULL(NEW.duracion, 0));
END $$

CREATE TRIGGER after_insert_anuncio
AFTER INSERT ON anuncio
FOR EACH ROW
BEGIN
  INSERT INTO bloque (tipo_contenido, id_referencia, duracion)
  VALUES ('anuncio', NEW.id_anuncio, IFNULL(NEW.duracion, 0));
END $$

-- ---- 6.2 Cambio de duracion -> sincroniza bloque y grilla ----
CREATE TRIGGER after_update_programa
AFTER UPDATE ON programa
FOR EACH ROW
BEGIN
  DECLARE v_id_bloque INT DEFAULT NULL;
  IF NOT (OLD.duracion <=> NEW.duracion) THEN
    SELECT id_bloque INTO v_id_bloque FROM bloque
    WHERE tipo_contenido = 'programa' AND id_referencia = NEW.id_programa;
    IF v_id_bloque IS NOT NULL THEN
      CALL sp_sync_duracion_bloque(v_id_bloque, IFNULL(NEW.duracion, 0));
    END IF;
  END IF;
END $$

CREATE TRIGGER after_update_vivo
AFTER UPDATE ON vivo
FOR EACH ROW
BEGIN
  DECLARE v_id_bloque INT DEFAULT NULL;
  IF NOT (OLD.duracion <=> NEW.duracion) THEN
    SELECT id_bloque INTO v_id_bloque FROM bloque
    WHERE tipo_contenido = 'vivo' AND id_referencia = NEW.id_envivo;
    IF v_id_bloque IS NOT NULL THEN
      CALL sp_sync_duracion_bloque(v_id_bloque, IFNULL(NEW.duracion, 0));
    END IF;
  END IF;
END $$

CREATE TRIGGER after_update_anuncio
AFTER UPDATE ON anuncio
FOR EACH ROW
BEGIN
  DECLARE v_id_bloque INT DEFAULT NULL;
  IF NOT (OLD.duracion <=> NEW.duracion) THEN
    SELECT id_bloque INTO v_id_bloque FROM bloque
    WHERE tipo_contenido = 'anuncio' AND id_referencia = NEW.id_anuncio;
    IF v_id_bloque IS NOT NULL THEN
      CALL sp_sync_duracion_bloque(v_id_bloque, IFNULL(NEW.duracion, 0));
    END IF;
  END IF;
END $$

-- ---- 6.3 Baja de contenido -> borra su bloque (y por cascada la grilla) ----
CREATE TRIGGER after_delete_programa
AFTER DELETE ON programa
FOR EACH ROW
BEGIN
  DELETE FROM bloque WHERE tipo_contenido = 'programa' AND id_referencia = OLD.id_programa;
END $$

CREATE TRIGGER after_delete_vivo
AFTER DELETE ON vivo
FOR EACH ROW
BEGIN
  DELETE FROM bloque WHERE tipo_contenido = 'vivo' AND id_referencia = OLD.id_envivo;
END $$

CREATE TRIGGER after_delete_anuncio
AFTER DELETE ON anuncio
FOR EACH ROW
BEGIN
  DELETE FROM bloque WHERE tipo_contenido = 'anuncio' AND id_referencia = OLD.id_anuncio;
END $$

-- ---- 6.4 Vivo ya retransmitido: no se puede cambiar su duracion ----
CREATE TRIGGER before_update_vivo
BEFORE UPDATE ON vivo
FOR EACH ROW
BEGIN
  IF OLD.retransmitido = TRUE AND NOT (OLD.duracion <=> NEW.duracion) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'No se puede modificar la duracion de un vivo ya retransmitido';
  END IF;
END $$

-- ---- 6.5 Grilla: validaciones y orden automatico ----
CREATE TRIGGER before_insert_grilla
BEFORE INSERT ON grilla
FOR EACH ROW
BEGIN
  DECLARE v_dur_bloque INT DEFAULT NULL;

  SELECT duracion INTO v_dur_bloque FROM bloque WHERE id_bloque = NEW.id_bloque;

  IF v_dur_bloque IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El bloque indicado no existe';
  END IF;

  IF NEW.duracion IS NULL OR NEW.duracion <= 0 THEN
    SET NEW.duracion = v_dur_bloque;
  END IF;

  IF NEW.duracion <= 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'No se puede programar un bloque con duracion 0';
  END IF;

  IF NEW.duracion <> v_dur_bloque THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'La duracion de la grilla no coincide con la del bloque';
  END IF;

  IF NEW.orden IS NULL OR NEW.orden <= 0 THEN
    SET NEW.orden = (SELECT IFNULL(MAX(orden), 0) + 1 FROM grilla);
  END IF;
END $$

-- ---- 6.6 Grilla: historial de lo que salio al aire ----
CREATE TRIGGER after_delete_grilla
AFTER DELETE ON grilla
FOR EACH ROW
BEGIN
  DECLARE v_tipo VARCHAR(20) DEFAULT NULL;
  DECLARE v_titulo VARCHAR(100) DEFAULT NULL;

  IF OLD.hora_inicio IS NOT NULL THEN
    SELECT tipo_contenido, titulo INTO v_tipo, v_titulo
    FROM vista_bloques WHERE id_bloque = OLD.id_bloque;

    INSERT INTO historial_emision (id_bloque, tipo_contenido, titulo, hora_inicio, hora_fin_real)
    VALUES (OLD.id_bloque, v_tipo, v_titulo, OLD.hora_inicio, NOW());
  END IF;
END $$

-- ---- 6.7 Usuarios ----
CREATE TRIGGER before_insert_usuario
BEFORE INSERT ON usuario
FOR EACH ROW
BEGIN
  IF NEW.email NOT REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El email no tiene un formato valido';
  END IF;
  IF NEW.id_rol IS NULL THEN
    SET NEW.id_rol = 7;
  END IF;
END $$

CREATE TRIGGER before_update_usuario
BEFORE UPDATE ON usuario
FOR EACH ROW
BEGIN
  IF NEW.email NOT REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El email no tiene un formato valido';
  END IF;
  IF NEW.id_rol IS NULL THEN
    SET NEW.id_rol = 7;
  END IF;
  -- No dejar el sistema sin Administrador General
  IF OLD.id_rol = 1 AND NEW.id_rol <> 1
     AND (SELECT COUNT(*) FROM usuario WHERE id_rol = 1) <= 1 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'No se puede quitar el rol al ultimo Administrador General';
  END IF;
END $$

CREATE TRIGGER after_update_usuario
AFTER UPDATE ON usuario
FOR EACH ROW
BEGIN
  IF NOT (OLD.id_rol <=> NEW.id_rol) THEN
    INSERT INTO log_cambios_rol (id_usuario, rol_anterior, rol_nuevo)
    VALUES (NEW.id_usuario, OLD.id_rol, NEW.id_rol);
  END IF;
END $$

CREATE TRIGGER before_delete_usuario
BEFORE DELETE ON usuario
FOR EACH ROW
BEGIN
  IF OLD.id_rol = 1 AND (SELECT COUNT(*) FROM usuario WHERE id_rol = 1) <= 1 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'No se puede eliminar al ultimo Administrador General';
  END IF;
END $$

DELIMITER ;

-- Usuario administrador inicial (despues de crear los triggers)
INSERT INTO usuario (nombre_usuario, nombre, apellido, email, contrasena, id_rol)
VALUES ('a', 'Administrador', 'General', 'admin@mediacore.com', 'a', 1);

-- =====================================================================
-- 7. PROCEDIMIENTOS: ANUNCIO
-- =====================================================================
DELIMITER $$

CREATE PROCEDURE crear_anuncio(
  IN p_nombre VARCHAR(100), IN p_descripcion TEXT, IN p_genero VARCHAR(50),
  IN p_duracion INT, IN p_empresa VARCHAR(100), OUT p_id_anuncio INT)
BEGIN
  IF p_duracion IS NULL OR p_duracion <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La duracion del anuncio debe ser mayor a 0';
  END IF;
  INSERT INTO anuncio (nombre, descripcion, genero, duracion, empresa)
  VALUES (p_nombre, p_descripcion, p_genero, p_duracion, p_empresa);
  SET p_id_anuncio = LAST_INSERT_ID();
END $$

CREATE PROCEDURE modificar_anuncio(
  IN p_id_anuncio INT, IN p_nombre VARCHAR(100), IN p_descripcion TEXT,
  IN p_genero VARCHAR(50), IN p_duracion INT, IN p_empresa VARCHAR(100))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM anuncio WHERE id_anuncio = p_id_anuncio) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El anuncio no existe';
  END IF;
  IF p_duracion IS NULL OR p_duracion <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La duracion del anuncio debe ser mayor a 0';
  END IF;
  UPDATE anuncio
  SET nombre = p_nombre, descripcion = p_descripcion, genero = p_genero,
      duracion = p_duracion, empresa = p_empresa
  WHERE id_anuncio = p_id_anuncio;
END $$

CREATE PROCEDURE eliminar_anuncio(IN p_id_anuncio INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM anuncio WHERE id_anuncio = p_id_anuncio) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El anuncio no existe';
  END IF;
  DELETE FROM anuncio WHERE id_anuncio = p_id_anuncio;
END $$

CREATE PROCEDURE listar_anuncios_por_empresa(IN p_empresa VARCHAR(100))
BEGIN
  SELECT * FROM anuncio WHERE empresa LIKE CONCAT('%', p_empresa, '%') ORDER BY nombre;
END $$

CREATE PROCEDURE anuncios_por_duracion(IN p_min INT, IN p_max INT)
BEGIN
  SELECT * FROM anuncio WHERE duracion BETWEEN p_min AND p_max ORDER BY duracion;
END $$

CREATE PROCEDURE insertar_anuncio_en_grilla(IN p_id_anuncio INT)
BEGIN
  DECLARE v_id_bloque INT DEFAULT NULL;
  SELECT id_bloque INTO v_id_bloque FROM bloque
  WHERE tipo_contenido = 'anuncio' AND id_referencia = p_id_anuncio;
  IF v_id_bloque IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El anuncio no existe';
  END IF;
  CALL agregar_bloque_a_grilla(v_id_bloque);
END $$

-- =====================================================================
-- 8. PROCEDIMIENTOS: PROGRAMA
-- =====================================================================

CREATE PROCEDURE crear_programa(
  IN p_nombre VARCHAR(100), IN p_descripcion VARCHAR(500), IN p_genero VARCHAR(50),
  IN p_duracion INT, OUT p_id_programa INT)
BEGIN
  IF p_duracion IS NULL OR p_duracion <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La duracion del programa debe ser mayor a 0';
  END IF;
  INSERT INTO programa (nombre, descripcion, genero, duracion)
  VALUES (p_nombre, p_descripcion, p_genero, p_duracion);
  SET p_id_programa = LAST_INSERT_ID();
END $$

CREATE PROCEDURE modificar_programa(
  IN p_id_programa INT, IN p_nombre VARCHAR(100), IN p_descripcion VARCHAR(500),
  IN p_genero VARCHAR(50), IN p_duracion INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM programa WHERE id_programa = p_id_programa) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El programa no existe';
  END IF;
  IF p_duracion IS NULL OR p_duracion <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La duracion del programa debe ser mayor a 0';
  END IF;
  UPDATE programa
  SET nombre = p_nombre, descripcion = p_descripcion, genero = p_genero, duracion = p_duracion
  WHERE id_programa = p_id_programa;
END $$

CREATE PROCEDURE eliminar_programa(IN p_id_programa INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM programa WHERE id_programa = p_id_programa) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El programa no existe';
  END IF;
  DELETE FROM programa WHERE id_programa = p_id_programa;
END $$

CREATE PROCEDURE buscar_programas_por_genero(IN p_genero VARCHAR(50))
BEGIN
  SELECT * FROM programa WHERE genero LIKE CONCAT('%', p_genero, '%') ORDER BY nombre;
END $$

CREATE PROCEDURE programar_programa(IN p_id_programa INT)
BEGIN
  DECLARE v_id_bloque INT DEFAULT NULL;
  SELECT id_bloque INTO v_id_bloque FROM bloque
  WHERE tipo_contenido = 'programa' AND id_referencia = p_id_programa;
  IF v_id_bloque IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El programa no existe';
  END IF;
  CALL agregar_bloque_a_grilla(v_id_bloque);
END $$

CREATE PROCEDURE programas_mas_largos(IN p_n INT)
BEGIN
  SELECT * FROM programa ORDER BY duracion DESC LIMIT p_n;
END $$

-- =====================================================================
-- 9. PROCEDIMIENTOS: VIVO
-- =====================================================================

CREATE PROCEDURE crear_vivo(
  IN p_titulo VARCHAR(100), IN p_descripcion VARCHAR(500), IN p_duracion INT,
  OUT p_id_envivo INT)
BEGIN
  IF p_duracion IS NULL OR p_duracion <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La duracion del vivo debe ser mayor a 0';
  END IF;
  INSERT INTO vivo (titulo, descripcion, duracion, retransmitido)
  VALUES (p_titulo, p_descripcion, p_duracion, FALSE);
  SET p_id_envivo = LAST_INSERT_ID();
END $$

CREATE PROCEDURE marcar_como_retransmitido(IN p_id_envivo INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM vivo WHERE id_envivo = p_id_envivo) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El vivo no existe';
  END IF;
  UPDATE vivo SET retransmitido = TRUE WHERE id_envivo = p_id_envivo;
END $$

CREATE PROCEDURE listar_vivos_pendientes()
BEGIN
  SELECT * FROM vivo WHERE retransmitido = FALSE ORDER BY id_envivo;
END $$

CREATE PROCEDURE modificar_vivo(
  IN p_id_envivo INT, IN p_titulo VARCHAR(100), IN p_descripcion VARCHAR(500), IN p_duracion INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM vivo WHERE id_envivo = p_id_envivo) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El vivo no existe';
  END IF;
  IF p_duracion IS NULL OR p_duracion <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La duracion del vivo debe ser mayor a 0';
  END IF;
  UPDATE vivo
  SET titulo = p_titulo, descripcion = p_descripcion, duracion = p_duracion
  WHERE id_envivo = p_id_envivo;
END $$

CREATE PROCEDURE eliminar_vivo(IN p_id_envivo INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM vivo WHERE id_envivo = p_id_envivo) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El vivo no existe';
  END IF;
  DELETE FROM vivo WHERE id_envivo = p_id_envivo;
END $$

CREATE PROCEDURE programar_vivo(IN p_id_envivo INT)
BEGIN
  DECLARE v_id_bloque INT DEFAULT NULL;
  SELECT id_bloque INTO v_id_bloque FROM bloque
  WHERE tipo_contenido = 'vivo' AND id_referencia = p_id_envivo;
  IF v_id_bloque IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El vivo no existe';
  END IF;
  CALL agregar_bloque_a_grilla(v_id_bloque);
END $$

-- =====================================================================
-- 10. PROCEDIMIENTOS: ROL
-- =====================================================================

CREATE PROCEDURE crear_rol(IN p_nombre_rol VARCHAR(50), OUT p_id_rol INT)
BEGIN
  IF p_nombre_rol IS NULL OR TRIM(p_nombre_rol) = '' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre del rol es obligatorio';
  END IF;
  IF EXISTS (SELECT 1 FROM rol WHERE nombre_rol = p_nombre_rol) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ya existe un rol con ese nombre';
  END IF;
  INSERT INTO rol (nombre_rol) VALUES (TRIM(p_nombre_rol));
  SET p_id_rol = LAST_INSERT_ID();
END $$

CREATE PROCEDURE renombrar_rol(IN p_id_rol INT, IN p_nombre_rol VARCHAR(50))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM rol WHERE id_rol = p_id_rol) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rol no existe';
  END IF;
  IF EXISTS (SELECT 1 FROM rol WHERE nombre_rol = p_nombre_rol AND id_rol <> p_id_rol) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ya existe un rol con ese nombre';
  END IF;
  UPDATE rol SET nombre_rol = TRIM(p_nombre_rol) WHERE id_rol = p_id_rol;
END $$

-- Los usuarios del rol eliminado pasan a "usuario" (id 7)
CREATE PROCEDURE eliminar_rol(IN p_id_rol INT)
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  IF NOT EXISTS (SELECT 1 FROM rol WHERE id_rol = p_id_rol) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rol no existe';
  END IF;
  IF p_id_rol IN (1, 7) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Los roles Administrador General y usuario no se pueden eliminar';
  END IF;

  START TRANSACTION;
  UPDATE usuario SET id_rol = 7 WHERE id_rol = p_id_rol;
  DELETE FROM rol WHERE id_rol = p_id_rol;
  COMMIT;
END $$

CREATE PROCEDURE listar_usuarios_por_rol(IN p_id_rol INT)
BEGIN
  SELECT id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol
  FROM usuario WHERE id_rol = p_id_rol ORDER BY apellido, nombre;
END $$

CREATE PROCEDURE contar_usuarios_por_rol()
BEGIN
  SELECT r.id_rol, r.nombre_rol, COUNT(u.id_usuario) AS cantidad_usuarios
  FROM rol r
  LEFT JOIN usuario u ON u.id_rol = r.id_rol
  GROUP BY r.id_rol, r.nombre_rol
  ORDER BY r.id_rol;
END $$

-- =====================================================================
-- 11. PROCEDIMIENTOS: USUARIO
-- =====================================================================

CREATE PROCEDURE registrar_usuario(
  IN p_nombre_usuario VARCHAR(50), IN p_nombre VARCHAR(50), IN p_apellido VARCHAR(50),
  IN p_email VARCHAR(100), IN p_contrasena VARCHAR(255))
BEGIN
  INSERT INTO usuario (nombre_usuario, nombre, apellido, email, contrasena)
  VALUES (p_nombre_usuario, p_nombre, p_apellido, p_email, p_contrasena);
END $$

CREATE PROCEDURE asignar_rol_usuario(IN p_id_usuario INT, IN p_id_rol INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario no existe';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM rol WHERE id_rol = p_id_rol) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rol no existe';
  END IF;
  UPDATE usuario SET id_rol = p_id_rol WHERE id_usuario = p_id_usuario;
END $$

CREATE PROCEDURE eliminar_usuario(IN p_id_usuario INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario no existe';
  END IF;
  DELETE FROM usuario WHERE id_usuario = p_id_usuario;
END $$

-- Devuelve el usuario (sin contrasena) si las credenciales son correctas
CREATE PROCEDURE login_usuario(IN p_nombre_usuario VARCHAR(50), IN p_contrasena VARCHAR(255))
BEGIN
  SELECT u.id_usuario, u.nombre_usuario, u.nombre, u.apellido, u.email,
         u.id_rol, r.nombre_rol
  FROM usuario u
  JOIN rol r ON r.id_rol = u.id_rol
  WHERE u.nombre_usuario = p_nombre_usuario AND u.contrasena = p_contrasena;
END $$

CREATE PROCEDURE cambiar_contrasena(
  IN p_id_usuario INT, IN p_vieja VARCHAR(255), IN p_nueva VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario AND contrasena = p_vieja) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La contrasena actual es incorrecta';
  END IF;
  IF p_nueva IS NULL OR CHAR_LENGTH(p_nueva) < 1 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La contrasena nueva no puede estar vacia';
  END IF;
  UPDATE usuario SET contrasena = p_nueva WHERE id_usuario = p_id_usuario;
END $$

CREATE PROCEDURE modificar_datos_usuario(
  IN p_id_usuario INT, IN p_nombre VARCHAR(50), IN p_apellido VARCHAR(50), IN p_email VARCHAR(100))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario no existe';
  END IF;
  UPDATE usuario SET nombre = p_nombre, apellido = p_apellido, email = p_email
  WHERE id_usuario = p_id_usuario;
END $$

CREATE PROCEDURE buscar_usuario(IN p_texto VARCHAR(100))
BEGIN
  SELECT id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol
  FROM usuario
  WHERE nombre_usuario LIKE CONCAT('%', p_texto, '%')
     OR nombre LIKE CONCAT('%', p_texto, '%')
     OR apellido LIKE CONCAT('%', p_texto, '%')
     OR email LIKE CONCAT('%', p_texto, '%')
  ORDER BY apellido, nombre;
END $$

CREATE PROCEDURE listar_usuarios_recientes(IN p_dias INT)
BEGIN
  SELECT id_usuario, nombre_usuario, nombre, apellido, email, fecha_registro, id_rol
  FROM usuario
  WHERE fecha_registro >= DATE_SUB(NOW(), INTERVAL p_dias DAY)
  ORDER BY fecha_registro DESC;
END $$

CREATE PROCEDURE degradar_a_usuario(IN p_id_usuario INT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM usuario WHERE id_usuario = p_id_usuario) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El usuario no existe';
  END IF;
  UPDATE usuario SET id_rol = 7 WHERE id_usuario = p_id_usuario;
END $$

CREATE PROCEDURE ver_log_cambios_rol(IN p_limite INT)
BEGIN
  SELECT l.id_log, l.id_usuario, u.nombre_usuario, l.rol_anterior, l.rol_nuevo, l.fecha
  FROM log_cambios_rol l
  LEFT JOIN usuario u ON u.id_usuario = l.id_usuario
  ORDER BY l.id_log DESC
  LIMIT p_limite;
END $$

-- =====================================================================
-- 12. PROCEDIMIENTOS: BLOQUE
-- =====================================================================

CREATE PROCEDURE obtener_bloque(
  IN p_tipo VARCHAR(20), IN p_id_referencia INT, OUT p_id_bloque INT)
BEGIN
  SET p_id_bloque = NULL;
  SELECT id_bloque INTO p_id_bloque FROM bloque
  WHERE tipo_contenido = p_tipo AND id_referencia = p_id_referencia;
END $$

CREATE PROCEDURE listar_bloques_por_tipo(IN p_tipo VARCHAR(20))
BEGIN
  SELECT * FROM vista_bloques WHERE tipo_contenido = p_tipo ORDER BY titulo;
END $$

CREATE PROCEDURE listar_todos_los_bloques()
BEGIN
  SELECT * FROM vista_bloques ORDER BY tipo_contenido, titulo;
END $$

CREATE PROCEDURE listar_bloques_no_usados()
BEGIN
  SELECT vb.* FROM vista_bloques vb
  WHERE NOT EXISTS (SELECT 1 FROM grilla g WHERE g.id_bloque = vb.id_bloque)
  ORDER BY vb.tipo_contenido, vb.titulo;
END $$

-- Vuelve a leer la duracion desde el contenido original
CREATE PROCEDURE recalcular_duracion_bloque(IN p_id_bloque INT)
BEGIN
  DECLARE v_tipo VARCHAR(20) DEFAULT NULL;
  DECLARE v_ref INT DEFAULT NULL;
  DECLARE v_dur INT DEFAULT NULL;

  SELECT tipo_contenido, id_referencia INTO v_tipo, v_ref
  FROM bloque WHERE id_bloque = p_id_bloque;

  IF v_tipo IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El bloque no existe';
  END IF;

  IF v_tipo = 'programa' THEN
    SELECT duracion INTO v_dur FROM programa WHERE id_programa = v_ref;
  ELSEIF v_tipo = 'vivo' THEN
    SELECT duracion INTO v_dur FROM vivo WHERE id_envivo = v_ref;
  ELSE
    SELECT duracion INTO v_dur FROM anuncio WHERE id_anuncio = v_ref;
  END IF;

  CALL sp_sync_duracion_bloque(p_id_bloque, IFNULL(v_dur, 0));
END $$

CREATE PROCEDURE duracion_total_por_tipo()
BEGIN
  SELECT tipo_contenido, COUNT(*) AS cantidad_bloques, IFNULL(SUM(duracion), 0) AS segundos_totales
  FROM bloque
  GROUP BY tipo_contenido
  ORDER BY tipo_contenido;
END $$

-- Borra bloques cuyo contenido original ya no existe
CREATE PROCEDURE limpiar_bloques_huerfanos()
BEGIN
  DELETE b FROM bloque b
  WHERE (b.tipo_contenido = 'programa'
         AND NOT EXISTS (SELECT 1 FROM programa p WHERE p.id_programa = b.id_referencia))
     OR (b.tipo_contenido = 'vivo'
         AND NOT EXISTS (SELECT 1 FROM vivo v WHERE v.id_envivo = b.id_referencia))
     OR (b.tipo_contenido = 'anuncio'
         AND NOT EXISTS (SELECT 1 FROM anuncio a WHERE a.id_anuncio = b.id_referencia));
  SELECT ROW_COUNT() AS bloques_eliminados;
END $$

-- =====================================================================
-- 13. PROCEDIMIENTOS: GRILLA (todos con transaccion y bloqueo)
--     "posicion" = valor de la columna orden. La posicion 1 esta al aire
--     y no se puede mover, intercambiar ni reemplazar.
-- =====================================================================

-- Agrega un bloque al final de la cola
CREATE PROCEDURE agregar_bloque_a_grilla(IN p_id_bloque INT)
BEGIN
  DECLARE v_dur INT DEFAULT NULL;
  DECLARE v_n INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;

  SELECT duracion INTO v_dur FROM bloque WHERE id_bloque = p_id_bloque;
  IF v_dur IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El bloque indicado no existe';
  END IF;

  CALL sp_compactar_orden();
  INSERT INTO grilla (id_bloque, orden, duracion) VALUES (p_id_bloque, v_n + 1, v_dur);
  CALL sp_arrancar_primero();
  COMMIT;
END $$

-- Termina el bloque actual y sube el siguiente (original)
CREATE PROCEDURE avanzar_grilla()
BEGIN
  CALL sp_avanzar_interno(1);
END $$

-- Lo llama el evento cada segundo: avanza solo si el bloque vencio
CREATE PROCEDURE tick_grilla()
BEGIN
  DECLARE v_id INT DEFAULT NULL;
  DECLARE v_inicio DATETIME DEFAULT NULL;
  DECLARE v_fin DATETIME DEFAULT NULL;

  SELECT id_posicion, hora_inicio, hora_fin INTO v_id, v_inicio, v_fin
  FROM grilla
  ORDER BY orden ASC, id_posicion ASC
  LIMIT 1;

  IF v_id IS NOT NULL AND (v_inicio IS NULL OR v_fin <= NOW()) THEN
    CALL sp_avanzar_interno(0);
  END IF;
END $$

CREATE PROCEDURE saltar_bloque_actual()
BEGIN
  CALL sp_avanzar_interno(1);
END $$

-- TRANSACCION: inserta un bloque en una posicion (corre los demas)
CREATE PROCEDURE insertar_bloque_en_posicion(
  IN p_id_bloque INT, IN p_posicion INT, OUT p_id_posicion INT)
BEGIN
  DECLARE v_dur INT DEFAULT NULL;
  DECLARE v_n INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  CALL sp_compactar_orden();

  SELECT duracion INTO v_dur FROM bloque WHERE id_bloque = p_id_bloque;
  IF v_dur IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El bloque indicado no existe';
  END IF;
  IF p_posicion IS NULL OR p_posicion < 1 OR p_posicion > v_n + 1 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Posicion fuera de rango';
  END IF;
  IF p_posicion = 1 AND v_n > 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La posicion 1 esta al aire: no se puede insertar ahi';
  END IF;

  UPDATE grilla SET orden = orden + 1 WHERE orden >= p_posicion;
  INSERT INTO grilla (id_bloque, orden, duracion) VALUES (p_id_bloque, p_posicion, v_dur);
  SET p_id_posicion = LAST_INSERT_ID();
  CALL sp_arrancar_primero();
  COMMIT;
END $$

-- TRANSACCION: intercambia dos posiciones de la cola
CREATE PROCEDURE intercambiar_posiciones(IN p_pos_a INT, IN p_pos_b INT)
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  CALL sp_compactar_orden();

  IF p_pos_a IS NULL OR p_pos_b IS NULL OR p_pos_a = p_pos_b THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Las dos posiciones deben ser distintas';
  END IF;
  IF p_pos_a < 1 OR p_pos_b < 1 OR p_pos_a > v_n OR p_pos_b > v_n THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Posicion fuera de rango';
  END IF;
  IF p_pos_a = 1 OR p_pos_b = 1 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La posicion 1 esta al aire: no se puede intercambiar';
  END IF;

  UPDATE grilla
  SET orden = IF(orden = p_pos_a, p_pos_b, p_pos_a)
  WHERE orden IN (p_pos_a, p_pos_b);
  COMMIT;
END $$

-- TRANSACCION: mueve un bloque a otra posicion (corre los del medio)
CREATE PROCEDURE mover_bloque(IN p_pos_origen INT, IN p_pos_destino INT)
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  CALL sp_compactar_orden();

  IF p_pos_origen IS NULL OR p_pos_destino IS NULL OR p_pos_origen = p_pos_destino THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Origen y destino deben ser distintos';
  END IF;
  IF p_pos_origen < 1 OR p_pos_destino < 1 OR p_pos_origen > v_n OR p_pos_destino > v_n THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Posicion fuera de rango';
  END IF;
  IF p_pos_origen = 1 OR p_pos_destino = 1 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La posicion 1 esta al aire: no se puede mover';
  END IF;

  IF p_pos_origen < p_pos_destino THEN
    UPDATE grilla
    SET orden = IF(orden = p_pos_origen, p_pos_destino, orden - 1)
    WHERE orden BETWEEN p_pos_origen AND p_pos_destino;
  ELSE
    UPDATE grilla
    SET orden = IF(orden = p_pos_origen, p_pos_destino, orden + 1)
    WHERE orden BETWEEN p_pos_destino AND p_pos_origen;
  END IF;
  COMMIT;
END $$

-- TRANSACCION: cambia el contenido de una posicion sin alterar el orden
CREATE PROCEDURE reemplazar_bloque(IN p_posicion INT, IN p_id_bloque_nuevo INT)
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE v_dur INT DEFAULT NULL;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  CALL sp_compactar_orden();

  SELECT duracion INTO v_dur FROM bloque WHERE id_bloque = p_id_bloque_nuevo;
  IF v_dur IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El bloque indicado no existe';
  END IF;
  IF v_dur <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede programar un bloque con duracion 0';
  END IF;
  IF p_posicion IS NULL OR p_posicion < 1 OR p_posicion > v_n THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Posicion fuera de rango';
  END IF;
  IF p_posicion = 1 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La posicion 1 esta al aire: no se puede reemplazar';
  END IF;

  UPDATE grilla SET id_bloque = p_id_bloque_nuevo, duracion = v_dur WHERE orden = p_posicion;
  COMMIT;
END $$

-- TRANSACCION: repite un bloque justo despues de su posicion
CREATE PROCEDURE duplicar_posicion(IN p_posicion INT)
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE v_id_bloque INT DEFAULT NULL;
  DECLARE v_dur INT DEFAULT NULL;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  CALL sp_compactar_orden();

  IF p_posicion IS NULL OR p_posicion < 1 OR p_posicion > v_n THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Posicion fuera de rango';
  END IF;

  SELECT id_bloque, duracion INTO v_id_bloque, v_dur FROM grilla WHERE orden = p_posicion;

  UPDATE grilla SET orden = orden + 1 WHERE orden > p_posicion;
  INSERT INTO grilla (id_bloque, orden, duracion) VALUES (v_id_bloque, p_posicion + 1, v_dur);
  COMMIT;
END $$

-- TRANSACCION: quita una posicion y compacta. Si es la 1, equivale a terminar el bloque al aire.
CREATE PROCEDURE quitar_de_grilla(IN p_posicion INT)
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  CALL sp_compactar_orden();

  IF p_posicion IS NULL OR p_posicion < 1 OR p_posicion > v_n THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Posicion fuera de rango';
  END IF;

  DELETE FROM grilla WHERE orden = p_posicion;
  CALL sp_compactar_orden();
  CALL sp_arrancar_primero();
  COMMIT;
END $$

CREATE PROCEDURE vaciar_grilla()
BEGIN
  DECLARE v_n INT DEFAULT 0;
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;
  SELECT COUNT(*) INTO v_n FROM grilla FOR UPDATE;
  DELETE FROM grilla;
  COMMIT;
END $$

-- Lectura: grilla con horarios estimados de cada posicion
CREATE PROCEDURE ver_grilla_con_horarios_proyectados()
BEGIN
  SELECT
    t.orden, t.id_posicion, t.id_bloque, t.tipo_contenido, t.titulo,
    t.duracion_segundos,
    DATE_ADD(t.base, INTERVAL t.acumulado_previo SECOND) AS inicio_proyectado,
    DATE_ADD(t.base, INTERVAL (t.acumulado_previo + t.duracion_segundos) SECOND) AS fin_proyectado
  FROM (
    SELECT
      v.orden, v.id_posicion, v.id_bloque, v.tipo_contenido, v.titulo, v.duracion_segundos,
      (SELECT IFNULL(SUM(g2.duracion), 0) FROM grilla g2
        WHERE g2.orden < v.orden
           OR (g2.orden = v.orden AND g2.id_posicion < v.id_posicion)) AS acumulado_previo,
      COALESCE(
        (SELECT g3.hora_inicio FROM grilla g3 ORDER BY g3.orden, g3.id_posicion LIMIT 1), NOW()) AS base
    FROM vista_grilla_activa v
  ) t
  ORDER BY t.orden, t.id_posicion;
END $$

CREATE PROCEDURE duracion_total_grilla()
BEGIN
  SELECT
    COUNT(*) AS cantidad_bloques,
    IFNULL(SUM(duracion), 0) AS segundos_totales,
    GREATEST(IFNULL(SUM(duracion), 0)
      - IFNULL((SELECT TIMESTAMPDIFF(SECOND, hora_inicio, NOW()) FROM grilla
                WHERE hora_inicio IS NOT NULL ORDER BY orden, id_posicion LIMIT 1), 0), 0)
      AS segundos_restantes
  FROM grilla;
END $$

CREATE PROCEDURE bloque_en_aire()
BEGIN
  SELECT
    orden, id_posicion, id_bloque, tipo_contenido, titulo, duracion_segundos,
    hora_inicio, hora_fin,
    GREATEST(TIMESTAMPDIFF(SECOND, NOW(), hora_fin), 0) AS segundos_restantes
  FROM vista_grilla_activa
  ORDER BY orden, id_posicion
  LIMIT 1;
END $$

CREATE PROCEDURE ver_historial_emision(IN p_limite INT)
BEGIN
  SELECT * FROM historial_emision ORDER BY id_historial DESC LIMIT p_limite;
END $$

DELIMITER ;


-- =====================================================================
-- 13b. LISTADOS GENERALES (listados completos)
-- =====================================================================
DELIMITER $$

CREATE PROCEDURE listar_programas()
BEGIN
  SELECT * FROM programa ORDER BY nombre;
END $$

CREATE PROCEDURE listar_anuncios()
BEGIN
  SELECT * FROM anuncio ORDER BY nombre;
END $$

CREATE PROCEDURE listar_vivos()
BEGIN
  SELECT * FROM vivo ORDER BY id_envivo DESC;
END $$

CREATE PROCEDURE listar_roles()
BEGIN
  SELECT * FROM rol ORDER BY id_rol;
END $$

CREATE PROCEDURE listar_usuarios()
BEGIN
  SELECT u.id_usuario, u.nombre_usuario, u.nombre, u.apellido, u.email,
         u.fecha_registro, u.id_rol, r.nombre_rol
  FROM usuario u JOIN rol r ON r.id_rol = u.id_rol
  ORDER BY u.id_usuario;
END $$

DELIMITER ;

-- =====================================================================
-- 14. EVENTO: revisa la grilla cada segundo
-- =====================================================================
DELIMITER $$

DROP EVENT IF EXISTS evt_limpiar_y_avanzar_grilla $$

CREATE EVENT evt_limpiar_y_avanzar_grilla
ON SCHEDULE EVERY 1 SECOND
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  CALL tick_grilla();
END $$

DELIMITER ;
