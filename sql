DROP DATABASE IF EXISTS `mediacore`;
CREATE DATABASE `mediacore` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mediacore`;


CREATE TABLE `anuncio` (
  `id_anuncio` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `descripcion` TEXT DEFAULT NULL,
  `genero` VARCHAR(50) DEFAULT NULL,
  `duracion` INT(11) DEFAULT NULL,
  `empresa` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id_anuncio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `programa` (
  `id_programa` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `descripcion` VARCHAR(500) DEFAULT NULL,
  `genero` VARCHAR(50) DEFAULT NULL,
  `duracion` INT(11) DEFAULT NULL,
  PRIMARY KEY (`id_programa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `permiso` (
  `id_permiso` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL,
  `descripcion` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id_permiso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `rol` (
  `id_rol` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre_rol` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `usuario` (
  `id_usuario` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL,
  `apellido` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `contrasena` VARCHAR(255) NOT NULL,
  `fecha_registro` DATETIME DEFAULT CURRENT_TIMESTAMP(),
  `id_rol` INT(11) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `grilla` (
  `id_grilla` INT(11) NOT NULL AUTO_INCREMENT,
  `fecha` DATE NOT NULL,
  `id_usuario` INT(11) DEFAULT NULL,
  PRIMARY KEY (`id_grilla`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `fk_grilla_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `bloque_anuncio` (
  `id_bloque_anuncio` INT(11) NOT NULL AUTO_INCREMENT,
  `id_anuncio` INT(11) NOT NULL,
  `id_grilla` INT(11) NOT NULL,
  `hora_inicio` TIME NOT NULL,
  `hora_fin` TIME NOT NULL,
  `posicion` INT(11) NOT NULL,
  PRIMARY KEY (`id_bloque_anuncio`),
  KEY `id_anuncio` (`id_anuncio`),
  KEY `id_grilla` (`id_grilla`),
  CONSTRAINT `bloque_anuncio_ibfk_1` FOREIGN KEY (`id_anuncio`) REFERENCES `anuncio` (`id_anuncio`) ON DELETE CASCADE,
  CONSTRAINT `bloque_anuncio_ibfk_2` FOREIGN KEY (`id_grilla`) REFERENCES `grilla` (`id_grilla`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `bloque_grilla` (
  `id_bloque` INT(11) NOT NULL AUTO_INCREMENT,
  `id_grilla` INT(11) NOT NULL,
  `id_programa` INT(11) NOT NULL,
  `hora_inicio` TIME NOT NULL,
  `hora_fin` TIME NOT NULL,
  `posicion` INT(11) NOT NULL,
  `tipo` VARCHAR(30) DEFAULT 'PROGRAMA',
  PRIMARY KEY (`id_bloque`),
  KEY `id_grilla` (`id_grilla`),
  KEY `id_programa` (`id_programa`),
  CONSTRAINT `bloque_grilla_ibfk_1` FOREIGN KEY (`id_grilla`) REFERENCES `grilla` (`id_grilla`) ON DELETE CASCADE,
  CONSTRAINT `bloque_grilla_ibfk_2` FOREIGN KEY (`id_programa`) REFERENCES `programa` (`id_programa`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `rel_rol_permisos` (
  `id_rol_permiso` INT(11) NOT NULL AUTO_INCREMENT,
  `id_rol` INT(11) NOT NULL,
  `id_permiso` INT(11) NOT NULL,
  PRIMARY KEY (`id_rol_permiso`),
  KEY `id_rol` (`id_rol`),
  KEY `id_permiso` (`id_permiso`),
  CONSTRAINT `fk_rel_permiso` FOREIGN KEY (`id_permiso`) REFERENCES `permiso` (`id_permiso`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rel_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `rel_rol_usuario` (
  `id_usuario` INT(11) NOT NULL,
  `id_rol` INT(11) NOT NULL,
  PRIMARY KEY (`id_usuario`, `id_rol`),
  KEY `id_rol` (`id_rol`),
  CONSTRAINT `rel_rol_usuario_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `rel_rol_usuario_ibfk_2` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DELIMITER //

CREATE PROCEDURE registrar_usuario(
    IN p_nombre VARCHAR(50),
    IN p_apellido VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_contrasena VARCHAR(255)
)
BEGIN
    INSERT INTO usuario (nombre, apellido, email, contrasena)
    VALUES (p_nombre, p_apellido, p_email, p_contrasena);
END //

DELIMITER ;