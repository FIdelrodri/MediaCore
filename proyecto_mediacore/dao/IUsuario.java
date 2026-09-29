package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.Usuario;

public interface IUsuario {

    // Obtener todos los usuarios
    public List<Usuario> listarUsuarios();

    // Buscar un usuario por su ID (idUsuario)
    public Usuario buscarUsuarioPorId(Integer idUsuario);

    // Guardar un nuevo usuario
    public Usuario guardarUsuario(Usuario usuario);

    // Modificar un usuario existente
    public Usuario modificarUsuario(Usuario usuario);

    // Eliminar un usuario por su ID
    public void eliminarUsuario(Integer idUsuario);
}
