DROP DATABASE IF EXISTS mediacore;
CREATE DATABASE mediacore;
USE mediacore;
SET GLOBAL event_scheduler = ON;

-- 1. TABLAS PRINCIPALES DE CONTENIDO

create TABLE anuncio (
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

create TABLE vivo (
  id_envivo INT(11) NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(20) NOT NULL,
  descripcion VARCHAR(500) NOT NULL,
  duracion INT(20) NOT NULL,
  retrasmitido BOOLEAN NOT NULL,
  PRIMARY KEY (id_envivo)
);

-- 2. ROLES Y USUARIOS

CREATE TABLE rol (
  id_rol INT(11) NOT NULL AUTO_INCREMENT,
  nombre_rol VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_rol)
);

INSERT INTO rol (nombre_rol) VALUES
('Administrador General'),
('Administrador'),
('escenografo'),
('administrador de audio'),
('Administrador de planilla'),
('personal de postproduccion'),
('usuario');

create TABLE usuario (
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

insert into usuario (nombre_usuario, nombre, apellido, email, contrasena, id_rol) values
('a', 'Administrador', 'General', 'admin@mediacore.com', 'a', 1);

-- 3. ESTRUCTURA DE BLOQUE Y GRILLA DINÁMICA

CREATE TABLE bloque (
  id_bloque INT(11) NOT NULL AUTO_INCREMENT,
  tipo_contenido ENUM('programa', 'vivo', 'anuncio') NOT NULL,
  id_referencia INT(11) NOT NULL,
  duracion INT(11) NOT NULL,
  PRIMARY KEY (id_bloque)
);

create TABLE grilla (
  id_posicion INT(11) NOT NULL AUTO_INCREMENT,
  id_bloque INT(11) NOT NULL,
  duracion INT(11) NOT NULL,
  hora_inicio DATETIME DEFAULT NULL,
  hora_fin DATETIME DEFAULT NULL,
  PRIMARY KEY (id_posicion),
  CONSTRAINT fk_grilla_bloque FOREIGN KEY (id_bloque) REFERENCES bloque (id_bloque) on DELETE CASCADE
);

-- 4. TRIGGERS AUTOMÁTICOS

DELIMITER //

-- Copiar contenido y su duración a la tabla bloque al insertar un Programa
CREATE TRIGGER after_insert_programa
AFTER INSERT ON programa
FOR EACH ROW
BEGIN
  insert INTO bloque (tipo_contenido, id_referencia, duracion)
  VALUES ('programa', NEW.id_programa, IFNULL(NEW.duracion, 0));
END //

-- Copiar contenido y su duración a la tabla bloque al insertar un Vivo
create TRIGGER after_insert_vivo
AFTER INSERT on vivo
FOR EACH ROW
begin
  insert into bloque (tipo_contenido, id_referencia, duracion)
  values ('vivo', NEW.id_envivo, IFNULL(NEW.duracion, 0));
end //

-- Copiar contenido y su duración a la tabla bloque al insertar un Anuncio
CREATE TRIGGER after_insert_anuncio
AFTER INSERT ON anuncio
FOR EACH ROW
BEGIN
  insert INTO bloque (tipo_contenido, id_referencia, duracion)
  VALUES ('anuncio', NEW.id_anuncio, IFNULL(NEW.duracion, 0));
END //

DELIMITER ;

-- 5. PROCEDIMIENTOS ALMACENADOS

DELIMITER $$

-- --- Procedimientos Originales de Usuario ---

create PROCEDURE registrar_usuario(
   IN p_nombre_usuario VARCHAR(50),
   IN p_nombre VARCHAR(50),
   IN p_apellido VARCHAR(50),
   IN p_email VARCHAR(100),
   IN p_contrasena VARCHAR(255)
)
begin
    INSERT into usuario (nombre_usuario, nombre, apellido, email, contrasena)
    values (p_nombre_usuario, p_nombre, p_apellido, p_email, p_contrasena);
end $$

CREATE PROCEDURE asignar_rol_usuario(
    IN p_id_usuario INT,
    IN p_id_rol INT
)
BEGIN
    UPDATE usuario set id_rol = p_id_rol WHERE id_usuario = p_id_usuario;
END $$

create PROCEDURE eliminar_usuario(
    IN p_id_usuario INT
)
begin
    delete FROM usuario where id_usuario = p_id_usuario;
end $$

-- --- Procedimientos de la Grilla Dinámica ---

-- Procedimiento para agregar un bloque al final de la cola de la grilla
CREATE PROCEDURE agregar_bloque_a_grilla(
    IN p_id_bloque INT
)
BEGIN
    DECLARE v_duracion INT;

    -- Obtener la duración guardada en el bloque
    SELECT duracion INTO v_duracion from bloque WHERE id_bloque = p_id_bloque;

    -- Insertar en la grilla. Si es el primer elemento (posición 1), inicia inmediatamente
    insert into grilla (id_bloque, duracion)
    VALUES (p_id_bloque, v_duracion);

    -- Si es la primera posición de la grilla, arrancar tiempos automáticamente
    if (select COUNT(*) FROM grilla) = 1 THEN
        update grilla
        SET hora_inicio = NOW(),
            hora_fin = DATE_ADD(NOW(), INTERVAL v_duracion SECOND)
        ORDER BY id_posicion ASC
        LIMIT 1;
    END IF;
end $$

-- Procedimiento para finalizar el bloque actual y subir el siguiente a la posición 1
create PROCEDURE avanzar_grilla()
begin
    declare v_id_posicion_siguiente INT;
    DECLARE v_duracion_siguiente INT;

    -- 1. Eliminar el bloque que acaba de terminar (el primero en la cola)
    DELETE from grilla
    order by id_posicion ASC
    limit 1;

    -- 2. Obtener el nuevo bloque que pasa al frente
    SELECT id_posicion, duracion
    INTO v_id_posicion_siguiente, v_duracion_siguiente
    FROM grilla
    ORDER BY id_posicion ASC
    LIMIT 1;

    -- 3. Si hay un bloque disponible, setear su tiempo de inicio al momento actual y calcular su fin
    if v_id_posicion_siguiente IS NOT NULL then
        UPDATE grilla
        set hora_inicio = NOW(),
            hora_fin = DATE_ADD(NOW(), INTERVAL v_duracion_siguiente SECOND)
        where id_posicion = v_id_posicion_siguiente;
    END IF;
end $$

DELIMITER ;

-- 6. VISTA PARA LEER LA GRILLA EN TIEMPO REAL

CREATE VIEW vista_grilla_activa AS
select
  g.id_posicion,
  g.id_bloque,
  b.tipo_contenido,
  COALESCE(p.nombre, v.titulo, a.nombre) as titulo,
  g.duracion AS duracion_segundos,
  g.hora_inicio,
  g.hora_fin
from grilla g
INNER JOIN bloque b on g.id_bloque = b.id_bloque
LEFT JOIN programa p ON b.tipo_contenido = 'programa' AND b.id_referencia = p.id_programa
left join vivo v on b.tipo_contenido = 'vivo' AND b.id_referencia = v.id_envivo
LEFT JOIN anuncio a ON b.tipo_contenido = 'anuncio' AND b.id_referencia = a.id_anuncio
order by g.id_posicion ASC;

DELIMITER //

drop EVENT if EXISTS evt_limpiar_y_avanzar_grilla //

create EVENT evt_limpiar_y_avanzar_grilla
on SCHEDULE EVERY 1 SECOND
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  declare v_hora_fin DATETIME;

  -- Obtener la hora_fin del bloque en la primera posición
  SELECT hora_fin into v_hora_fin
  FROM grilla
  ORDER BY id_posicion ASC
  limit 1;

  -- Si existe un bloque activo y su tiempo ya venció, avanzar la grilla
  IF v_hora_fin IS NOT NULL AND v_hora_fin <= NOW() THEN
    CALL avanzar_grilla();
  END if;
end //

DELIMITER ;
