// Roles con permiso de alta, edición y baja de programas
const ROLES_CONTENIDO = [1, 2, 6];

let programas = [];
let idRolActual = null;

document.addEventListener('DOMContentLoaded', iniciar);

async function iniciar() {
    const sesion = await apiFetch('/api/auth/sesion-actual');
    if (!sesion.ok || !sesion.data.autenticado) {
        window.location.href = '/login';
        return;
    }
    idRolActual = sesion.data.idRol;

    if (ROLES_CONTENIDO.includes(idRolActual)) {
        document.getElementById('formPrograma').style.display = 'block';
    }

    document.getElementById('formPrograma').addEventListener('submit', guardarPrograma);
    document.getElementById('btnCancelar').addEventListener('click', limpiarFormulario);
    document.getElementById('buscador').addEventListener('input', filtrarProgramas);

    cargarProgramas();
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

async function cargarProgramas() {
    const tbody = document.getElementById('lista_programas');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Cargando programas...</td></tr>';

    const res = await apiFetch('/api/programas');
    if (res.redirected) return;

    if (!res.ok) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Error al cargar la lista de programas.</td></tr>';
        mostrarAlerta(res.msg, false);
        return;
    }

    programas = res.data;
    filtrarProgramas();
}

// Filtra en el navegador la lista ya cargada
function filtrarProgramas() {
    const texto = document.getElementById('buscador').value.trim().toLowerCase();

    const filtrados = programas.filter(p =>
        (p.nombre || '').toLowerCase().includes(texto) ||
        (p.genero || '').toLowerCase().includes(texto) ||
        (p.descripcion || '').toLowerCase().includes(texto)
    );

    renderizarTabla(filtrados);
}

function renderizarTabla(lista) {
    const tbody = document.getElementById('lista_programas');

    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay programas para mostrar.</td></tr>';
        return;
    }

    const puedeEditar = ROLES_CONTENIDO.includes(idRolActual);

    tbody.innerHTML = lista.map(p => {
        let acciones = '';
        if (puedeEditar) {
            acciones += `<button class="btn_editar" onclick="editarPrograma(${p.idPrograma})">Editar</button>
                         <button class="btn_eliminar" onclick="eliminarPrograma(${p.idPrograma})">Eliminar</button>`;
        }

        return `
            <tr>
                <td>${p.idPrograma}</td>
                <td><strong>${escapar(p.nombre)}</strong></td>
                <td>${escapar(p.genero) || '-'}</td>
                <td>${formatearDuracion(p.duracion)}</td>
                <td>${escapar(p.descripcion) || '-'}</td>
                <td>${acciones || '-'}</td>
            </tr>
        `;
    }).join('');
}

async function guardarPrograma(evento) {
    evento.preventDefault();

    const idPrograma = document.getElementById('idPrograma').value;
    const datos = {
        nombre: document.getElementById('nombre').value.trim(),
        genero: document.getElementById('genero').value.trim(),
        descripcion: document.getElementById('descripcion').value.trim(),
        duracion: parseInt(document.getElementById('duracion').value, 10)
    };

    const esEdicion = idPrograma !== '';
    const url = esEdicion ? `/api/programas/${idPrograma}` : '/api/programas';
    const res = await apiFetch(url, esEdicion ? 'PUT' : 'POST', datos);
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Programa guardado correctamente.') : res.msg, res.ok);
    if (res.ok) {
        limpiarFormulario();
        cargarProgramas();
    }
}

function editarPrograma(idPrograma) {
    const p = programas.find(x => x.idPrograma === idPrograma);
    if (!p) return;

    document.getElementById('idPrograma').value = p.idPrograma;
    document.getElementById('nombre').value = p.nombre || '';
    document.getElementById('genero').value = p.genero || '';
    document.getElementById('descripcion').value = p.descripcion || '';
    document.getElementById('duracion').value = p.duracion || '';

    document.getElementById('tituloForm').textContent = 'Editar programa';
    document.getElementById('btnGuardar').textContent = 'Guardar cambios';
    document.getElementById('btnCancelar').style.display = 'inline-block';
    document.getElementById('formPrograma').scrollIntoView({ behavior: 'smooth' });
}

function limpiarFormulario() {
    document.getElementById('formPrograma').reset();
    document.getElementById('idPrograma').value = '';
    document.getElementById('tituloForm').textContent = 'Nuevo programa';
    document.getElementById('btnGuardar').textContent = 'Crear programa';
    document.getElementById('btnCancelar').style.display = 'none';
}

async function eliminarPrograma(idPrograma) {
    const p = programas.find(x => x.idPrograma === idPrograma);
    if (!confirm(`¿Seguro que deseas eliminar el programa "${p ? p.nombre : idPrograma}"?`)) return;

    const res = await apiFetch(`/api/programas/${idPrograma}`, 'DELETE');
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Programa eliminado con éxito.') : res.msg, res.ok);
    if (res.ok) cargarProgramas();
}

// La duración se guarda en segundos, se muestra como h:mm:ss
function formatearDuracion(segundos) {
    if (!segundos && segundos !== 0) return '-';
    const h = Math.floor(segundos / 3600);
    const m = Math.floor((segundos % 3600) / 60);
    const s = segundos % 60;
    const mm = String(m).padStart(2, '0');
    const ss = String(s).padStart(2, '0');
    return h > 0 ? `${h}:${mm}:${ss}` : `${m}:${ss}`;
}

function escapar(texto) {
    if (texto === null || texto === undefined) return '';
    return String(texto)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
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
