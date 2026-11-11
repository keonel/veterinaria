import { useState } from 'react';
import useRecurso from '../../hooks/useRecurso.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { api } from '../../services/api.js';
import { formatearPrecio } from '../../utils/formatos.js';
import { formatearFechaHora } from '../../utils/citas.js';

const TEXTO_ESTADO = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

function claseEstado(estado) {
  if (estado === 'CANCELADO') return 'producto-estado inactivo';
  if (estado === 'PENDIENTE') return 'producto-estado critico';
  return 'producto-estado normal';
}

// Lista y detalle de pedidos.
// - ADMIN y VENDEDOR pueden ver esta pantalla (GET /pedidos y GET /pedidos/{id}).
// - Solo el ADMIN ve los botones para cambiar estado o eliminar.
export default function AdminPedidos() {
  const { esAdmin } = useAuth();
  const { datos: pedidos, cargando, error, recargar } = useRecurso('/pedidos');
  const [detalle, setDetalle] = useState(null);

  const confirmados = pedidos.filter((p) => p.estado === 'CONFIRMADO').length;
  const entregados = pedidos.filter((p) => p.estado === 'ENTREGADO').length;
  const totalVendido = pedidos
    .filter((p) => p.estado === 'CONFIRMADO' || p.estado === 'ENTREGADO')
    .reduce((suma, p) => suma + p.total, 0);

  async function verDetalle(pedido) {
    try {
      setDetalle(await api(`/pedidos/${pedido.id}`));
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (e) {
      window.alert(e.message);
    }
  }

  async function marcarEntregado(pedido) {
    try {
      const actualizado = await api(`/pedidos/${pedido.id}/estado`, {
        method: 'PUT',
        body: { estado: 'ENTREGADO' },
      });
      if (detalle?.id === pedido.id) setDetalle(actualizado);
      await recargar();
    } catch (e) {
      window.alert(e.message);
    }
  }

  async function eliminar(pedido) {
    if (!window.confirm(`¿Eliminar el pedido N° ${pedido.id}?`)) return;
    try {
      await api(`/pedidos/${pedido.id}`, { method: 'DELETE' });
      if (detalle?.id === pedido.id) setDetalle(null);
      await recargar();
    } catch (e) {
      window.alert(e.message);
    }
  }

  return (
    <div className="productos-admin">
      <div className="admin-header">
        <div>
          <h1>Pedidos</h1>
          <p>Compras realizadas en la tienda de Veterinaria San Marcos.</p>
        </div>

        <div className="productos-admin-etiqueta">
          {esAdmin ? 'Administrador' : 'Vendedor · solo lectura'}
        </div>
      </div>

      <section className="productos-resumen">
        <div className="producto-resumen-card">
          <h3>Total</h3>
          <strong>{pedidos.length}</strong>
          <p>Pedidos registrados</p>
        </div>

        <div className="producto-resumen-card">
          <h3>Confirmados</h3>
          <strong>{confirmados}</strong>
          <p>Por entregar</p>
        </div>

        <div className="producto-resumen-card">
          <h3>Entregados</h3>
          <strong>{entregados}</strong>
          <p>Pedidos finalizados</p>
        </div>

        <div className="producto-resumen-card">
          <h3>Ventas</h3>
          <strong>{formatearPrecio(totalVendido)}</strong>
          <p>Total vendido</p>
        </div>
      </section>

      {detalle && (
        <section className="productos-panel" aria-live="polite">
          <h2>Detalle del pedido N° {detalle.id}</h2>
          <p><strong>Cliente:</strong> {detalle.clienteNombre} ({detalle.clienteCorreo})</p>
          <p><strong>Fecha:</strong> {formatearFechaHora(detalle.creadoEn)}</p>
          <p>
            <strong>Estado:</strong>{' '}
            <span className={claseEstado(detalle.estado)}>{TEXTO_ESTADO[detalle.estado]}</span>
          </p>

          <div className="productos-tabla-contenedor">
            <table className="productos-tabla">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Producto</th>
                  <th>Precio de compra</th>
                  <th>Cantidad</th>
                  <th>Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {detalle.items.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="productos-vacio">
                      Este pedido no tiene productos confirmados.
                    </td>
                  </tr>
                ) : (
                  detalle.items.map((linea) => (
                    <tr key={linea.productoId}>
                      <td>{linea.codigo}</td>
                      <td>{linea.nombre}</td>
                      <td>{formatearPrecio(linea.precioUnitario)}</td>
                      <td>{linea.cantidad}</td>
                      <td>{formatearPrecio(linea.subtotal)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <p><strong>Total:</strong> {formatearPrecio(detalle.total)}</p>

          <button
            type="button"
            className="productos-btn-cancelar"
            onClick={() => setDetalle(null)}
          >
            Cerrar detalle
          </button>
        </section>
      )}

      <section className="productos-panel">
        <h2>Listado</h2>

        <div className="productos-tabla-contenedor">
          <table className="productos-tabla">
            <thead>
              <tr>
                <th>N°</th>
                <th>Fecha</th>
                <th>Cliente</th>
                <th>Productos</th>
                <th>Total</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>

            <tbody>
              {pedidos.length === 0 ? (
                <tr>
                  <td colSpan={7} className="productos-vacio">
                    {error || (cargando ? 'Cargando pedidos…' : 'No hay pedidos registrados.')}
                  </td>
                </tr>
              ) : (
                pedidos.map((p) => (
                  <tr key={p.id}>
                    <td>{p.id}</td>
                    <td>{formatearFechaHora(p.creadoEn)}</td>
                    <td>{p.clienteNombre}</td>
                    <td>{p.items.reduce((suma, l) => suma + l.cantidad, 0)}</td>
                    <td>{formatearPrecio(p.total)}</td>
                    <td>
                      <span className={claseEstado(p.estado)}>{TEXTO_ESTADO[p.estado]}</span>
                    </td>
                    <td>
                      <div className="productos-acciones">
                        <button
                          type="button"
                          className="productos-btn-estado"
                          onClick={() => verDetalle(p)}
                        >
                          Ver detalle
                        </button>

                        {/* Acciones de escritura: solo ADMIN */}
                        {esAdmin && p.estado === 'CONFIRMADO' && (
                          <button
                            type="button"
                            className="productos-btn-editar"
                            onClick={() => marcarEntregado(p)}
                          >
                            Marcar entregado
                          </button>
                        )}

                        {esAdmin && p.estado === 'CANCELADO' && (
                          <button
                            type="button"
                            className="productos-btn-eliminar"
                            onClick={() => eliminar(p)}
                          >
                            Eliminar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
