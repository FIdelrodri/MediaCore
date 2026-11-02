package com.mediacore.factory;

import com.mediacore.dao.AnuncioDaoImpl;
import com.mediacore.dao.BloqueDaoImpl;
import com.mediacore.dao.GrillaDaoImpl;
import com.mediacore.dao.IAnuncioDao;
import com.mediacore.dao.IBloqueDao;
import com.mediacore.dao.IGrillaDao;
import com.mediacore.dao.IProgramaDao;
import com.mediacore.dao.IRolDao;
import com.mediacore.dao.IUsuarioDao;
import com.mediacore.dao.IVivoDao;
import com.mediacore.dao.ProgramaDaoImpl;
import com.mediacore.dao.RolDaoImpl;
import com.mediacore.dao.UsuarioDaoImpl;
import com.mediacore.dao.VivoDaoImpl;

/**
 * PATRON FACTORY: factoria que centraliza la creacion de los DAO.
 * Los Servlets piden el DAO a la factoria y solo conocen la interfaz (IProgramaDao, IVivoDao, ...),
 * nunca la clase concreta (ProgramaDaoImpl, VivoDaoImpl, ...). Si manana cambia la tecnologia de acceso a
 * datos, solo se modifica esta clase.
 */
public class DaoFactory {

    private DaoFactory() {
    }

    public static IUsuarioDao getUsuarioDao() {
        return new UsuarioDaoImpl();
    }

    public static IRolDao getRolDao() {
        return new RolDaoImpl();
    }

    public static IProgramaDao getProgramaDao() {
        return new ProgramaDaoImpl();
    }

    public static IVivoDao getVivoDao() {
        return new VivoDaoImpl();
    }

    public static IAnuncioDao getAnuncioDao() {
        return new AnuncioDaoImpl();
    }

    public static IBloqueDao getBloqueDao() {
        return new BloqueDaoImpl();
    }

    public static IGrillaDao getGrillaDao() {
        return new GrillaDaoImpl();
    }
}
