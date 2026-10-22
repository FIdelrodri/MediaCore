document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Consultar los datos de la sesión activa al backend
        const respuesta = await fetch('/api/auth/sesion-actual');
        const datosSesion = await respuesta.json();

        if (datosSesion.autenticado) {
            const rolUsuario = String(datosSesion.idRol);
            const modulosProtegidos = document.querySelectorAll('[data-roles-permitidos]');

            modulosProtegidos.forEach(modulo => {
                // Obtener la lista de roles permitidos del atributo data-roles-permitidos="1,2"
                const rolesPermitidos = modulo.getAttribute('data-roles-permitidos').split(',').map(r => r.trim());

                if (rolesPermitidos.includes(rolUsuario)) {
                     modulo.style.display = 'flex'; 
            }
            });
        } else {
            window.location.href = '/login';
        }
    } catch (error) {
        console.error('Error al verificar el rol del usuario:', error);
    }
});
//boton de cerrar sesion
document.addEventListener('DOMContentLoaded', () => {
    const btnLogout = document.getElementById('btnCerrarSesion');
    if (btnLogout) {
        btnLogout.addEventListener('click', cerrarSesion);
    }
});

async function cerrarSesion() {
    try {
        const respuesta = await fetch('/api/auth/logout', {
            method: 'POST'
        });

        if (respuesta.ok) {
            window.location.href = '/login';
        } else {
            console.error('Error al cerrar sesión');
        }
    } catch (error) {
        console.error('Error de red al intentar cerrar sesión:', error);
    }
}