import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import Buscador from './Buscador.jsx'; 
export default function Header({ cantidadCarrito = 0, alAbrirCarrito }) {
  const { esCliente, esAdmin, esVendedor, usuario, cerrarSesion } = useAuth();
  const [menuAbierto, setMenuAbierto] = useState(false);
  const navegar = useNavigate();

  const primerNombre = usuario?.nombre?.trim().split(' ')[0] || 'Usuario';
  // Administrador y vendedor ven el acceso al panel; el cliente no
  const vePanel = esAdmin || esVendedor;

  async function manejarCerrarSesion(evento) {
    evento.preventDefault();
    if (!confirm('¿Quieres cerrar sesión?')) return;
    await cerrarSesion();
    alert('Sesión cerrada correctamente.');
    navegar('/');
  }

  function botonSesion(clase) {
    if (usuario) {
      return (
        <a href="#" className={`${clase} usuario-logueado`} onClick={manejarCerrarSesion}>
          👤 Hola, {primerNombre}
        </a>
      );
    }
    return (
      <Link to="/login" className={clase}>
        👤 Iniciar sesión
      </Link>
    );
  }

  return (
    <header>
      <div className="header-container">
        <div className="logo-container">
          <img
            src="/assets/img/af0b5c1a02edd66e8cf9aa0a86a5b760.gif"
            alt="Logo Veterinaria San Marcos"
            className="logo-img"
          />
          <div className="logo">
            <Link to="/">Veterinaria San Marcos</Link>
          </div>
        </div>

        {<Buscador />}

        <button type="button" className="menu-icon sm-menu-button"
          aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
          aria-expanded={menuAbierto} aria-controls="sm-menu-movil"
          onClick={() => setMenuAbierto(v => !v)}>☰</button>

        <nav className="navbar sm-nav-escritorio">
          <ul className="listajiji">
            {esCliente && (
              <li>
                <NavLink to="/mi-perfil">Mi Perfil</NavLink>
              </li>
            )}
            <li>
              <NavLink to="/" end>
                Inicio
              </NavLink>
            </li>
            <li>
              <NavLink to="/solicitar-cita">Solicitar Cita</NavLink>
            </li>
            {esCliente && (
              <li>
                <NavLink to="/mis-citas">Mis Citas</NavLink>
              </li>
            )}
            {vePanel && (
              <li>
                <NavLink to="/admin">Panel</NavLink>
              </li>
            )}
            <li>
              <NavLink to="/contacto">Contáctanos</NavLink>
            </li>
            <li>
              <NavLink to="/acerca-de">Acerca de Nosotros</NavLink>
            </li>
            <li className="menu-sesion-movil">
              {botonSesion('iniciar-sesion-movil')}
            </li>
          </ul>
        </nav>

        {botonSesion('iniciar-sesion')}

        <button
          id="botonCarrito"
          className="carrito-icono"
          type="button"
          aria-label="Abrir carrito"
          onClick={alAbrirCarrito}
        >
          🛒
          <span id="contadorCarrito">{cantidadCarrito}</span>
        </button>
      </div>
      <nav id="sm-menu-movil" className={`sm-nav-movil${menuAbierto ? ' sm-abierto' : ''}`} aria-label="Navegación móvil">
        <ul>
          {esCliente && <li><NavLink to="/mi-perfil" onClick={() => setMenuAbierto(false)}>Mi Perfil</NavLink></li>}
          <li><NavLink to="/" end onClick={() => setMenuAbierto(false)}>Inicio</NavLink></li>
          <li><NavLink to="/solicitar-cita" onClick={() => setMenuAbierto(false)}>Solicitar Cita</NavLink></li>
          {esCliente && <li><NavLink to="/mis-citas" onClick={() => setMenuAbierto(false)}>Mis Citas</NavLink></li>}
          {esCliente && <li><NavLink to="/mis-compras" onClick={() => setMenuAbierto(false)}>Mis Compras</NavLink></li>}
          {vePanel && <li><NavLink to="/admin" onClick={() => setMenuAbierto(false)}>Panel</NavLink></li>}
          <li><NavLink to="/contacto" onClick={() => setMenuAbierto(false)}>Contáctanos</NavLink></li>
          <li><NavLink to="/acerca-de" onClick={() => setMenuAbierto(false)}>Acerca de Nosotros</NavLink></li>
          <li className="sm-sesion">{botonSesion('iniciar-sesion-movil')}</li>
        </ul>
      </nav>
    </header>
  );
}