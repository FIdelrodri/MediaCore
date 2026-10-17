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

            if (!credenciales.nombreUsuario.trim() || !credenciales.contrasena) {
                divMensaje.textContent = 'Por favor, completa todos los campos.';
                divMensaje.style.color = 'red';
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
                    divMensaje.textContent = resultado.mensaje;
                    divMensaje.style.color = 'green';
                    
                    setTimeout(() => {
                        window.location.href = '/panel';
                    }, 1000);
                } else {
                    divMensaje.textContent = resultado.mensaje;
                    divMensaje.style.color = 'red';
                }
            } catch (error) {
                console.error('Error al conectar con la API de login:', error);
                divMensaje.textContent = 'Ocurrió un error al conectar con el servidor.';
                divMensaje.style.color = 'red';
            }
        });
    }
});