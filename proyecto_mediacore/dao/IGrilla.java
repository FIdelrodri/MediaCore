package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.Grilla;

public interface IGrilla {

    // Obtener todas las grillas
    public List<Grilla> listarGrillas();

    // Buscar una grilla por su ID (idGrilla)
    public Grilla buscarGrillaPorId(Integer idGrilla);

    // Guardar una nueva grilla
    public Grilla guardarGrilla(Grilla grilla);

    // Modificar una grilla existente
    public Grilla modificarGrilla(Grilla grilla);

    // Eliminar una grilla por su ID
    public void eliminarGrilla(Integer idGrilla);
}
