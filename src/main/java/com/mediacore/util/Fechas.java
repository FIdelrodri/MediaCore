package com.mediacore.util;

/**
 * Las fechas se leen de MySQL como texto ("2026-10-29 14:05:09.0") y se devuelven en formato ISO
 * ("2026-10-29T14:05:09"), igual que antes. Asi no hay conversiones de zona horaria de por medio.
 */
public class Fechas {

    private Fechas() {
    }

    public static String iso(String fechaMySql) {
        if (fechaMySql == null) {
            return null;
        }
        String f = fechaMySql.trim().replace(' ', 'T');
        return f.length() > 19 ? f.substring(0, 19) : f;
    }
}
