import { createContext, useContext, useEffect, useState } from 'react';
import {
  api,
  renovarSesion,
  limpiarToken,
  haySesionGuardada,
  EVENTO_SESION_EXPIRADA,
} from '../services/api.js';

const AuthContext = createContext(null);

// Gestión de sesión del frontend.
// - El usuario y su rol vienen SIEMPRE del backend (login o refresh), no de localStorage.
// - Al recargar la página, el access token en memoria se pierde: se recupera llamando a
//   /auth/refresh, que funciona gracias a la cookie HttpOnly. Así la sesión persiste.
export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  // cargando = todavía no sabemos si hay sesión. Las rutas protegidas esperan a que termine.
  const [cargando, setCargando] = useState(() => haySesionGuardada());

  useEffect(() => {
    let vigente = true;

    if (haySesionGuardada()) {
      renovarSesion()
        .then((sesion) => {
          if (vigente) setUsuario(sesion.usuario);
        })
        .catch(() => {
          if (vigente) setUsuario(null);
        })
        .finally(() => {
          if (vigente) setCargando(false);
        });
    }

    // api.js avisa cuando el refresh token ya no sirve (venció o fue revocado)
    const alExpirar = () => setUsuario(null);
    window.addEventListener(EVENTO_SESION_EXPIRADA, alExpirar);

    return () => {
      vigente = false;
      window.removeEventListener(EVENTO_SESION_EXPIRADA, alExpirar);
    };
  }, []);

  async function iniciarSesion(correo, contrasena) {
    const sesion = await api('/auth/login', {
      method: 'POST',
      body: { correo, contrasena },
    });
    setUsuario(sesion.usuario);
    return sesion.usuario;
  }

  async function cerrarSesion() {
    try {
      // El backend revoca el refresh token y borra la cookie
      await api('/auth/logout', { method: 'POST' });
    } catch {
      // aunque falle la red, se cierra la sesión local
    }
    limpiarToken();
    setUsuario(null);
  }

  function actualizarUsuario(datosUsuario) {
    setUsuario(datosUsuario);
  }

  const rol = usuario?.rol || null;
  const esAdmin = rol === 'ADMIN';
  const esVendedor = rol === 'VENDEDOR';
  const esCliente = rol === 'CLIENTE';

  function tieneRol(...roles) {
    return Boolean(rol) && roles.includes(rol);
  }

  return (
    <AuthContext.Provider
      value={{
        usuario,
        rol,
        cargando,
        esAdmin,
        esVendedor,
        esCliente,
        tieneRol,
        iniciarSesion,
        cerrarSesion,
        actualizarUsuario,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider');
  }
  return context;
}
