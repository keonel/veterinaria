import { describe, it, expect } from 'vitest';
import { formatearPrecio } from './formatos.js';

describe('Pruebas en utilidad de formatos', () => {
  it('debe formatear correctamente un número a formato de moneda CLP', () => {
    const resultado = formatearPrecio(15000);
    expect(resultado).toContain('15');
    expect(resultado).toContain('000');
  });

  it('debe manejar el número 0 correctamente', () => {
    const resultado = formatearPrecio(0);
    expect(resultado).toContain('0');
  });
});