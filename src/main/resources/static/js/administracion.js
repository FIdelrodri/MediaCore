document.addEventListener('DOMContentLoaded', cargarUsuarios);

const ROLES_ASIGNABLES = [
    { id: 2, nombre: 'Administrador' },
    { id: 3, nombre: 'Escenógrafo' },
    { id: 4, nombre: 'Administrador de Audio' },
    { id: 5, nombre: 'Administrador de Planilla' },
    { id: 6, nombre: 'Personal de Postproducción' },
    { id: 7, nombre: 'Usuario' }
];

async function apiFetch(url, method = 'GET', body = null) {
    try {
        const options = { method, headers: { 'Content-Type': 'application/json' } };
        if (body) options.body = JSON.stringify(body);

        const res = await fetch(url, options);

        if (res.status === 403) {
            window.location.href = '/panel';
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

async function cargarUsuarios() {
    const tbody = document.getElementById('lista_usuarios');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Cargando usuarios...</td></tr>';

    const res = await apiFetch('/api/admin/usuarios');
    if (res.redirected) return;

    if (!res.ok) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Error al cargar la lista de usuarios.</td></tr>';
        mostrarAlerta(res.msg, false);
        return;
    }

    renderizarTabla(res.data);
}

function renderizarTabla(usuarios) {
    const tbody = document.getElementById('lista_usuarios');

    if (!usuarios?.length) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No hay usuarios registrados.</td></tr>';
        return;
    }

    tbody.innerHTML = usuarios.map(u => {
        const esAdmin = u.idRol === 1;

        const selectHtml = esAdmin 
            ? `<select disabled><option value="1">Administrador General</option></select>`
            : `<select id="select-rol-${u.idUsuario}">
                ${ROLES_ASIGNABLES.map(r => `<option value="${r.id}" ${u.idRol === r.id ? 'selected' : ''}>${r.nombre}</option>`).join('')}
               </select>`;

        const accionesHtml = esAdmin 
            ? '-'
            : `<button class="btn_guardar" onclick="guardarRol(${u.idUsuario})">Guardar</button>
               <button class="btn_eliminar" onclick="eliminarUsuario(${u.idUsuario}, '${u.nombreUsuario}', ${u.idRol})">Eliminar</button>`;

        return `
            <tr>
                <td>${u.idUsuario}</td>
                <td><strong>${u.nombreUsuario}</strong></td>
                <td>${u.nombre}</td>
                <td>${u.apellido}</td>
                <td>${u.email}</td>
                <td>${selectHtml}</td>
                <td>${accionesHtml}</td>
            </tr>
        `;
    }).join('');
}

async function guardarRol(idUsuario) {
    const select = document.getElementById(`select-rol-${idUsuario}`);
    if (!select) return;

    const idRol = parseInt(select.value, 10);
    if (idRol === 1) {
        mostrarAlerta('No está permitido otorgar el rol de Administrador General.', false);
        return;
    }

    const res = await apiFetch('/api/admin/asignar-rol', 'PUT', { idUsuario, idRol });
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Rol actualizado correctamente.') : res.msg, res.ok);
    if (res.ok) cargarUsuarios();
}

async function eliminarUsuario(idUsuario, nombreUsuario, idRol) {
    if (idRol === 1) {
        mostrarAlerta('No se puede eliminar a un Administrador General.', false);
        return;
    }

    if (!confirm(`¿Seguro que deseas eliminar al usuario "${nombreUsuario}"?`)) return;

    const res = await apiFetch('/api/admin/eliminar-usuario', 'DELETE', { idUsuario });
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Usuario eliminado con éxito.') : res.msg, res.ok);
    if (res.ok) cargarUsuarios();
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