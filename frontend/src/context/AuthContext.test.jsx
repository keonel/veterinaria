import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext.jsx';

// Se reemplaza el cliente HTTP por un doble de prueba: no se llama al backend real
vi.mock('../services/api.js', () => ({
  api: vi.fn(),
  renovarSesion: vi.fn(),
  limpiarToken: vi.fn(),
  haySesionGuardada: vi.fn(() => false),
  EVENTO_SESION_EXPIRADA: 'sanmarcos:sesion-expirada',
}));

import { api, renovarSesion, limpiarToken, haySesionGuardada } from '../services/api.js';

const cliente = { id: 3, nombre: 'Camila', correo: 'cliente@gmail.com', rol: 'CLIENTE' };
const admin = { id: 1, nombre: 'Admin', correo: 'admin@sanmarcos.cl', rol: 'ADMIN' };
const vendedor = { id: 2, nombre: 'Valentina', correo: 'vendedor@sanmarcos.cl', rol: 'VENDEDOR' };

describe('Pruebas en AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    haySesionGuardada.mockReturnValue(false);
  });

  it('debe lanzar un error si useAuth se usa fuera de AuthProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow(
      'useAuth debe ser utilizado dentro de un AuthProvider'
    );
  });

  it('debe iniciar sin sesión y sin consultar al backend si nunca se inició sesión', () => {
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    expect(result.current.usuario).toBeNull();
    expect(result.current.cargando).toBe(false);
    expect(result.current.esCliente).toBe(false);
    expect(result.current.esAdmin).toBe(false);
    expect(renovarSesion).not.toHaveBeenCalled();
  });

  it('debe iniciar sesión con el backend y exponer el rol del usuario', async () => {
    api.mockResolvedValueOnce({ accessToken: 'jwt', usuario: cliente });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => {
      await result.current.iniciarSesion('cliente@gmail.com', 'Cliente123');
    });

    expect(api).toHaveBeenCalledWith('/auth/login', {
      method: 'POST',
      body: { correo: 'cliente@gmail.com', contrasena: 'Cliente123' },
    });
    expect(result.current.usuario).toEqual(cliente);
    expect(result.current.esCliente).toBe(true);
    expect(result.current.esAdmin).toBe(false);
    expect(result.current.tieneRol('ADMIN', 'VENDEDOR')).toBe(false);
  });

  it('no debe guardar el token ni los datos del usuario en localStorage', async () => {
    api.mockResolvedValueOnce({ accessToken: 'jwt-secreto', usuario: admin });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => {
      await result.current.iniciarSesion('admin@sanmarcos.cl', 'Admin1234');
    });

    expect(result.current.esAdmin).toBe(true);
    expect(JSON.stringify(localStorage)).not.toContain('jwt-secreto');
    expect(localStorage.getItem('usuarioActual')).toBeNull();
    expect(localStorage.getItem('tipoUsuario')).toBeNull();
  });

  it('debe recuperar la sesión al recargar la página usando el refresh', async () => {
    haySesionGuardada.mockReturnValue(true);
    renovarSesion.mockResolvedValueOnce({ accessToken: 'nuevo', usuario: vendedor });

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    expect(result.current.cargando).toBe(true);

    await waitFor(() => expect(result.current.cargando).toBe(false));
    expect(result.current.usuario).toEqual(vendedor);
    expect(result.current.esVendedor).toBe(true);
    expect(result.current.tieneRol('ADMIN', 'VENDEDOR')).toBe(true);
  });

  it('debe quedar sin sesión si el refresh falla', async () => {
    haySesionGuardada.mockReturnValue(true);
    renovarSesion.mockRejectedValueOnce(new Error('expirada'));

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await waitFor(() => expect(result.current.cargando).toBe(false));
    expect(result.current.usuario).toBeNull();
  });

  it('debe actualizar los datos del usuario', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    act(() => {
      result.current.actualizarUsuario({ ...cliente, nombre: 'Camila Editada' });
    });

    expect(result.current.usuario.nombre).toBe('Camila Editada');
  });

  it('debe cerrar sesión en el backend y limpiar el token', async () => {
    api.mockResolvedValueOnce({ accessToken: 'jwt', usuario: cliente });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => {
      await result.current.iniciarSesion('cliente@gmail.com', 'Cliente123');
    });

    api.mockResolvedValueOnce(null);
    await act(async () => {
      await result.current.cerrarSesion();
    });

    expect(api).toHaveBeenLastCalledWith('/auth/logout', { method: 'POST' });
    expect(limpiarToken).toHaveBeenCalled();
    expect(result.current.usuario).toBeNull();
    expect(result.current.rol).toBeNull();
  });

  it('debe cerrar la sesión local cuando el backend avisa que expiró', async () => {
    api.mockResolvedValueOnce({ accessToken: 'jwt', usuario: cliente });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => {
      await result.current.iniciarSesion('cliente@gmail.com', 'Cliente123');
    });

    act(() => {
      window.dispatchEvent(new Event('sanmarcos:sesion-expirada'));
    });

    expect(result.current.usuario).toBeNull();
  });
});
