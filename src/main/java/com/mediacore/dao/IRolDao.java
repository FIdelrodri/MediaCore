package com.mediacore.dao;

import com.mediacore.modelo.Rol;
import com.mediacore.modelo.RolConteo;
import com.mediacore.modelo.Usuario;

import java.util.List;

// La tabla rol es fija: solo se consulta
public interface IRolDao {

    List<Rol> listar() throws DaoException;

    boolean existe(int idRol) throws DaoException;

    List<Usuario> listarUsuariosPorRol(int idRol) throws DaoException;

    List<RolConteo> contarUsuariosPorRol() throws DaoException;
}
