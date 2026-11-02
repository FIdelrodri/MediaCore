package com.mediacore.modelo;

import java.io.Serializable;
import java.util.Objects;

/** Representa a un usuario del sistema (tabla usuario). La contraseña es transient: Gson nunca la serializa. */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idUsuario;
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String fechaRegistro;
    private int idRol;
    private String nombreRol;
    private transient String contrasena;

    // Constructor vacio (obligatorio en un JavaBean; tambien lo usa Gson)
    public Usuario() {
    }

    // Constructor completo
    public Usuario(int idUsuario, String nombreUsuario, String nombre, String apellido, String email, String fechaRegistro, int idRol, String nombreRol, String contrasena) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.contrasena = contrasena;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario that = (Usuario) o;
        return idUsuario == that.idUsuario;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + "'" +
                ", nombre='" + nombre + "'" +
                ", apellido='" + apellido + "'" +
                ", email='" + email + "'" +
                ", fechaRegistro='" + fechaRegistro + "'" +
                ", idRol=" + idRol +
                ", nombreRol='" + nombreRol + "'" +
                "}";
    }
}
