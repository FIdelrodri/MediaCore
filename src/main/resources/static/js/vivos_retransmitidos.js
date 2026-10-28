// Vista pública: no exige sesión. La sesión solo se consulta para mostrar "Ir al Panel".
let vivos = [];

document.addEventListener('DOMContentLoaded', iniciar);

function iniciar() {
    document.getElementById('buscador').addEventListener('input', filtrarVivos);
    mostrarBotonPanel();
    cargarVivos();
}

// El botón dice "Iniciar sesión" (hacia /login) y pasa a "Ir al Panel" si hay usuario logueado con rol
async function mostrarBotonPanel() {
    try {
        const res = await fetch('/api/auth/sesion-actual');
        if (!res.ok) return;
        const data = await res.json();
        if (data.autenticado && data.idRol) {
            const boton = document.getElementById('btnIrPanel');
            boton.textContent = 'Ir al Panel';
            boton.href = '/panel';
        }
    } catch (err) {
        // Sin sesión o sin conexión: el botón queda como "Iniciar sesión"
    }
}

async function cargarVivos() {
    const tbody = document.getElementById('lista_vivos');
    tbody.innerHTML = '<p class="sin_resultados">Cargando vivos...</p>';

    try {
        const res = await fetch('/api/vivos/retransmitidos');
        if (!res.ok) throw new Error();
        vivos = await res.json();
        filtrarVivos();
    } catch (err) {
        tbody.innerHTML = '';
        mostrarAlerta('No se pudieron cargar los vivos retransmitidos.', false);
    }
}

// Búsqueda por JS sobre la lista ya cargada
function filtrarVivos() {
    const texto = document.getElementById('buscador').value.trim().toLowerCase();
    const filtrados = vivos.filter(v =>
        (v.titulo || '').toLowerCase().includes(texto) ||
        (v.descripcion || '').toLowerCase().includes(texto)
    );
    renderizarVivos(filtrados);
}

function renderizarVivos(lista) {
    const tbody = document.getElementById('lista_vivos');
    if (lista.length === 0) {
        tbody.innerHTML = '<p class="sin_resultados">No hay vivos retransmitidos para mostrar.</p>';
        return;
    }
    tbody.innerHTML = lista.map(v => `
        <article class="fila_vivo">
            <span class="col_titulo">${escapar(v.titulo)}</span>
            <span class="col_duracion">${formatearDuracion(v.duracion)}</span>
            <span class="col_descripcion">${escapar(v.descripcion) || '-'}</span>
        </article>
    `).join('');
}

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
    div.textContent = mensaje;
    div.classList.toggle('exito', esExito);
    div.classList.toggle('error', !esExito);
}
