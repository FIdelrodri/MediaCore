package proyecto_mediacore.dao.implementacion;

import java.util.List;
import proyecto_mediacore.dao.IUsuario;
import proyecto_mediacore.model.Usuario;

public class UsuarioDAO implements IUsuario {

    @Override
    public List<Usuario> listarUsuarios() {
        // TODO: Lógica para obtener todos los usuarios
        return null;
    }

    @Override
    public Usuario buscarUsuarioPorId(Integer idUsuario) {
        // TODO: Lógica para buscar un usuario por su ID
        return null;
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        // TODO: Lógica para registrar un nuevo usuario
        return null;
    }

    @Override
    public Usuario modificarUsuario(Usuario usuario) {
        // TODO: Lógica para modificar un usuario existente
        return null;
    }

    @Override
    public void eliminarUsuario(Integer idUsuario) {
        // TODO: Lógica para eliminar un usuario por su ID
    }
}
