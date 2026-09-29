package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.Permiso;

public interface IPermiso {

    // Obtener todos los permisos
    public List<Permiso> listarPermisos();

    // Buscar un permiso por su ID (idPermiso)
    public Permiso buscarPermisoPorId(Integer idPermiso);

    // Guardar un nuevo permiso
    public Permiso guardarPermiso(Permiso permiso);

    // Modificar un permiso existente
    public Permiso modificarPermiso(Permiso permiso);

    // Eliminar un permiso por su ID
    public void eliminarPermiso(Integer idPermiso);
}
