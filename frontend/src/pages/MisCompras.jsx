import { Link } from 'react-router-dom';
import useRecurso from '../hooks/useRecurso.js';
import { formatearPrecio } from '../utils/formatos.js';
import { formatearFechaHora } from '../utils/citas.js';

const ESTADO = {
  PENDIENTE: { texto: 'Pendiente', clase: 'pendiente' },
  CONFIRMADO: { texto: 'Confirmado', clase: 'confirmada' },
  ENTREGADO: { texto: 'Entregado', clase: 'confirmada' },
  CANCELADO: { texto: 'Cancelado', clase: 'rechazada' },
};

// Historial de compras del cliente: GET /pedidos/mis-pedidos.
// El backend devuelve solo los pedidos del usuario que viene en el JWT.
export default function MisCompras() {
  const { datos: pedidos, cargando, error } = useRecurso('/pedidos/mis-pedidos');

  const totalGastado = pedidos
    .filter((p) => p.estado === 'CONFIRMADO' || p.estado === 'ENTREGADO')
    .reduce((suma, p) => suma + p.total, 0);

  return (
    <section className="mis-citas-pagina">
      <div className="mis-citas-contenedor">
        <header className="mis-citas-encabezado">
          <h1>Mis Compras</h1>
          <p>Aquí puedes revisar los pedidos que has realizado en la tienda.</p>
        </header>

        <div className="mis-citas-estadisticas">
          <div className="mis-citas-estadistica">
            <span>Pedidos</span>
            <strong>{pedidos.length}</strong>
          </div>

          <div className="mis-citas-estadistica">
            <span>Total comprado</span>
            <strong>{formatearPrecio(totalGastado)}</strong>
          </div>
        </div>

        <div className="mis-citas-historial">
          <div className="mis-citas-historial-header">
            <h2>Historial de compras</h2>

            <Link to="/#tienda" className="mis-citas-nueva">
              Ir a la tienda
            </Link>
          </div>

          <div className="mis-citas-tabla-contenedor">
            <table className="mis-citas-tabla">
              <thead>
                <tr>
                  <th>N°</th>
                  <th>Fecha</th>
                  <th>Productos</th>
                  <th>Total</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody>
                {pedidos.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="mis-citas-vacio">
                      {error || (cargando ? 'Cargando…' : 'Todavía no tienes compras registradas.')}
                    </td>
                  </tr>
                ) : (
                  pedidos.map((pedido) => (
                    <tr key={pedido.id}>
                      <td>{pedido.id}</td>
                      <td>{formatearFechaHora(pedido.creadoEn)}</td>
                      <td>
                        {pedido.items.length === 0
                          ? '-'
                          : pedido.items.map((l) => `${l.nombre} x${l.cantidad}`).join(', ')}
                      </td>
                      <td>{formatearPrecio(pedido.total)}</td>
                      <td>
                        <span className={`estado estado-${ESTADO[pedido.estado].clase}`}>
                          {ESTADO[pedido.estado].texto}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  );
}
