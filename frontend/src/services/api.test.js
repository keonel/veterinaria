import { describe, it, expect, beforeEach, vi } from 'vitest';

// Respuesta falsa de fetch
function respuesta(status, datos) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: async () => (datos === undefined ? '' : JSON.stringify(datos)),
  };
}

describe('Pruebas en el cliente HTTP api.js', () => {
  let modulo;

  beforeEach(async () => {
    localStorage.clear();
    vi.resetModules(); // cada prueba parte sin token en memoria
    vi.stubGlobal('fetch', vi.fn());
    modulo = await import('./api.js');
  });

  it('llama al Gateway con credenciales y sin Authorization si no hay sesión', async () => {
    fetch.mockResolvedValueOnce(respuesta(200, [{ id: 1 }]));

    const datos = await modulo.api('/productos');

    expect(datos).toEqual([{ id: 1 }]);
    const [url, opciones] = fetch.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/v1/productos');
    expect(opciones.credentials).toBe('include');
    expect(opciones.headers.Authorization).toBeUndefined();
  });

  it('tras el login envía el JWT en Authorization y no lo guarda en localStorage', async () => {
    fetch.mockResolvedValueOnce(respuesta(200, { accessToken: 'jwt-123', usuario: { rol: 'ADMIN' } }));
    await modulo.api('/auth/login', { method: 'POST', body: { correo: 'a', contrasena: 'b' } });

    fetch.mockResolvedValueOnce(respuesta(200, []));
    await modulo.api('/usuarios');

    expect(fetch.mock.calls[1][1].headers.Authorization).toBe('Bearer jwt-123');
    expect(JSON.stringify(localStorage)).not.toContain('jwt-123');
    expect(modulo.haySesionGuardada()).toBe(true);
  });

  it('convierte los errores del backend en ApiError con mensaje y campos', async () => {
    fetch.mockResolvedValueOnce(
      respuesta(400, { status: 400, mensaje: 'Hay datos inválidos', campos: { precio: 'Inválido' } })
    );

    await expect(modulo.api('/productos', { method: 'POST', body: {} })).rejects.toMatchObject({
      status: 400,
      message: 'Hay datos inválidos',
      campos: { precio: 'Inválido' },
    });
  });

  it('si el token venció (401) renueva la sesión y repite la solicitud', async () => {
    fetch
      .mockResolvedValueOnce(respuesta(401, { mensaje: 'Debes iniciar sesión' })) // solicitud original
      .mockResolvedValueOnce(respuesta(200, { accessToken: 'nuevo', usuario: {} })) // /auth/refresh
      .mockResolvedValueOnce(respuesta(200, [{ id: 9 }])); // reintento

    const datos = await modulo.api('/pedidos');

    expect(datos).toEqual([{ id: 9 }]);
    expect(fetch.mock.calls[1][0]).toBe('http://localhost:8080/api/v1/auth/refresh');
    expect(fetch.mock.calls[2][1].headers.Authorization).toBe('Bearer nuevo');
  });

  it('si el refresh también falla avisa que la sesión expiró', async () => {
    const alExpirar = vi.fn();
    window.addEventListener(modulo.EVENTO_SESION_EXPIRADA, alExpirar);
    fetch
      .mockResolvedValueOnce(respuesta(401, { mensaje: 'x' }))
      .mockResolvedValueOnce(respuesta(401, { mensaje: 'La sesión expiró' }));

    await expect(modulo.api('/pedidos')).rejects.toMatchObject({ status: 401 });

    expect(alExpirar).toHaveBeenCalledTimes(1);
    expect(modulo.haySesionGuardada()).toBe(false);
    window.removeEventListener(modulo.EVENTO_SESION_EXPIRADA, alExpirar);
  });

  it('un 401 en el login no intenta renovar: es una contraseña incorrecta', async () => {
    fetch.mockResolvedValueOnce(respuesta(401, { mensaje: 'Correo o contraseña incorrectos.' }));

    await expect(
      modulo.api('/auth/login', { method: 'POST', body: { correo: 'a', contrasena: 'b' } })
    ).rejects.toMatchObject({ status: 401, message: 'Correo o contraseña incorrectos.' });
    expect(fetch).toHaveBeenCalledTimes(1);
  });

  it('varias renovaciones simultáneas comparten una sola llamada a /auth/refresh', async () => {
    fetch.mockResolvedValue(respuesta(200, { accessToken: 't', usuario: {} }));

    await Promise.all([modulo.renovarSesion(), modulo.renovarSesion()]);

    expect(fetch).toHaveBeenCalledTimes(1);
  });

  it('informa un mensaje claro si el backend está apagado', async () => {
    fetch.mockRejectedValueOnce(new TypeError('Failed to fetch'));

    await expect(modulo.api('/productos')).rejects.toThrow('No se pudo conectar con el servidor');
  });
});
