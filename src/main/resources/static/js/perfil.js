document.addEventListener('DOMContentLoaded', iniciar);

async function iniciar() {
    document.getElementById('formDatos').addEventListener('submit', guardarDatos);
    document.getElementById('formContrasena').addEventListener('submit', cambiarContrasena);
    await cargarPerfil();
}

async function apiFetch(url, method = 'GET', body = null) {
    try {
        const options = { method, headers: { 'Content-Type': 'application/json' } };
        if (body) options.body = JSON.stringify(body);

        const res = await fetch(url, options);

        if (res.status === 401) {
            window.location.href = '/login';
            return { redirected: true };
        }

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            return { ok: false, msg: data.mensaje || 'Error al procesar la solicitud.' };
        }

        return { ok: true, data };
    } catch (err) {
        return { ok: false, msg: 'Error de conexión con el servidor.' };
    }
}

// Carga los datos del usuario en sesión en el resumen y en el formulario
async function cargarPerfil() {
    const res = await apiFetch('/api/auth/sesion-actual');
    if (res.redirected) return;

    if (!res.ok || !res.data.autenticado) {
        window.location.href = '/login';
        return;
    }

    const u = res.data;
    document.getElementById('perfilUsuario').textContent = '@' + u.nombreUsuario;
    document.getElementById('perfilRol').textContent = NOMBRES_ROLES[u.idRol] || 'Sin rol';

    document.getElementById('nombre').value = u.nombre || '';
    document.getElementById('apellido').value = u.apellido || '';
    document.getElementById('email').value = u.email || '';
}

async function guardarDatos(evento) {
    evento.preventDefault();

    const datos = {
        nombre: document.getElementById('nombre').value.trim(),
        apellido: document.getElementById('apellido').value.trim(),
        email: document.getElementById('email').value.trim()
    };

    if (!datos.nombre || datos.nombre.length > 25) {
        mostrarAlerta('El nombre es obligatorio y no puede tener más de 25 caracteres.', false);
        return;
    }
    if (!datos.apellido || datos.apellido.length > 25) {
        mostrarAlerta('El apellido es obligatorio y no puede tener más de 25 caracteres.', false);
        return;
    }
    if (!datos.email || datos.email.length > 50) {
        mostrarAlerta('El email es obligatorio y no puede tener más de 50 caracteres.', false);
        return;
    }

    const res = await apiFetch('/api/usuarios/datos', 'PUT', datos);
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Datos actualizados exitosamente.') : res.msg, res.ok);
    if (res.ok) {
        // Refleja el cambio en la barra superior
        actualizarBarraUsuario();
    }
}

async function cambiarContrasena(evento) {
    evento.preventDefault();

    const contrasenaActual = document.getElementById('contrasenaActual').value;
    const contrasenaNueva = document.getElementById('contrasenaNueva').value;
    const contrasenaRepetida = document.getElementById('contrasenaRepetida').value;

    const errorValidacion = validarContrasena(contrasenaNueva);
    if (errorValidacion) {
        mostrarAlerta(errorValidacion, false);
        return;
    }
    if (contrasenaNueva !== contrasenaRepetida) {
        mostrarAlerta('La contraseña nueva y su repetición no coinciden.', false);
        return;
    }

    const res = await apiFetch('/api/usuarios/contrasena', 'PUT', { contrasenaActual, contrasenaNueva });
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Contraseña actualizada exitosamente.') : res.msg, res.ok);
    if (res.ok) {
        document.getElementById('formContrasena').reset();
    }
}

// Mismas reglas que el registro y que el backend
function validarContrasena(contrasena) {
    if (contrasena.length < 6) {
        return 'La contraseña debe tener al menos 6 caracteres.';
    }
    const caracteres = contrasena.split('');
    if (!caracteres.some(c => c >= 'A' && c <= 'Z')) {
        return 'La contraseña debe contener al menos una letra mayúscula.';
    }
    if (!caracteres.some(c => c >= 'a' && c <= 'z')) {
        return 'La contraseña debe contener al menos una letra minúscula.';
    }
    if (!caracteres.some(c => c >= '0' && c <= '9')) {
        return 'La contraseña debe contener al menos un número.';
    }
    return null;
}

function mostrarAlerta(mensaje, esExito) {
    const div = document.getElementById('mensaje');
    if (!div) return;

    div.textContent = mensaje;
    div.classList.toggle('exito', esExito);
    div.classList.toggle('error', !esExito);

    setTimeout(() => {
        div.classList.remove('exito', 'error');
    }, 4000);
}
