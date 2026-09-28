package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.RelRolPermiso;

public interface IRelRolPermiso {

    // Obtener todas las relaciones de roles y permisos
    public List<RelRolPermiso> listarRelRolPermisos();

    // Buscar una relación por su ID (idRolPermiso)
    public RelRolPermiso buscarRelRolPermisoPorId(Integer idRolPermiso);

    // Guardar una nueva relación
    public RelRolPermiso guardarRelRolPermiso(RelRolPermiso relRolPermiso);

    // Modificar una relación existente
    public RelRolPermiso modificarRelRolPermiso(RelRolPermiso relRolPermiso);

    // Eliminar una relación por su ID
    public void eliminarRelRolPermiso(Integer idRolPermiso);
}
