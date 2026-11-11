import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useTienda } from '../context/TiendaContext.jsx';
import { formatearPrecio } from '../utils/formatos.js';

export default function CarritoPanel({ abierto, alCerrar }) {
  const {
    carrito,
    total,
    buscarProducto,
    aumentarCantidad,
    disminuirCantidad,
    eliminarProducto,
    vaciarCarrito,
    finalizarCompra,
    comprando,
  } = useTienda();
  const { esCliente } = useAuth();

  
  useEffect(() => {
    document.body.style.overflow = abierto ? 'hidden' : '';
    return () => {
      document.body.style.overflow = '';
    };
  }, [abierto]);

  async function confirmarCompra() {
    // La compra se confirma en el backend: se espera su respuesta antes de cerrar el panel
    if (await finalizarCompra()) alCerrar();
  }

  return (
    <>
      <div id="carritoPanel" className={`carrito-panel ${abierto ? 'abierto' : ''}`}>
        <div className="carrito-header">
          <h2>🛒 Mi Carrito</h2>
          <button
            id="cerrarCarrito"
            className="cerrar-carrito"
            type="button"
            onClick={alCerrar}
          >
            ×
          </button>
        </div>

        <div id="carritoContenido" className="carrito-contenido">
          {carrito.length === 0 ? (
            <div className="carrito-vacio">
              <span className="icono-vacio">🛒</span>
              <p>Tu carrito está vacío</p>
              <p>Agrega productos para comenzar</p>
            </div>
          ) : (
            carrito.map((item) => {
              const enCatalogo = buscarProducto(item.id);
              return (
                <div className="item-carrito" key={item.id}>
                  <img
                    className="item-carrito-imagen"
                    src={item.imagen}
                    alt={item.nombre}
                  />

                  <div className="item-carrito-info">
                    <div className="item-carrito-nombre">{item.nombre}</div>
                    <div className="item-carrito-precio">
                      {formatearPrecio(item.precio)}
                    </div>
                    {enCatalogo && (
                      <div style={{ display: 'block', marginBottom: '6px' }}>
                        Stock disponible: {enCatalogo.stockActual}
                      </div>
                    )}
                    <div className="controles-cantidad">
                      <button
                        className="btn-cantidad"
                        type="button"
                        onClick={() => disminuirCantidad(item.id)}
                      >
                        -
                      </button>
                      <span className="cantidad">{item.cantidad}</span>
                      <button
                        className="btn-cantidad"
                        type="button"
                        onClick={() => aumentarCantidad(item.id)}
                      >
                        +
                      </button>
                    </div>
                  </div>
                  <button
                    className="btn-eliminar"
                    type="button"
                    onClick={() => eliminarProducto(item.id)}
                  >
                    Eliminar
                  </button>
                </div>
              );
            })
          )}
        </div>

        <div className="carrito-footer">
          <div className="carrito-total">
            <span>Total:</span>
            <strong id="carritoTotal">{formatearPrecio(total)}</strong>
          </div>
          <button
            id="finalizarCompra"
            className="btn-finalizar"
            type="button"
            onClick={confirmarCompra}
            disabled={comprando}
          >
            {comprando ? 'Procesando compra…' : 'Finalizar compra'}
          </button>
          <button
            id="vaciarCarrito"
            className="btn-vaciar"
            type="button"
            onClick={vaciarCarrito}
          >
            Vaciar carrito
          </button>
          {esCliente && (
            <Link
              to="/mis-compras"
              onClick={alCerrar}
              style={{ display: 'block', textAlign: 'center', marginTop: '12px' }}
            >
              Ver mis compras
            </Link>
          )}
        </div>
      </div>

      <div
        id="fondoCarrito"
        className={`fondo-carrito ${abierto ? 'activo' : ''}`}
        onClick={alCerrar}
      ></div>
    </>
  );
}