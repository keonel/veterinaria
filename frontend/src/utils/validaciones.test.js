import { describe, it, expect } from 'vitest';
import * as validaciones from './validaciones.js';

describe('Pruebas en validaciones.js', () => {
  it('debe existir el módulo de validaciones', () => {
    expect(validaciones).toBeDefined();
  });
});