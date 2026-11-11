import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import '../styles/style_admin.css';

const NOMBRE_ROL = { ADMIN: 'Administrador', VENDEDOR: 'Vendedor' };

export default function AdminLayout() {
  const { usuario, esAdmin, cerrarSesion } = useAuth();
  const navegar = useNavigate();

  async function salir() {
    if (!confirm('¿Quieres cerrar sesión?')) return;
    await cerrarSesion();
    navegar('/login');
  }

  return (
    <div className="admin-layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <img
            src="/assets/img/af0b5c1a02edd66e8cf9aa0a86a5b760.gif"
            alt="Logo Veterinaria San Marcos"
          />
          <h2>San Marcos</h2>
          <p>{NOMBRE_ROL[usuario?.rol] || 'Administración'}</p>
          <p>{usuario?.nombre}</p>
        </div>

        {/* El menú se arma según el rol: el VENDEDOR solo ve Productos y Pedidos.
            Las opciones restantes ni siquiera se dibujan para él. */}
        <nav className="sidebar-nav">
          {esAdmin && (
            <NavLink to="/admin" end>
              Dashboard
            </NavLink>
          )}
          {esAdmin && <NavLink to="/admin/citas">Citas</NavLink>}
          {esAdmin && <NavLink to="/admin/usuarios">Usuarios</NavLink>}
          <NavLink to="/admin/productos">Productos</NavLink>
          <NavLink to="/admin/pedidos">Pedidos</NavLink>
          {esAdmin && <NavLink to="/admin/mensajes">Mensajes</NavLink>}
        </nav>

        <button
          id="cerrarSesion"
          className="btn-cerrar-sesion"
          type="button"
          onClick={salir}
        >
          Cerrar sesión
        </button>
      </aside>

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
