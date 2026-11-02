package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.Vivo;
import com.mediacore.modelo.VivoPublico;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VivoDaoImpl extends BaseDao implements IVivoDao {

    @Override
    public int crear(Vivo vivo) throws DaoException {
        String sql = "{CALL crear_vivo(?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, vivo.getTitulo());
            cs.setString(2, vivo.getDescripcion());
            cs.setInt(3, vivo.getDuracion());
            cs.registerOutParameter(4, Types.INTEGER); // p_id_envivo
            cs.execute();

            return cs.getInt(4);

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void modificar(Vivo vivo) throws DaoException {
        String sql = "{CALL modificar_vivo(?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, vivo.getIdEnvivo());
            cs.setString(2, vivo.getTitulo());
            cs.setString(3, vivo.getDescripcion());
            cs.setInt(4, vivo.getDuracion());
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void eliminar(int idEnvivo) throws DaoException {
        ejecutar("{CALL eliminar_vivo(?)}", idEnvivo);
    }

    @Override
    public void marcarComoRetransmitido(int idEnvivo) throws DaoException {
        ejecutar("{CALL marcar_como_retransmitido(?)}", idEnvivo);
    }

    @Override
    public void programar(int idEnvivo) throws DaoException {
        ejecutar("{CALL programar_vivo(?)}", idEnvivo);
    }

    @Override
    public List<Vivo> listar() throws DaoException {
        return consultar("{CALL listar_vivos()}");
    }

    @Override
    public List<Vivo> listarPendientes() throws DaoException {
        return consultar("{CALL listar_vivos_pendientes()}");
    }

    @Override
    public List<VivoPublico> listarRetransmitidos() throws DaoException {
        List<VivoPublico> vivos = new ArrayList<>();
        String sql = "{CALL listar_vivos_retransmitidos()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                vivos.add(new VivoPublico(rs.getInt("id_envivo"), rs.getString("titulo"),
                        rs.getString("descripcion"), rs.getInt("duracion")));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return vivos;
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

    private List<Vivo> consultar(String sql) throws DaoException {
        List<Vivo> vivos = new ArrayList<>();

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                vivos.add(new Vivo(rs.getInt("id_envivo"), rs.getString("titulo"),
                        rs.getString("descripcion"), rs.getInt("duracion"), rs.getBoolean("retransmitido")));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return vivos;
    }
}
