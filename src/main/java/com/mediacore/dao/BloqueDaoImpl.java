package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.Bloque;
import com.mediacore.modelo.DuracionPorTipo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BloqueDaoImpl extends BaseDao implements IBloqueDao {

    @Override
    public Integer obtenerId(String tipo, int idReferencia) throws DaoException {
        String sql = "{CALL obtener_bloque(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, tipo);
            cs.setInt(2, idReferencia);
            cs.registerOutParameter(3, Types.INTEGER); // p_id_bloque
            cs.execute();

            int idBloque = cs.getInt(3);
            return cs.wasNull() ? null : idBloque;

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public List<Bloque> listar() throws DaoException {
        return consultar("{CALL listar_todos_los_bloques()}", null);
    }

    @Override
    public List<Bloque> listarPorTipo(String tipo) throws DaoException {
        return consultar("{CALL listar_bloques_por_tipo(?)}", tipo);
    }

    @Override
    public List<Bloque> listarNoUsados() throws DaoException {
        return consultar("{CALL listar_bloques_no_usados()}", null);
    }

    @Override
    public List<DuracionPorTipo> duracionTotalPorTipo() throws DaoException {
        List<DuracionPorTipo> totales = new ArrayList<>();
        String sql = "{CALL duracion_total_por_tipo()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                totales.add(new DuracionPorTipo(rs.getString("tipo_contenido"),
                        rs.getLong("cantidad_bloques"), rs.getLong("segundos_totales")));
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return totales;
    }

    @Override
    public void recalcularDuracion(int idBloque) throws DaoException {
        String sql = "{CALL recalcular_duracion_bloque(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idBloque);
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public int limpiarHuerfanos() throws DaoException {
        String sql = "{CALL limpiar_bloques_huerfanos()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            return rs.next() ? rs.getInt("bloques_eliminados") : 0;

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    // ---- metodos privados de apoyo ----

    // parametro: null (sin parametros) o el tipo de contenido
    private List<Bloque> consultar(String sql, String parametro) throws DaoException {
        List<Bloque> bloques = new ArrayList<>();

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            if (parametro != null) {
                cs.setString(1, parametro);
            }

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    bloques.add(new Bloque(rs.getInt("id_bloque"), rs.getString("tipo_contenido"),
                            rs.getInt("id_referencia"), rs.getString("titulo"), rs.getInt("duracion")));
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return bloques;
    }
}
