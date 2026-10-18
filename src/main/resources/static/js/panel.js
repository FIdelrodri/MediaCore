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