package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.Anuncio;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AnuncioDaoImpl extends BaseDao implements IAnuncioDao {

    @Override
    public int crear(Anuncio anuncio) throws DaoException {
        String sql = "{CALL crear_anuncio(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, anuncio.getNombre());
            cs.setString(2, anuncio.getDescripcion());
            cs.setString(3, anuncio.getGenero());
            cs.setInt(4, anuncio.getDuracion());
            cs.setString(5, anuncio.getEmpresa());
            cs.registerOutParameter(6, Types.INTEGER); // p_id_anuncio
            cs.execute();

            return cs.getInt(6);

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void modificar(Anuncio anuncio) throws DaoException {
        String sql = "{CALL modificar_anuncio(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, anuncio.getIdAnuncio());
            cs.setString(2, anuncio.getNombre());
            cs.setString(3, anuncio.getDescripcion());
            cs.setString(4, anuncio.getGenero());
            cs.setInt(5, anuncio.getDuracion());
            cs.setString(6, anuncio.getEmpresa());
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void eliminar(int idAnuncio) throws DaoException {
        ejecutar("{CALL eliminar_anuncio(?)}", idAnuncio);
    }

    @Override
    public void programar(int idAnuncio) throws DaoException {
        ejecutar("{CALL insertar_anuncio_en_grilla(?)}", idAnuncio);
    }

    @Override
    public List<Anuncio> listar() throws DaoException {
        List<Anuncio> anuncios = new ArrayList<>();
        String sql = "{CALL listar_anuncios()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                anuncios.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return anuncios;
    }

    @Override
    public List<Anuncio> listarPorEmpresa(String empresa) throws DaoException {
        List<Anuncio> anuncios = new ArrayList<>();
        String sql = "{CALL listar_anuncios_por_empresa(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, empresa == null ? "" : empresa.trim());

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    anuncios.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return anuncios;
    }

    @Override
    public List<Anuncio> listarPorDuracion(int minimo, int maximo) throws DaoException {
        List<Anuncio> anuncios = new ArrayList<>();
        String sql = "{CALL anuncios_por_duracion(?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, minimo);
            cs.setInt(2, maximo);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    anuncios.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return anuncios;
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

    private Anuncio mapear(ResultSet rs) throws SQLException {
        Anuncio a = new Anuncio();
        a.setIdAnuncio(rs.getInt("id_anuncio"));
        a.setNombre(rs.getString("nombre"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setGenero(rs.getString("genero"));
        a.setDuracion(rs.getInt("duracion"));
        a.setEmpresa(rs.getString("empresa"));
        return a;
    }
}
