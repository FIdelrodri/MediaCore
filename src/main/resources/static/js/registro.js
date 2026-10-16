document.addEventListener('DOMContentLoaded', () => {
    const formRegistro = document.getElementById('formRegistro');

    if (formRegistro) {
        formRegistro.addEventListener('submit', async function(event) {
            event.preventDefault();

            // 1. Obtener valores de los inputs del HTML
            const datosUsuario = {
                nombre: document.getElementById('nombre').value,
                apellido: document.getElementById('apellido').value,
                email: document.getElementById('email').value,
                contrasena: document.getElementById('contrasena').value,
                idRol: document.getElementById('idRol').value ? parseInt(document.getElementById('idRol').value) : null
            };

            const divMensaje = document.getElementById('mensaje');

            try {
                // 2. Hacer la llamada a la API en Java (Spring Boot)
                const respuesta = await fetch('/api/usuarios/registro', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(datosUsuario)
                });

                const resultado = await respuesta.json();

                // 3. Procesar la respuesta de la API
                if (respuesta.ok) {
                    divMensaje.textContent = resultado.mensaje;
                    divMensaje.style.color = 'green';
                    formRegistro.reset();
                } else {
                    divMensaje.textContent = resultado.mensaje;
                    divMensaje.style.color = 'red';
                }
            } catch (error) {
                console.error('Error al conectar con la API:', error);
                divMensaje.textContent = 'Ocurrió un error al conectar con el servidor.';
                divMensaje.style.color = 'red';
            }
        });
    }
});