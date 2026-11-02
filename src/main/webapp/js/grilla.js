// Roles que pueden modificar la grilla, vaciarla o mantener bloques
const ROLES_PLANILLA = [1, 2, 5];
const ROLES_ADMIN = [1, 2];
const ROLES_MANTENIMIENTO = [1, 6];
const ROLES_AUDIO = [1, 2, 4];       // pueden cambiar el audio (1 a 100) de la grilla
const ROLES_BLOQUES = [1, 2, 5, 6];  // roles a los que la API deja consultar los bloques

let rol = null;
let bloques = [];
let grilla = [];
let noUsados = [];

document.addEventListener('DOMContentLoaded', async () => {
    const sesion = await pedir('api/auth/sesion-actual');
    if (!sesion.ok || !sesion.data.autenticado) {
        window.location.href = 'login';
        return;
    }
    rol = sesion.data.idRol;

    // Los botones de edición solo se muestran a los roles que pueden usarlos
    document.getElementById('btnVaciar').hidden = !ROLES_ADMIN.includes(rol);
    document.getElementById('btnHuerfanos').hidden = !ROLES_MANTENIMIENTO.includes(rol);

    document.getElementById('buscador').addEventListener('input', mostrarBloques);
    document.getElementById('filtro').addEventListener('change', cargarBloques);
    document.getElementById('btnVaciar').addEventListener('click', () => accion('api/grilla', 'DELETE', '¿Vaciar toda la grilla?'));
    document.getElementById('btnHuerfanos').addEventListener('click', () => accion('api/bloques/huerfanos', 'DELETE', '¿Eliminar los bloques huérfanos?'));
    document.addEventListener('click', clicEnBoton);
    document.addEventListener('change', cambioDeAudio);

    // El administrador de audio no necesita la lista de bloques
    if (ROLES_BLOQUES.includes(rol)) {
        cargarBloques();
    } else {
        document.getElementById('panelBloques').hidden = true;
        document.querySelector('.columnas_grilla').style.gridTemplateColumns = '1fr';
    }
    cargarGrilla();
    setInterval(cargarGrilla, 3000); // la grilla avanza sola, se refresca cada 3 segundos
});

// ---------- Comunicación con la API ----------

async function pedir(url, metodo = 'GET', cuerpo = null) {
    const res = await fetch(url, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: cuerpo ? JSON.stringify(cuerpo) : null
    });
    if (res.status === 401) window.location.href = 'login';
    if (res.status === 403) window.location.href = 'panel';
    const data = await res.json().catch(() => ({}));
    return { ok: res.ok, data };
}

// Hace una modificación (con confirmación opcional), muestra el mensaje y recarga la grilla
async function accion(url, metodo, confirmar, cuerpo) {
    if (confirmar && !confirm(confirmar)) return;
    const res = await pedir(url, metodo, cuerpo);
    const div = document.getElementById('mensaje');
    div.textContent = res.data.mensaje || 'No se pudo completar la acción.';
    div.className = 'Alerta ' + (res.ok ? 'exito' : 'error');
    cargarGrilla();
}

// ---------- Carga de datos ----------

async function cargarBloques() {
    const soloNoUsados = document.getElementById('filtro').value === 'no_usados';
    const res = await pedir(soloNoUsados ? 'api/bloques/no-usados' : 'api/bloques');
    bloques = res.ok ? res.data : [];
    mostrarBloques();
}

async function cargarGrilla() {
    if (document.activeElement.classList.contains('audio')) return; // no pisar lo que se está escribiendo
    const [lista, total, aire, historial] = await Promise.all([
        pedir('api/grilla'),
        pedir('api/grilla/duracion-total'),
        pedir('api/grilla/en-aire'),
        pedir('api/grilla/historial?limite=10')
    ]);
    grilla = lista.ok ? lista.data : [];
    mostrarAudio(grilla.length ? grilla[0].audio : undefined);
    mostrarAire(aire.ok ? aire.data : null, total.data);
    mostrarCola();
    mostrarHistorial(historial.ok ? historial.data : []);
}

// ---------- Dibujo de la pantalla ----------

function mostrarBloques() {
    const texto = document.getElementById('buscador').value.toLowerCase();
    const editar = ROLES_PLANILLA.includes(rol);
    const mantener = ROLES_MANTENIMIENTO.includes(rol);

    document.getElementById('listaBloques').innerHTML = bloques
        .filter(b => b.titulo.toLowerCase().includes(texto))
        .map(b => `
            <div class="bloque_item">
                <span class="tipo ${b.tipoContenido}">${b.tipoContenido}</span>
                <div class="titulo_bloque">${escapar(b.titulo)}</div>
                <div class="meta_bloque">Bloque #${b.idBloque} · ${formatear(b.duracion)}</div>
                <div class="acciones_bloque">
                    ${editar ? `<button class="btn_mini" data-accion="agregar" data-id="${b.idBloque}">Agregar al final</button>
                                <button class="btn_mini" data-accion="insertar" data-id="${b.idBloque}">Insertar en...</button>` : ''}
                    ${mantener ? `<button class="btn_mini secundario" data-accion="recalcular" data-id="${b.idBloque}">Recalcular</button>` : ''}
                </div>
            </div>`).join('') || '<p class="vacio">No hay bloques.</p>';
}

function mostrarAire(aire, total) {
    const editar = ROLES_PLANILLA.includes(rol);
    document.getElementById('resumenGrilla').textContent =
        `${total.cantidadBloques || 0} bloque(s) · total ${formatear(total.segundosTotales)} · restan ${formatear(total.segundosRestantes)}`;

    document.getElementById('enAire').innerHTML = !aire ? 'No hay ningún bloque al aire.' : `
        <div class="etiqueta_aire">AL AIRE</div>
        <div class="titulo_aire">${escapar(aire.titulo)}</div>
        <div class="tiempos_aire">${hora(aire.horaInicio)} → ${hora(aire.horaFin)} · restan ${formatear(aire.segundosRestantes)}</div>
        ${editar ? `<div class="acciones_aire">
            <button class="btn_mini" data-accion="avanzar">Terminar y avanzar</button>
            <button class="btn_mini" data-accion="saltar">Saltar bloque</button></div>` : ''}`;
}

// La posición 1 es la que está al aire, la cola empieza en la 2
function mostrarCola() {
    const editar = ROLES_PLANILLA.includes(rol);
    document.getElementById('listaGrilla').innerHTML = grilla
        .filter(p => p.orden > 1)
        .map(p => `
            <div class="fila_cola">
                <span class="orden">${p.orden}</span>
                <span class="titulo_fila"><span class="tipo ${p.tipoContenido}">${p.tipoContenido}</span> ${escapar(p.titulo)}</span>
                <span>${formatear(p.duracionSegundos)}</span>
                <span class="horario">${hora(p.inicioProyectado)} → ${hora(p.finProyectado)}</span>
                <span>Audio: ${p.audio}</span>
                <span class="acciones_fila">${editar ? `
                    <button class="btn_mini secundario" data-accion="mover" data-orden="${p.orden}">Mover a...</button>
                    <button class="btn_mini secundario" data-accion="intercambiar" data-orden="${p.orden}">Intercambiar con...</button>
                    <button class="btn_mini secundario" data-accion="duplicar" data-orden="${p.orden}">Duplicar</button>
                    <button class="btn_mini secundario" data-accion="reemplazar" data-orden="${p.orden}">Reemplazar</button>
                    <button class="btn_mini" data-accion="quitar" data-orden="${p.orden}">Quitar</button>` : ''}</span>
            </div>`).join('') || '<p class="vacio">No hay bloques en cola.</p>';
}

function mostrarHistorial(lista) {
    document.getElementById('listaHistorial').innerHTML = lista
        .map(e => `<div class="fila_historial">${escapar(e.titulo)} <span class="horario">${hora(e.horaInicio)} → ${hora(e.horaFinReal)}</span></div>`)
        .join('') || '<p class="vacio">Todavía no salió nada al aire.</p>';
}

// Un solo campo cambia el audio de toda la grilla (solo roles de audio); el resto ve el valor
function mostrarAudio(audio) {
    const caja = document.getElementById('cajaAudio');
    caja.hidden = audio === undefined;
    if (audio === undefined) return;
    caja.innerHTML = ROLES_AUDIO.includes(rol)
        ? `Audio de la grilla (1 a 100): <input type="number" class="audio" min="1" max="100" value="${audio}">`
        : `Audio de la grilla: ${audio}`;
}

// Al terminar de editar el campo se guarda el nuevo audio para todas las posiciones
function cambioDeAudio(e) {
    if (!e.target.classList.contains('audio')) return;
    accion('api/grilla/audio', 'PUT', null, { audio: Number(e.target.value) });
}

// ---------- Botones (un solo escuchador para todos) ----------

function clicEnBoton(e) {
    const boton = e.target.closest('[data-accion]');
    if (!boton) return;
    const id = boton.dataset.id;
    const orden = boton.dataset.orden;

    switch (boton.dataset.accion) {
        case 'agregar':     return accion('api/grilla/bloques/' + id, 'POST');
        case 'insertar':    return accion('api/grilla/posiciones/' + preguntar('¿En qué posición?') + '/bloques/' + id, 'POST');
        case 'avanzar':     return accion('api/grilla/avanzar', 'POST');
        case 'saltar':      return accion('api/grilla/saltar', 'POST', '¿Saltar el bloque que está al aire?');
        case 'quitar':      return accion('api/grilla/posiciones/' + orden, 'DELETE', '¿Quitar la posición ' + orden + '?');
        case 'duplicar':    return accion('api/grilla/posiciones/' + orden + '/duplicar', 'POST');
        case 'reemplazar':  return accion('api/grilla/posiciones/' + orden + '/bloques/' + preguntar('¿Número del bloque nuevo?'), 'PUT');
        case 'recalcular':  return accion('api/bloques/' + id + '/recalcular-duracion', 'PUT');
        case 'mover':       return accion('api/grilla/mover', 'PUT', null, { origen: orden, destino: preguntar('¿A qué posición?') });
        case 'intercambiar': return accion('api/grilla/intercambiar', 'PUT', null, { posicionA: orden, posicionB: preguntar('¿Con qué posición?') });
    }
}

function preguntar(texto) {
    return prompt(texto) || 0;
}

// ---------- Utilidades ----------

function hora(fecha) {
    return fecha ? fecha.substring(11, 19) : '-';
}

function formatear(segundos) {
    const h = Math.floor(segundos / 3600), m = Math.floor(segundos % 3600 / 60), s = segundos % 60;
    return (h ? h + ':' + String(m).padStart(2, '0') : m) + ':' + String(s).padStart(2, '0');
}

function escapar(texto) {
    return String(texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
