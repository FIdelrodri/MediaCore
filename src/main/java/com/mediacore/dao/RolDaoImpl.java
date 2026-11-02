package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.Rol;
import com.mediacore.modelo.RolConteo;
import com.mediacore.modelo.Usuario;
import com.mediacore.util.Fechas;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDaoImpl extends BaseDao implements IRolDao {

    @Override
    public List<Rol> listar() throws DaoException {
        List<Rol> roles = new ArrayList<>();
        String sql = "{CALL listar_roles()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                roles.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre_rol")));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return roles;
    }

    @Override
    public boolean existe(int idRol) throws DaoException {
        for (Rol rol : listar()) {
            if (rol.getIdRol() == idRol) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Usuario> listarUsuariosPorRol(int idRol) throws DaoException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "{CALL listar_usuarios_por_rol(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idRol);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setEmail(rs.getString("email"));
                    u.setFechaRegistro(Fechas.iso(rs.getString("fecha_registro")));
                    u.setIdRol(rs.getInt("id_rol"));
                    usuarios.add(u);
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return usuarios;
    }

    @Override
    public List<RolConteo> contarUsuariosPorRol() throws DaoException {
        List<RolConteo> conteo = new ArrayList<>();
        String sql = "{CALL contar_usuarios_por_rol()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                conteo.add(new RolConteo(rs.getInt("id_rol"), rs.getString("nombre_rol"),
                        rs.getLong("cantidad_usuarios")));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return conteo;
    }
}
