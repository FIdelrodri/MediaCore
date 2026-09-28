package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.Programa;

public interface IPrograma {

    // Obtener todos los programas
    public List<Programa> listarProgramas();

    // Buscar un programa por su ID (idPrograma)
    public Programa buscarProgramaPorId(Integer idPrograma);

    // Guardar un nuevo programa
    public Programa guardarPrograma(Programa programa);

    // Modificar un programa existente
    public Programa modificarPrograma(Programa programa);

    // Eliminar un programa por su ID
    public void eliminarPrograma(Integer idPrograma);
}
