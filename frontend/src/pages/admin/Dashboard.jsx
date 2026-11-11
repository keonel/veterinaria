import { Link } from 'react-router-dom';
import { useTienda } from '../../context/TiendaContext.jsx';
import useRecurso from '../../hooks/useRecurso.js';
import { normalizarEstado, textoEstadoCita } from '../../utils/citas.js';

export default function Dashboard() {
  const { productos } = useTienda();

  // Cada contador viene de un microservicio distinto, todos a través del Gateway
  const { datos: citas } = useRecurso('/citas');
  const { datos: usuarios } = useRecurso('/usuarios');
  const { datos: pedidos } = useRecurso('/pedidos');

  const ultimasCitas = [...citas]
    .sort((a, b) =>
      `${b.fecha} ${b.hora}`.localeCompare(`${a.fecha} ${a.hora}`)
    )
    .slice(0, 5);

  return (
    <div className="admin-dashboard">

      
      <div className="admin-header">
        <div>
          <h1>Dashboard</h1>
          <p>Resumen general de Veterinaria San Marcos.</p>
        </div>
      </div>

      
      <section className="dashboard-resumen">

        <div className="dashboard-card">
          <span>📅 Citas</span>
          <strong id="totalCitas">{citas.length}</strong>
        </div>

        <div className="dashboard-card">
          <span>👥 Usuarios</span>
          <strong id="totalUsuarios">{usuarios.length}</strong>
        </div>

        <div className="dashboard-card">
          <span>🛍️ Productos</span>
          <strong id="totalProductos">{productos.length}</strong>
        </div>

        <div className="dashboard-card">
          <span>🧾 Pedidos</span>
          <strong id="totalPedidos">{pedidos.length}</strong>
        </div>

      </section>

      
      <div className="dashboard-contenido">

        
        <section className="dashboard-panel">

          <h2>Últimas citas registradas</h2>

          <div className="tabla-contenedor">
            <table className="dashboard-tabla">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Cliente</th>
                  <th>Mascota</th>
                  <th>Fecha</th>
                  <th>Hora</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody id="tablaCitas">
                {ultimasCitas.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="dashboard-vacio">
                      No hay citas registradas en el sistema.
                    </td>
                  </tr>
                ) : (
                  ultimasCitas.map((c) => (
                    <tr key={c.codigo}>
                      <td>{c.codigo}</td>
                      <td>{c.cliente}</td>
                      <td>{c.mascota}</td>
                      <td>{c.fecha}</td>
                      <td>{c.hora}</td>
                      <td>
                        <span
                          className={`estado-cita estado-${normalizarEstado(c.estado)}`}
                        >
                          {textoEstadoCita(c.estado)}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

        </section>

        
        <section className="dashboard-panel">

          <h2>Accesos rápidos</h2>

          <div className="accesos-rapidos">
            <Link to="/admin/citas" className="acceso-rapido">
              📅 Gestionar citas
            </Link>

            <Link to="/admin/usuarios" className="acceso-rapido">
              👥 Gestionar usuarios
            </Link>

            <Link to="/admin/productos" className="acceso-rapido">
              🛍️ Gestionar productos
            </Link>

            <Link to="/admin/pedidos" className="acceso-rapido">
              🧾 Ver pedidos
            </Link>

            <Link to="/admin/mensajes" className="acceso-rapido">
              ✉️ Ver mensajes
            </Link>
          </div>

        </section>

      </div>

    </div>
  );
}
