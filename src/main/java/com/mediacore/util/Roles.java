package com.mediacore.util;

/**
 * Ids de los roles de la tabla rol y los grupos de roles que usan los Servlets y el filtro de vistas.
 */
public class Roles {

    private Roles() {
    }

    public static final int ADMINISTRADOR_GENERAL = 1;
    public static final int ADMINISTRADOR = 2;
    public static final int ESCENOGRAFO = 3;
    public static final int ADMINISTRADOR_AUDIO = 4;
    public static final int ADMINISTRADOR_PLANILLA = 5;
    public static final int POSTPRODUCCION = 6;
    public static final int USUARIO = 7;

    // Atributo de la sesion HTTP donde se guarda el Usuario que inicio sesion
    public static final String ATRIBUTO_SESION = "usuarioLogueado";
}
