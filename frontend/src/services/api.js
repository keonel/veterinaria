// Cliente HTTP central. Todas las páginas llaman al backend a través de api().
// El frontend solo conoce UNA dirección: la del API Gateway.
const BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1').replace(/\/$/, '');

// El access token (JWT) vive solo en memoria: no se guarda en localStorage ni sessionStorage,
// así un script malicioso (XSS) no puede leerlo desde el almacenamiento del navegador.
let accessToken = null;

// Promesa compartida: si varias solicitudes reciben 401 a la vez, se renueva una sola vez.
let renovando = null;

// Marca (no sensible) que indica que este navegador inició sesión alguna vez.
// Evita pedir /auth/refresh a visitantes que nunca han ingresado.
const MARCA_SESION = 'sm_sesion';

export const EVENTO_SESION_EXPIRADA = 'sanmarcos:sesion-expirada';

export class ApiError extends Error {
  constructor(status, data) {
    super(data?.mensaje || `Error HTTP ${status}`);
    this.name = 'ApiError';
    this.status = status;
    // Errores por campo que envía el backend cuando falla la validación (400)
    this.campos = data?.campos || {};
  }
}

async function leerCuerpo(respuesta) {
  const texto = await respuesta.text();
  if (!texto) return null;
  try {
    return JSON.parse(texto);
  } catch {
    return { mensaje: 'Respuesta inválida del servidor.' };
  }
}

export function haySesionGuardada() {
  try {
    return localStorage.getItem(MARCA_SESION) === '1';
  } catch {
    return false;
  }
}

function marcarSesion(activa) {
  try {
    if (activa) localStorage.setItem(MARCA_SESION, '1');
    else localStorage.removeItem(MARCA_SESION);
  } catch {
    // almacenamiento no disponible: la sesión igual funciona mientras la pestaña esté abierta
  }
}

export function limpiarToken() {
  accessToken = null;
  marcarSesion(false);
}

// Pide un access token nuevo usando la cookie HttpOnly de refresh.
// El navegador envía la cookie por credentials: 'include'; JavaScript nunca la ve.
export function renovarSesion() {
  if (!renovando) {
    renovando = (async () => {
      let respuesta;
      try {
        respuesta = await fetch(`${BASE}/auth/refresh`, {
          method: 'POST',
          credentials: 'include',
        });
      } catch {
        throw new Error('No se pudo conectar con el servidor.');
      }
      const cuerpo = await leerCuerpo(respuesta);
      if (!respuesta.ok) {
        limpiarToken();
        throw new ApiError(respuesta.status, cuerpo);
      }
      accessToken = cuerpo.accessToken;
      marcarSesion(true);
      return cuerpo;
    })().finally(() => {
      renovando = null;
    });
  }
  return renovando;
}

const SIN_REINTENTO = ['/auth/login', '/auth/registro', '/auth/refresh', '/auth/logout'];

// api('/productos')                                  -> GET
// api('/productos', { method: 'POST', body: {...} }) -> POST con JSON
export async function api(ruta, opciones = {}, reintentar = true) {
  const headers = { ...opciones.headers };
  if (opciones.body !== undefined) headers['Content-Type'] = 'application/json';
  if (accessToken) headers.Authorization = `Bearer ${accessToken}`;

  let respuesta;
  try {
    respuesta = await fetch(`${BASE}${ruta}`, {
      method: opciones.method || 'GET',
      credentials: 'include',
      headers,
      body: opciones.body === undefined ? undefined : JSON.stringify(opciones.body),
    });
  } catch {
    throw new Error('No se pudo conectar con el servidor. Revisa que el backend esté iniciado.');
  }

  // 401 = el access token venció (dura 15 minutos): se renueva y se repite la solicitud una vez.
  if (respuesta.status === 401 && reintentar && !SIN_REINTENTO.includes(ruta)) {
    try {
      await renovarSesion();
    } catch (error) {
      window.dispatchEvent(new Event(EVENTO_SESION_EXPIRADA));
      throw error instanceof ApiError
        ? new ApiError(401, { mensaje: 'Tu sesión expiró. Inicia sesión nuevamente.' })
        : error;
    }
    return api(ruta, opciones, false);
  }

  const cuerpo = await leerCuerpo(respuesta);
  if (!respuesta.ok) throw new ApiError(respuesta.status, cuerpo);

  if (ruta === '/auth/login') {
    accessToken = cuerpo.accessToken;
    marcarSesion(true);
  }
  return cuerpo;
}
