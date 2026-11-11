import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import useLocalStorage, { leerJSON } from './useLocalStorage.js';

describe('Pruebas en useLocalStorage y leerJSON', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('leerJSON retorna valorPorDefecto si la clave no existe', () => {
    const resultado = leerJSON('clave_inexistente', { inicial: true });
    expect(resultado).toEqual({ inicial: true });
  });

  it('leerJSON parsea un valor JSON válido correctamente', () => {
    localStorage.setItem('testKey', JSON.stringify({ ok: true }));
    const resultado = leerJSON('testKey', null);
    expect(resultado).toEqual({ ok: true });
  });

  it('leerJSON atrapa el error de JSON corrupto y retorna el valor por defecto', () => {
    localStorage.setItem('corruptKey', '{jsonInvalido:');
    const resultado = leerJSON('corruptKey', 'defecto');
    expect(resultado).toBe('defecto');
  });

  it('useLocalStorage inicializa estado y actualiza localStorage', () => {
    const { result } = renderHook(() => useLocalStorage('miClave', 'valorInicial'));

    expect(result.current[0]).toBe('valorInicial');

    act(() => {
      result.current[1]('nuevoValor');
    });

    expect(result.current[0]).toBe('nuevoValor');
    expect(localStorage.getItem('miClave')).toBe(JSON.stringify('nuevoValor'));
  });
});