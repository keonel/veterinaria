import { describe, it, expect } from 'vitest';
import * as citasUtils from './citas.js';

describe('Pruebas en citas.js', () => {
  it('debe cargar las utilidades de citas', () => {
    expect(citasUtils).toBeDefined();
  });
});