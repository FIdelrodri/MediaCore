package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.Programa;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProgramaDaoImpl extends BaseDao implements IProgramaDao {

    @Override
    public int crear(Programa programa) throws DaoException {
        String sql = "{CALL crear_programa(?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, programa.getNombre());
            cs.setString(2, programa.getDescripcion());
            cs.setString(3, programa.getGenero());
            cs.setInt(4, programa.getDuracion());
            cs.registerOutParameter(5, Types.INTEGER); // p_id_programa
            cs.execute();

            return cs.getInt(5);

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void modificar(Programa programa) throws DaoException {
        String sql = "{CALL modificar_programa(?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, programa.getIdPrograma());
            cs.setString(2, programa.getNombre());
            cs.setString(3, programa.getDescripcion());
            cs.setString(4, programa.getGenero());
            cs.setInt(5, programa.getDuracion());
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void eliminar(int idPrograma) throws DaoException {
        ejecutar("{CALL eliminar_programa(?)}", idPrograma);
    }

    @Override
    public void programar(int idPrograma) throws DaoException {
        ejecutar("{CALL programar_programa(?)}", idPrograma);
    }

    @Override
    public List<Programa> listar() throws DaoException {
        List<Programa> programas = new ArrayList<>();
        String sql = "{CALL listar_programas()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                programas.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return programas;
    }

    @Override
    public List<Programa> buscarPorGenero(String genero) throws DaoException {
        List<Programa> programas = new ArrayList<>();
        String sql = "{CALL buscar_programas_por_genero(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, genero == null ? "" : genero.trim());

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    programas.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return programas;
    }

    @Override
    public List<Programa> masLargos(int cantidad) throws DaoException {
        List<Programa> programas = new ArrayList<>();
        String sql = "{CALL programas_mas_largos(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, cantidad);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    programas.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return programas;
    }

    // ---- metodos privados de apoyo ----

    private void ejecutar(String sql, int id) throws DaoException {
        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    private Programa mapear(ResultSet rs) throws SQLException {
        Programa p = new Programa();
        p.setIdPrograma(rs.getInt("id_programa"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setGenero(rs.getString("genero"));
        p.setDuracion(rs.getInt("duracion"));
        return p;
    }
}
