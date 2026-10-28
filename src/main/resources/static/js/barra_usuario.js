// Muestra en la barra superior la imagen, el nombre y el rol del usuario en sesión.
// Al hacer clic lleva a la vista de perfil. Se incluye en todas las vistas con sesión.
const NOMBRES_ROLES = {
    1: 'Administrador General',
    2: 'Administrador',
    3: 'Escenógrafo',
    4: 'Administrador de Audio',
    5: 'Administrador de Planilla',
    6: 'Personal de Postproducción',
    7: 'Usuario'
};

document.addEventListener('DOMContentLoaded', actualizarBarraUsuario);

async function actualizarBarraUsuario() {
    const barra = document.querySelector('.barra_superior');
    if (!barra) return;

    try {
        const res = await fetch('/api/auth/sesion-actual');
        const sesion = await res.json();
        if (!sesion.autenticado) return;

        const nombreCompleto = [sesion.nombre, sesion.apellido].filter(x => x).join(' ')
            || sesion.nombreUsuario;
        const rol = NOMBRES_ROLES[sesion.idRol] || 'Sin rol';

        let chip = document.getElementById('usuario_barra');
        if (!chip) {
            chip = document.createElement('a');
            chip.id = 'usuario_barra';
            chip.className = 'usuario_barra';
            chip.href = '/perfil';
            chip.title = 'Ir a mi perfil';
            chip.innerHTML = `
                <img class="usuario_barra_img" src="/img/usuario_logo.png" alt="Usuario">
                <span class="usuario_barra_texto">
                    <span class="usuario_barra_nombre"></span>
                    <span class="usuario_barra_rol"></span>
                </span>`;

            // Se ubica a la izquierda de la barra, a continuación del logo
            const logo = barra.querySelector('.logowich_arriba');
            if (logo) {
                logo.insertAdjacentElement('afterend', chip);
            } else {
                barra.prepend(chip);
            }
        }

        // textContent evita interpretar como HTML lo que viene de la base de datos
        chip.querySelector('.usuario_barra_nombre').textContent = nombreCompleto;
        chip.querySelector('.usuario_barra_rol').textContent = rol;
    } catch (error) {
        console.error('No se pudo cargar el usuario en la barra superior:', error);
    }
}
