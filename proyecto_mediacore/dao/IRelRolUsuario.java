package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.RelRolUsuario;

public interface IRelRolUsuario {

    // Obtener todas las relaciones de roles y usuarios
    public List<RelRolUsuario> listarRelRolUsuarios();

    // Buscar una relación por su ID (idRolUsuario)
    public RelRolUsuario buscarRelRolUsuarioPorId(Integer idRolUsuario);

    // Guardar una nueva relación
    public RelRolUsuario guardarRelRolUsuario(RelRolUsuario relRolUsuario);

    // Modificar una relación existente
    public RelRolUsuario modificarRelRolUsuario(RelRolUsuario relRolUsuario);

    // Eliminar una relación por su ID
    public void eliminarRelRolUsuario(Integer idRolUsuario);
}
