// Roles con permiso de alta, edición y baja de vivos
const ROLES_CONTENIDO = [1, 2, 3];

let vivos = [];
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
        document.getElementById('formVivo').style.display = 'block';
    }

    document.getElementById('formVivo').addEventListener('submit', guardarVivo);
    document.getElementById('btnCancelar').addEventListener('click', limpiarFormulario);
    document.getElementById('buscador').addEventListener('input', filtrarVivos);
    document.getElementById('filtroEstado').addEventListener('change', filtrarVivos);

    cargarVivos();
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

async function cargarVivos() {
    const tbody = document.getElementById('lista_vivos');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Cargando vivos...</td></tr>';

    const res = await apiFetch('/api/vivos');
    if (res.redirected) return;

    if (!res.ok) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Error al cargar la lista de vivos.</td></tr>';
        mostrarAlerta(res.msg, false);
        return;
    }

    vivos = res.data;
    filtrarVivos();
}

// Filtra en el navegador la lista ya cargada, por texto y por estado
function filtrarVivos() {
    const texto = document.getElementById('buscador').value.trim().toLowerCase();
    const estado = document.getElementById('filtroEstado').value;

    const filtrados = vivos.filter(v => {
        const coincideTexto =
            (v.titulo || '').toLowerCase().includes(texto) ||
            (v.descripcion || '').toLowerCase().includes(texto);
        const coincideEstado =
            estado === 'todos' ||
            (estado === 'retransmitidos' && v.retrasmitido) ||
            (estado === 'pendientes' && !v.retrasmitido);
        return coincideTexto && coincideEstado;
    });

    renderizarTabla(filtrados);
}

function renderizarTabla(lista) {
    const tbody = document.getElementById('lista_vivos');

    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay vivos para mostrar.</td></tr>';
        return;
    }

    const puedeEditar = ROLES_CONTENIDO.includes(idRolActual);

    tbody.innerHTML = lista.map(v => {
        let acciones = '';
        if (puedeEditar) {
            acciones += `<button class="btn_editar" onclick="editarVivo(${v.idEnvivo})">Editar</button>
                         <button class="btn_eliminar" onclick="eliminarVivo(${v.idEnvivo})">Eliminar</button>`;
        }

        const estado = v.retrasmitido
            ? '<span class="estado retransmitido">Retransmitido</span>'
            : '<span class="estado pendiente">Pendiente</span>';

        // El estado de retransmitido lo actualiza la base de datos de forma automática,
        // por eso la vista solo lo muestra: las filas retransmitidas se ven atenuadas
        return `
            <tr class="${v.retrasmitido ? 'fila_retransmitida' : ''}">
                <td>${v.idEnvivo}</td>
                <td><strong>${escapar(v.titulo)}</strong></td>
                <td>${formatearDuracion(v.duracion)}</td>
                <td>${estado}</td>
                <td>${escapar(v.descripcion) || '-'}</td>
                <td>${acciones || '-'}</td>
            </tr>
        `;
    }).join('');
}

async function guardarVivo(evento) {
    evento.preventDefault();

    const idEnvivo = document.getElementById('idEnvivo').value;
    const datos = {
        titulo: document.getElementById('titulo').value.trim(),
        descripcion: document.getElementById('descripcion').value.trim(),
        duracion: parseInt(document.getElementById('duracion').value, 10)
    };

    const esEdicion = idEnvivo !== '';
    const url = esEdicion ? `/api/vivos/${idEnvivo}` : '/api/vivos';
    const res = await apiFetch(url, esEdicion ? 'PUT' : 'POST', datos);
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Vivo guardado correctamente.') : res.msg, res.ok);
    if (res.ok) {
        limpiarFormulario();
        cargarVivos();
    }
}

function editarVivo(idEnvivo) {
    const v = vivos.find(x => x.idEnvivo === idEnvivo);
    if (!v) return;

    document.getElementById('idEnvivo').value = v.idEnvivo;
    document.getElementById('titulo').value = v.titulo || '';
    document.getElementById('descripcion').value = v.descripcion || '';
    document.getElementById('duracion').value = v.duracion || '';

    // La base de datos no permite cambiar la duración de un vivo ya retransmitido
    const duracion = document.getElementById('duracion');
    duracion.disabled = !!v.retrasmitido;
    duracion.title = v.retrasmitido ? 'La duración de un vivo retransmitido no se puede modificar' : '';

    document.getElementById('tituloForm').textContent = v.retrasmitido
        ? 'Editar vivo (retransmitido: la duración no se puede modificar)'
        : 'Editar vivo';
    document.getElementById('btnGuardar').textContent = 'Guardar cambios';
    document.getElementById('btnCancelar').style.display = 'inline-block';
    document.getElementById('formVivo').scrollIntoView({ behavior: 'smooth' });
}

function limpiarFormulario() {
    document.getElementById('formVivo').reset();
    document.getElementById('idEnvivo').value = '';
    document.getElementById('duracion').disabled = false;
    document.getElementById('duracion').title = '';
    document.getElementById('tituloForm').textContent = 'Nuevo vivo';
    document.getElementById('btnGuardar').textContent = 'Crear vivo';
    document.getElementById('btnCancelar').style.display = 'none';
}

async function eliminarVivo(idEnvivo) {
    const v = vivos.find(x => x.idEnvivo === idEnvivo);
    if (!confirm(`¿Seguro que deseas eliminar el vivo "${v ? v.titulo : idEnvivo}"?`)) return;

    const res = await apiFetch(`/api/vivos/${idEnvivo}`, 'DELETE');
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Vivo eliminado con éxito.') : res.msg, res.ok);
    if (res.ok) cargarVivos();
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
