import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import RutaProtegida from './RutaProtegida.jsx';

vi.mock('../context/AuthContext.jsx', () => ({ useAuth: vi.fn() }));
import { useAuth } from '../context/AuthContext.jsx';

// Monta una mini aplicación con una pantalla restringida en /admin/usuarios
function montar(roles) {
  return render(
    <MemoryRouter initialEntries={['/admin/usuarios']}>
      <Routes>
        <Route
          path="/admin/usuarios"
          element={
            <RutaProtegida roles={roles}>
              <h1>Gestión de usuarios</h1>
            </RutaProtegida>
          }
        />
        <Route path="/login" element={<h1>Pantalla de login</h1>} />
        <Route path="/admin" element={<h1>Inicio del panel</h1>} />
        <Route path="/" element={<h1>Tienda</h1>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('Pruebas en RutaProtegida (restricción por rol)', () => {
  it('sin sesión redirige al login', () => {
    useAuth.mockReturnValue({ usuario: null, cargando: false });
    montar(['ADMIN']);
    expect(screen.getByText('Pantalla de login')).toBeInTheDocument();
    expect(screen.queryByText('Gestión de usuarios')).not.toBeInTheDocument();
  });

  it('el ADMIN entra a una pantalla exclusiva de administrador', () => {
    useAuth.mockReturnValue({ usuario: { rol: 'ADMIN' }, cargando: false });
    montar(['ADMIN']);
    expect(screen.getByText('Gestión de usuarios')).toBeInTheDocument();
  });

  it('el VENDEDOR no entra a pantallas de administrador y vuelve a su panel', () => {
    useAuth.mockReturnValue({ usuario: { rol: 'VENDEDOR' }, cargando: false });
    montar(['ADMIN']);
    expect(screen.queryByText('Gestión de usuarios')).not.toBeInTheDocument();
    expect(screen.getByText('Inicio del panel')).toBeInTheDocument();
  });

  it('el CLIENTE no entra al panel y vuelve a la tienda', () => {
    useAuth.mockReturnValue({ usuario: { rol: 'CLIENTE' }, cargando: false });
    montar(['ADMIN', 'VENDEDOR']);
    expect(screen.queryByText('Gestión de usuarios')).not.toBeInTheDocument();
    expect(screen.getByText('Tienda')).toBeInTheDocument();
  });

  it('mientras se recupera la sesión no redirige (evita expulsar al recargar)', () => {
    useAuth.mockReturnValue({ usuario: null, cargando: true });
    montar(['ADMIN']);
    expect(screen.getByRole('status')).toHaveTextContent('Cargando sesión');
    expect(screen.queryByText('Pantalla de login')).not.toBeInTheDocument();
  });
});
