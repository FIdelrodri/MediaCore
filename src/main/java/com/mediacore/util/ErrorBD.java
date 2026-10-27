package com.mediacore.util;

public class ErrorBD {

    private ErrorBD() {}

    // Obtiene el MESSAGE_TEXT del SIGNAL de MySQL (la causa mas profunda de la excepcion)
    public static String mensaje(Exception e) {
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }
}
