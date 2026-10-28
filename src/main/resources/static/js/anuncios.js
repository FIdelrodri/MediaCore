// Roles con permiso de alta, edición y baja de anuncios
const ROLES_CONTENIDO = [1, 2];

let anuncios = [];
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
        document.getElementById('formAnuncio').style.display = 'block';
    }

    document.getElementById('formAnuncio').addEventListener('submit', guardarAnuncio);
    document.getElementById('btnCancelar').addEventListener('click', limpiarFormulario);
    ['buscador', 'filtroEmpresa', 'duracionMin', 'duracionMax'].forEach(id => {
        document.getElementById(id).addEventListener('input', filtrarAnuncios);
    });

    cargarAnuncios();
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

async function cargarAnuncios() {
    const tbody = document.getElementById('lista_anuncios');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Cargando anuncios...</td></tr>';

    const res = await apiFetch('/api/anuncios');
    if (res.redirected) return;

    if (!res.ok) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Error al cargar la lista de anuncios.</td></tr>';
        mostrarAlerta(res.msg, false);
        return;
    }

    anuncios = res.data;
    cargarEmpresas();
    filtrarAnuncios();
}

// Arma el selector de empresas con las que existen en la lista cargada
function cargarEmpresas() {
    const select = document.getElementById('filtroEmpresa');
    const seleccionada = select.value;

    const empresas = [...new Set(anuncios.map(a => a.empresa).filter(e => e))]
        .sort((a, b) => a.localeCompare(b));

    select.innerHTML = '<option value="">Todas las empresas</option>' +
        empresas.map(e => `<option value="${escapar(e)}">${escapar(e)}</option>`).join('');

    // Conserva la empresa elegida si todavía existe
    select.value = empresas.includes(seleccionada) ? seleccionada : '';
}

// Filtra en el navegador la lista ya cargada, por texto, empresa y duración
function filtrarAnuncios() {
    const texto = document.getElementById('buscador').value.trim().toLowerCase();
    const empresa = document.getElementById('filtroEmpresa').value;
    const min = parseInt(document.getElementById('duracionMin').value, 10);
    const max = parseInt(document.getElementById('duracionMax').value, 10);

    const filtrados = anuncios.filter(a =>
        ((a.nombre || '').toLowerCase().includes(texto) ||
         (a.genero || '').toLowerCase().includes(texto) ||
         (a.descripcion || '').toLowerCase().includes(texto)) &&
        (empresa === '' || a.empresa === empresa) &&
        (isNaN(min) || a.duracion >= min) &&
        (isNaN(max) || a.duracion <= max)
    );

    renderizarTabla(filtrados);
}

function renderizarTabla(lista) {
    const tbody = document.getElementById('lista_anuncios');

    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No hay anuncios para mostrar.</td></tr>';
        return;
    }

    const puedeEditar = ROLES_CONTENIDO.includes(idRolActual);

    tbody.innerHTML = lista.map(a => {
        let acciones = '';
        if (puedeEditar) {
            acciones += `<button class="btn_editar" onclick="editarAnuncio(${a.idAnuncio})">Editar</button>
                         <button class="btn_eliminar" onclick="eliminarAnuncio(${a.idAnuncio})">Eliminar</button>`;
        }

        return `
            <tr>
                <td>${a.idAnuncio}</td>
                <td><strong>${escapar(a.nombre)}</strong></td>
                <td>${escapar(a.empresa) || '-'}</td>
                <td>${escapar(a.genero) || '-'}</td>
                <td>${formatearDuracion(a.duracion)}</td>
                <td>${escapar(a.descripcion) || '-'}</td>
                <td>${acciones || '-'}</td>
            </tr>
        `;
    }).join('');
}

async function guardarAnuncio(evento) {
    evento.preventDefault();

    const idAnuncio = document.getElementById('idAnuncio').value;
    const datos = {
        nombre: document.getElementById('nombre').value.trim(),
        empresa: document.getElementById('empresa').value.trim(),
        genero: document.getElementById('genero').value.trim(),
        descripcion: document.getElementById('descripcion').value.trim(),
        duracion: parseInt(document.getElementById('duracion').value, 10)
    };

    const esEdicion = idAnuncio !== '';
    const url = esEdicion ? `/api/anuncios/${idAnuncio}` : '/api/anuncios';
    const res = await apiFetch(url, esEdicion ? 'PUT' : 'POST', datos);
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Anuncio guardado correctamente.') : res.msg, res.ok);
    if (res.ok) {
        limpiarFormulario();
        cargarAnuncios();
    }
}

function editarAnuncio(idAnuncio) {
    const a = anuncios.find(x => x.idAnuncio === idAnuncio);
    if (!a) return;

    document.getElementById('idAnuncio').value = a.idAnuncio;
    document.getElementById('nombre').value = a.nombre || '';
    document.getElementById('empresa').value = a.empresa || '';
    document.getElementById('genero').value = a.genero || '';
    document.getElementById('descripcion').value = a.descripcion || '';
    document.getElementById('duracion').value = a.duracion || '';

    document.getElementById('tituloForm').textContent = 'Editar anuncio';
    document.getElementById('btnGuardar').textContent = 'Guardar cambios';
    document.getElementById('btnCancelar').style.display = 'inline-block';
    document.getElementById('formAnuncio').scrollIntoView({ behavior: 'smooth' });
}

function limpiarFormulario() {
    document.getElementById('formAnuncio').reset();
    document.getElementById('idAnuncio').value = '';
    document.getElementById('tituloForm').textContent = 'Nuevo anuncio';
    document.getElementById('btnGuardar').textContent = 'Crear anuncio';
    document.getElementById('btnCancelar').style.display = 'none';
}

async function eliminarAnuncio(idAnuncio) {
    const a = anuncios.find(x => x.idAnuncio === idAnuncio);
    if (!confirm(`¿Seguro que deseas eliminar el anuncio "${a ? a.nombre : idAnuncio}"?`)) return;

    const res = await apiFetch(`/api/anuncios/${idAnuncio}`, 'DELETE');
    if (res.redirected) return;

    mostrarAlerta(res.ok ? (res.data.mensaje || 'Anuncio eliminado con éxito.') : res.msg, res.ok);
    if (res.ok) cargarAnuncios();
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
