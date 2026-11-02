package com.mediacore.dao;

import com.mediacore.config.Conexion;
import com.mediacore.modelo.BloqueEnAire;
import com.mediacore.modelo.DuracionGrilla;
import com.mediacore.modelo.EmisionHistorial;
import com.mediacore.modelo.PosicionGrilla;
import com.mediacore.util.Fechas;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class GrillaDaoImpl extends BaseDao implements IGrillaDao {

    // ---- Consultas ----

    @Override
    public List<PosicionGrilla> verConHorariosProyectados() throws DaoException {
        List<PosicionGrilla> posiciones = new ArrayList<>();
        String sql = "{CALL ver_grilla_con_horarios_proyectados()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                PosicionGrilla p = new PosicionGrilla();
                p.setOrden(rs.getInt("orden"));
                p.setIdPosicion(rs.getInt("id_posicion"));
                p.setIdBloque(rs.getInt("id_bloque"));
                p.setTipoContenido(rs.getString("tipo_contenido"));
                p.setTitulo(rs.getString("titulo"));
                p.setDuracionSegundos(rs.getInt("duracion_segundos"));
                p.setInicioProyectado(Fechas.iso(rs.getString("inicio_proyectado")));
                p.setFinProyectado(Fechas.iso(rs.getString("fin_proyectado")));
                p.setAudio(rs.getInt("audio"));
                posiciones.add(p);
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return posiciones;
    }

    @Override
    public DuracionGrilla duracionTotal() throws DaoException {
        String sql = "{CALL duracion_total_grilla()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            if (rs.next()) {
                return new DuracionGrilla(rs.getLong("cantidad_bloques"), rs.getLong("segundos_totales"),
                        rs.getLong("segundos_restantes"));
            }
            return new DuracionGrilla(0, 0, 0);

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public BloqueEnAire bloqueEnAire() throws DaoException {
        String sql = "{CALL bloque_en_aire()}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            if (!rs.next()) {
                return null;
            }
            BloqueEnAire b = new BloqueEnAire();
            b.setOrden(rs.getInt("orden"));
            b.setIdPosicion(rs.getInt("id_posicion"));
            b.setIdBloque(rs.getInt("id_bloque"));
            b.setTipoContenido(rs.getString("tipo_contenido"));
            b.setTitulo(rs.getString("titulo"));
            b.setDuracionSegundos(rs.getInt("duracion_segundos"));
            b.setHoraInicio(Fechas.iso(rs.getString("hora_inicio")));
            b.setHoraFin(Fechas.iso(rs.getString("hora_fin")));
            b.setSegundosRestantes(rs.getLong("segundos_restantes"));
            b.setAudio(rs.getInt("audio"));
            return b;

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public List<EmisionHistorial> verHistorial(int limite) throws DaoException {
        List<EmisionHistorial> historial = new ArrayList<>();
        String sql = "{CALL ver_historial_emision(?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, limite);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    historial.add(new EmisionHistorial(rs.getInt("id_historial"), rs.getInt("id_bloque"),
                            rs.getString("tipo_contenido"), rs.getString("titulo"),
                            Fechas.iso(rs.getString("hora_inicio")), Fechas.iso(rs.getString("hora_fin_real"))));
                }
            }

        } catch (SQLException e) {
            throw traducir(e);
        }
        return historial;
    }

    // ---- Operaciones sobre la cola ----

    @Override
    public void agregarBloque(int idBloque) throws DaoException {
        ejecutar("{CALL agregar_bloque_a_grilla(?)}", idBloque);
    }

    @Override
    public void avanzar() throws DaoException {
        ejecutar("{CALL avanzar_grilla()}");
    }

    @Override
    public void saltarBloqueActual() throws DaoException {
        ejecutar("{CALL saltar_bloque_actual()}");
    }

    @Override
    public void quitarPosicion(int posicion) throws DaoException {
        ejecutar("{CALL quitar_de_grilla(?)}", posicion);
    }

    @Override
    public void vaciar() throws DaoException {
        ejecutar("{CALL vaciar_grilla()}");
    }

    // ---- Reordenamiento de la cola ----

    @Override
    public int insertarEnPosicion(int idBloque, int posicion) throws DaoException {
        String sql = "{CALL insertar_bloque_en_posicion(?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idBloque);
            cs.setInt(2, posicion);
            cs.registerOutParameter(3, Types.INTEGER); // p_id_posicion
            cs.execute();

            return cs.getInt(3);

        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public void intercambiar(int posicionA, int posicionB) throws DaoException {
        ejecutar("{CALL intercambiar_posiciones(?, ?)}", posicionA, posicionB);
    }

    @Override
    public void mover(int origen, int destino) throws DaoException {
        ejecutar("{CALL mover_bloque(?, ?)}", origen, destino);
    }

    @Override
    public void reemplazar(int posicion, int idBloqueNuevo) throws DaoException {
        ejecutar("{CALL reemplazar_bloque(?, ?)}", posicion, idBloqueNuevo);
    }

    @Override
    public void duplicar(int posicion) throws DaoException {
        ejecutar("{CALL duplicar_posicion(?)}", posicion);
    }

    @Override
    public void cambiarAudio(int audio) throws DaoException {
        ejecutar("{CALL cambiar_audio_grilla(?)}", audio);
    }

    // ---- metodos privados de apoyo ----

    // Ejecuta un procedure que solo recibe enteros (o ninguno) y no devuelve filas
    private void ejecutar(String sql, int... parametros) throws DaoException {
        try (Connection con = Conexion.getInstancia().getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                cs.setInt(i + 1, parametros[i]);
            }
            cs.execute();

        } catch (SQLException e) {
            throw traducir(e);
        }
    }
}
