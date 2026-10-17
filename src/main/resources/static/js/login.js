document.addEventListener('DOMContentLoaded', () => {
    const formLogin = document.getElementById('formLogin');

    if (formLogin) {
        formLogin.addEventListener('submit', async function(event) {
            event.preventDefault();

            const credenciales = {
                nombreUsuario: document.getElementById('nombreUsuario').value,
                contrasena: document.getElementById('contrasena').value
            };

            const divMensaje = document.getElementById('mensaje');

            function mostrarError(mensaje) {
                divMensaje.textContent = mensaje;
                divMensaje.classList.remove('exito');
                divMensaje.classList.add('error');
            }

            function mostrarExito(mensaje) {
                divMensaje.textContent = mensaje;
                divMensaje.classList.remove('error');
                divMensaje.classList.add('exito');
            }
            if (!credenciales.nombreUsuario.trim() || !credenciales.contrasena) {
                mostrarError('Por favor, completa todos los campos.');
                return;
            }

            try {
                const respuesta = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(credenciales)
                });

                const resultado = await respuesta.json();

                if (respuesta.ok) {
                    mostrarExito(resultado.mensaje);
                    setTimeout(() => {
                        window.location.href = '/panel';
                    }, 1000);
                } else {
                    mostrarError(resultado.mensaje);
                }
            } catch (error) {
                console.error('Error al conectar con la API de login:', error);
                mostrarError('Ocurrió un error al procesar la solicitud. Por favor, inténtalo de nuevo más tarde.');
            }
        });
    }
});