package proyecto_mediacore.dao;

import java.util.List;
import proyecto_mediacore.model.BloqueAnuncio;

public interface IBloqueAnuncio {

    // Obtener todos los bloques de anuncios
    public List<BloqueAnuncio> listarBloqueAnuncios();

    // Buscar un bloque de anuncio por su ID
    public BloqueAnuncio buscarBloqueAnuncioPorId(Integer idBloqueAnuncio);

    // Guardar o registrar un nuevo bloque de anuncio
    public BloqueAnuncio guardarBloqueAnuncio(BloqueAnuncio bloqueAnuncio);

    // Modificar un bloque de anuncio existente
    public BloqueAnuncio modificarBloqueAnuncio(BloqueAnuncio bloqueAnuncio);

    // Eliminar un bloque de anuncio por su ID
    public void eliminarBloqueAnuncio(Integer idBloqueAnuncio);
}
