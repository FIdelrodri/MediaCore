package com.mediacore.dto;

// Datos que llegan en el POST /api/usuarios/registro
public class RegistroRequest {
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;

    public RegistroRequest() {}

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
