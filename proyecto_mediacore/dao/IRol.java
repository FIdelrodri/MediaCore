package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.Rol;

public interface IRol {

    // Obtener todos los roles
    public List<Rol> listarRoles();

    // Buscar un rol por su ID (idRol)
    public Rol buscarRolPorId(Integer idRol);

    // Guardar un nuevo rol
    public Rol guardarRol(Rol rol);

    // Modificar un rol existente
    public Rol modificarRol(Rol rol);

    // Eliminar un rol por su ID
    public void eliminarRol(Integer idRol);
}
