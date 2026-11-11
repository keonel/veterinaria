import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// Restringe una pantalla a los roles indicados.
//   <RutaProtegida roles={['ADMIN']}> ... </RutaProtegida>
//
// Esto mejora la experiencia (no se muestran pantallas que el usuario no puede usar),
// pero la seguridad real está en el backend: aunque alguien fuerce la interfaz,
// la API responde 401/403 porque valida el JWT y el rol en cada solicitud.
export default function RutaProtegida({ roles, children }) {
  const { usuario, cargando } = useAuth();

  // Mientras se recupera la sesión (al recargar la página) no se redirige a nadie
  if (cargando) {
    return (
      <p style={{ padding: '60px 20px', textAlign: 'center' }} role="status">
        Cargando sesión…
      </p>
    );
  }

  // Sin sesión: al login
  if (!usuario) return <Navigate to="/login" replace />;

  // Con sesión pero sin el rol necesario: a su página de inicio
  if (!roles.includes(usuario.rol)) {
    const destino = usuario.rol === 'CLIENTE' ? '/' : '/admin';
    return <Navigate to={destino} replace />;
  }

  return children;
}
