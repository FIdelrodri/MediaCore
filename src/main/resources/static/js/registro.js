document.addEventListener('DOMContentLoaded', () => {
    const formRegistro = document.getElementById('formRegistro');

    if (formRegistro) {
        formRegistro.addEventListener('submit', async function(event) {
            event.preventDefault();

            // 1. Obtener valores de los inputs del HTML
            const datosUsuario = {
                nombreUsuario: document.getElementById('nombreUsuario').value,
                nombre: document.getElementById('nombre').value,
                apellido: document.getElementById('apellido').value,
                email: document.getElementById('email').value,
                contrasena: document.getElementById('contrasena').value,
            };

            const divMensaje = document.getElementById('mensaje');

            // Validaciones de los datos del usuario
            if (datosUsuario.nombreUsuario.length > 25) {
                divMensaje.textContent = 'El nombre de usuario no puede tener más de 25 caracteres.';
                divMensaje.style.color = 'red';
                return;
            }
            if (datosUsuario.nombre.length > 25) {
                divMensaje.textContent = 'El nombre no puede tener más de 25 caracteres.';
                divMensaje.style.color = 'red';
                return;
            }
            if (datosUsuario.apellido.length > 25) {
                divMensaje.textContent = 'El apellido no puede tener más de 25 caracteres.';
                divMensaje.style.color = 'red';
                return;
            }
            if (datosUsuario.email.length > 50) {
                divMensaje.textContent = 'El email no puede tener más de 50 caracteres.';
                divMensaje.style.color = 'red';
                return;
            }
            if (datosUsuario.contrasena.length < 6) {
                divMensaje.textContent = 'La contraseña debe tener al menos 6 caracteres.';
                divMensaje.style.color = 'red';
                return;
            }

            const verificarcontraseña = datosUsuario.contrasena.split('');
            const tieneMayuscula = verificarcontraseña.some(caracter => caracter >= 'A' && caracter <= 'Z');
            const tieneLetra = verificarcontraseña.some(caracter => caracter >= 'a' && caracter <= 'z');
            const tieneNumero = verificarcontraseña.some(caracter => caracter >= '0' && caracter <= '9');

            if (!tieneMayuscula) {
                divMensaje.textContent = 'La contraseña debe contener al menos una letra mayúscula.';
                divMensaje.style.color = 'red';
                return;
            }
            if (!tieneLetra) {
                divMensaje.textContent = 'La contraseña debe contener al menos una letra minúscula.';
                divMensaje.style.color = 'red';
                return;
            }
            if (!tieneNumero) {
                divMensaje.textContent = 'La contraseña debe contener al menos un número.';
                divMensaje.style.color = 'red';
                return;
            }

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