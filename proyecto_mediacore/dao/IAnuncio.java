package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.Anuncio;

public interface IAnuncio {

    // Obtener todos los anuncios
    public List<Anuncio> listarAnuncios();

    // Buscar un anuncio por su ID
    public Anuncio buscarAnuncioPorId(Integer idAnuncio);

    // Guardar o registrar un nuevo anuncio
    public Anuncio guardarAnuncio(Anuncio anuncio);

    // Modificar los datos de un anuncio existente
    public Anuncio modificarAnuncio(Anuncio anuncio);

    // Eliminar un anuncio por su ID
    public void eliminarAnuncio(Integer idAnuncio);
}
