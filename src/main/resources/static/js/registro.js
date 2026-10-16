document.addEventListener('DOMContentLoaded', () => {
    const formRegistro = document.getElementById('formRegistro');

    if (formRegistro) {
        formRegistro.addEventListener('submit', async function(event) {
            event.preventDefault();

            // 1. Obtener valores de los inputs
            const datosUsuario = {
                nombreUsuario: document.getElementById('nombreUsuario').value,
                nombre: document.getElementById('nombre').value,
                apellido: document.getElementById('apellido').value,
                email: document.getElementById('email').value,
                contrasena: document.getElementById('contrasena').value,
            };

            const divMensaje = document.getElementById('Alerta');

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

            // Validaciones de los datos del usuario
            if (datosUsuario.nombreUsuario.length > 25) {
                mostrarError('El nombre de usuario no puede tener más de 25 caracteres.');
                return;
            }
            if (datosUsuario.nombre.length > 25) {
                mostrarError('El nombre no puede tener más de 25 caracteres.');
                return;
            }
            if (datosUsuario.apellido.length > 25) {
                mostrarError('El apellido no puede tener más de 25 caracteres.');
                return;
            }
            if (datosUsuario.email.length > 50) {
                mostrarError('El email no puede tener más de 50 caracteres.');
                return;
            }
            if (datosUsuario.contrasena.length < 6) {
                mostrarError('La contraseña debe tener al menos 6 caracteres.');
                return;
            }

            const verificarcontraseña = datosUsuario.contrasena.split('');
            const tieneMayuscula = verificarcontraseña.some(caracter => caracter >= 'A' && caracter <= 'Z');
            const tieneLetra = verificarcontraseña.some(caracter => caracter >= 'a' && caracter <= 'z');
            const tieneNumero = verificarcontraseña.some(caracter => caracter >= '0' && caracter <= '9');

            if (!tieneMayuscula) {
                mostrarError('La contraseña debe contener al menos una letra mayúscula.');
                return;
            }
            if (!tieneLetra) {
                mostrarError('La contraseña debe contener al menos una letra minúscula.');
                return;
            }
            if (!tieneNumero) {
                mostrarError('La contraseña debe contener al menos un número.');
                return;
            }

            try {
                // 2. Petición a la API Spring Boot
                const respuesta = await fetch('/api/usuarios/registro', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(datosUsuario)
                });

                const resultado = await respuesta.json();

                // 3. Procesar respuesta
                if (respuesta.ok) {
                    mostrarExito(resultado.mensaje || 'Registro exitoso.');
                    formRegistro.reset();
                } else {
                    mostrarError(resultado.mensaje || 'Error al registrar el usuario.');
                }
            } catch (error) {
                console.error('Error al conectar con la API:', error);
                mostrarError('Ocurrió un error al conectar con el servidor.');
            }
        });
    }
});