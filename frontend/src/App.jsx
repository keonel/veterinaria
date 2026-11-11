import { Navigate, Routes, Route } from 'react-router-dom';
import NotFound from './pages/NotFound.jsx';
// Layouts y Protecciones
import PublicLayout from './components/PublicLayout.jsx';
import RutaProtegida from './components/RutaProtegida.jsx';
import AdminLayout from './components/AdminLayout.jsx';
import { useAuth } from './context/AuthContext.jsx';

// Páginas Públicas y de Clientes
import Inicio from './pages/Inicio.jsx';
import Login from './pages/Login.jsx';
import Registro from './pages/Registro.jsx';
import SolicitarCita from './pages/SolicitarCita.jsx';
import MisCitas from './pages/MisCitas.jsx';
import MisCompras from './pages/MisCompras.jsx';
import MiPerfil from './pages/MiPerfil.jsx';
import Contacto from './pages/Contacto.jsx';
import AcercaDe from './pages/AcercaDe.jsx';

// Páginas de Administración
import Dashboard from './pages/admin/Dashboard.jsx';
import AdminCitas from './pages/admin/AdminCitas.jsx';
import AdminUsuarios from './pages/admin/AdminUsuarios.jsx';
import AdminProductos from './pages/admin/AdminProductos.jsx';
import AdminPedidos from './pages/admin/AdminPedidos.jsx';
import AdminMensajes from './pages/admin/AdminMensajes.jsx';

import './styles/style.css';
//import './styles/ajustes_visuales_san_marcos.css';

// Roles del sistema (los mismos que entrega el backend en el JWT)
const SOLO_CLIENTE = ['CLIENTE'];
const SOLO_ADMIN = ['ADMIN'];
const ADMIN_Y_VENDEDOR = ['ADMIN', 'VENDEDOR'];

// Página inicial del panel: el administrador ve el Dashboard;
// el vendedor no tiene Dashboard, así que entra directo a Productos.
function InicioPanel() {
  const { esAdmin } = useAuth();
  return esAdmin ? <Dashboard /> : <Navigate to="/admin/productos" replace />;
}

export default function App() {
  return (
    <Routes>
      {/* Rama Pública y de Clientes */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Inicio />} />
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Registro />} />
        <Route path="/contacto" element={<Contacto />} />
        <Route path="/acerca-de" element={<AcercaDe />} />
        <Route
          path="/solicitar-cita"
          element={
            <RutaProtegida roles={SOLO_CLIENTE}>
              <SolicitarCita />
            </RutaProtegida>
          }
        />
        <Route
          path="/mis-citas"
          element={
            <RutaProtegida roles={SOLO_CLIENTE}>
              <MisCitas />
            </RutaProtegida>
          }
        />
        <Route
          path="/mis-compras"
          element={
            <RutaProtegida roles={SOLO_CLIENTE}>
              <MisCompras />
            </RutaProtegida>
          }
        />
        <Route
          path="/mi-perfil"
          element={
            <RutaProtegida roles={SOLO_CLIENTE}>
              <MiPerfil />
            </RutaProtegida>
          }
        />
        <Route path="*" element={<NotFound />} />
      </Route>

      {/* Rama de Administración: entran ADMIN y VENDEDOR, pero cada pantalla define su rol */}
      <Route
        path="/admin"
        element={
          <RutaProtegida roles={ADMIN_Y_VENDEDOR}>
            <AdminLayout />
          </RutaProtegida>
        }
      >
        <Route index element={<InicioPanel />} />

        {/* El vendedor solo puede ver productos y pedidos */}
        <Route path="productos" element={<AdminProductos />} />
        <Route path="pedidos" element={<AdminPedidos />} />

        {/* Exclusivas del administrador */}
        <Route
          path="citas"
          element={
            <RutaProtegida roles={SOLO_ADMIN}>
              <AdminCitas />
            </RutaProtegida>
          }
        />
        <Route
          path="usuarios"
          element={
            <RutaProtegida roles={SOLO_ADMIN}>
              <AdminUsuarios />
            </RutaProtegida>
          }
        />
        <Route
          path="mensajes"
          element={
            <RutaProtegida roles={SOLO_ADMIN}>
              <AdminMensajes />
            </RutaProtegida>
          }
        />
      </Route>
    </Routes>
  );
}
