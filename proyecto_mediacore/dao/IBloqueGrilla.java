package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.BloqueGrilla;

public interface IBloqueGrilla {

    // Obtener todos los bloques de grilla
    public List<BloqueGrilla> listarBloqueGrillas();

    // Buscar un bloque de grilla por su ID (idBloque)
    public BloqueGrilla buscarBloqueGrillaPorId(Integer idBloque);

    // Guardar un nuevo bloque de grilla
    public BloqueGrilla guardarBloqueGrilla(BloqueGrilla bloqueGrilla);

    // Modificar un bloque de grilla existente
    public BloqueGrilla modificarBloqueGrilla(BloqueGrilla bloqueGrilla);

    // Eliminar un bloque de grilla por su ID
    public void eliminarBloqueGrilla(Integer idBloque);
}
