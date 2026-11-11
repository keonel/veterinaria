import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { api } from '../services/api.js';
import { useAuth } from './AuthContext.jsx';
import { leerJSON } from '../hooks/useLocalStorage.js';
import { formatearPrecio } from '../utils/formatos.js';

const TiendaContext = createContext(null);

// Estado de la tienda.
// - Los PRODUCTOS vienen del backend (GET /productos): precio y stock reales de la base de datos.
// - El CARRITO sigue en el navegador (localStorage): solo guarda qué productos y cuántos.
// - La COMPRA se confirma en el backend (POST /pedidos): él calcula precios y descuenta stock.
export function TiendaProvider({ children }) {
  const { esCliente } = useAuth();

  const [productos, setProductos] = useState([]);
  const [cargandoProductos, setCargandoProductos] = useState(true);
  const [errorProductos, setErrorProductos] = useState('');
  const [carrito, setCarrito] = useState(() => leerJSON('carritoVeterinaria', []));
  const [comprando, setComprando] = useState(false);

  // Clave de idempotencia de la compra en curso. Si la red falla y el usuario reintenta,
  // se envía la MISMA clave y el backend no crea un pedido duplicado.
  const claveCompra = useRef(null);

  // Carga inicial del catálogo desde el backend
  useEffect(() => {
    let vigente = true;
    api('/productos')
      .then((lista) => {
        if (!vigente) return;
        setProductos(lista);
        setErrorProductos('');
      })
      .catch((error) => {
        if (vigente) setErrorProductos(error.message);
      })
      .finally(() => {
        if (vigente) setCargandoProductos(false);
      });
    return () => {
      vigente = false;
    };
  }, []);

  // Vuelve a pedir el catálogo (después de una compra o de un cambio del administrador)
  const recargarProductos = useCallback(async () => {
    try {
      const lista = await api('/productos');
      setProductos(lista);
      setErrorProductos('');
      return lista;
    } catch (error) {
      setErrorProductos(error.message);
      return null;
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('carritoVeterinaria', JSON.stringify(carrito));
    // Si el carrito cambia, ya es otra compra: se generará una clave nueva
    claveCompra.current = null;
  }, [carrito]);

  const buscarProducto = (id) =>
    productos.find((p) => String(p.codigo) === String(id));

  const total = carrito.reduce((suma, i) => suma + Number(i.precio) * i.cantidad, 0);
  const cantidadTotal = carrito.reduce((suma, i) => suma + i.cantidad, 0);

  function agregarAlCarrito(producto) {
    const id = producto.codigo;
    const enCatalogo = buscarProducto(id);
    const existente = carrito.find((i) => String(i.id) === String(id));
    const cantidadActual = existente ? existente.cantidad : 0;

    if (!enCatalogo || enCatalogo.activo === false) {
      alert('Este producto ya no se encuentra disponible.');
      return false;
    }
    const stock = Number(enCatalogo.stockActual);
    if (stock <= 0) {
      alert('Este producto se encuentra sin stock.');
      return false;
    }
    if (cantidadActual >= stock) {
      alert(`No puedes agregar más unidades. Solo quedan ${stock} unidades disponibles.`);
      return false;
    }

    setCarrito((actual) =>
      existente
        ? actual.map((i) =>
            String(i.id) === String(id) ? { ...i, cantidad: i.cantidad + 1 } : i
          )
        : [
            ...actual,
            {
              id,
              nombre: producto.nombre,
              precio: producto.precio,
              imagen: producto.imagen,
              cantidad: 1,
            },
          ]
    );
    return true;
  }

  function aumentarCantidad(id) {
    const item = carrito.find((i) => String(i.id) === String(id));
    const enCatalogo = buscarProducto(id);
    if (!item) return;
    if (!enCatalogo || enCatalogo.activo === false) {
      alert('Este producto ya no se encuentra disponible.');
      return;
    }
    const stock = Number(enCatalogo.stockActual);
    if (item.cantidad >= stock) {
      alert(`Has alcanzado el stock disponible. Solo quedan ${stock} unidades.`);
      return;
    }
    setCarrito((actual) =>
      actual.map((i) => (String(i.id) === String(id) ? { ...i, cantidad: i.cantidad + 1 } : i))
    );
  }

  function disminuirCantidad(id) {
    setCarrito((actual) =>
      actual
        .map((i) => (String(i.id) === String(id) ? { ...i, cantidad: i.cantidad - 1 } : i))
        .filter((i) => i.cantidad > 0)
    );
  }

  function eliminarProducto(id) {
    setCarrito((actual) => actual.filter((i) => String(i.id) !== String(id)));
  }

  function vaciarCarrito() {
    if (carrito.length === 0) return;
    if (!confirm('¿Quieres vaciar el carrito?')) return;
    setCarrito([]);
  }

  // Devuelve true si la compra quedó confirmada en el backend.
  async function finalizarCompra() {
    if (comprando) return false;

    if (carrito.length === 0) {
      alert('Tu carrito está vacío. Agrega productos antes de finalizar la compra.');
      return false;
    }

    if (!esCliente) {
      alert('Debes iniciar sesión con una cuenta de cliente para finalizar la compra.');
      return false;
    }

    // Validación rápida en el navegador (el backend vuelve a validar todo)
    for (const item of carrito) {
      const enCatalogo = buscarProducto(item.id);
      if (!enCatalogo || enCatalogo.activo === false) {
        alert(`${item.nombre} ya no se encuentra disponible.`);
        return false;
      }
      if (item.cantidad > Number(enCatalogo.stockActual)) {
        alert(`Stock insuficiente para ${item.nombre}. Disponible: ${enCatalogo.stockActual}.`);
        return false;
      }
    }

    const resumen = carrito
      .map((i) => `${i.nombre} x${i.cantidad} - ${formatearPrecio(Number(i.precio) * i.cantidad)}`)
      .join('\n');

    const confirmado = confirm(
      `Resumen de compra:\n\n${resumen}\n\nTOTAL: ${formatearPrecio(total)}\n\n¿Deseas confirmar la compra?`
    );
    if (!confirmado) return false;

    if (!claveCompra.current) claveCompra.current = crypto.randomUUID();

    setComprando(true);
    try {
      // Solo se envían id y cantidad: el precio lo pone el backend desde el catálogo
      const pedido = await api('/pedidos', {
        method: 'POST',
        headers: { 'Idempotency-Key': claveCompra.current },
        body: {
          items: carrito.map((i) => ({
            productoId: buscarProducto(i.id).id,
            cantidad: i.cantidad,
          })),
        },
      });
      setCarrito([]);
      await recargarProductos(); // trae el stock ya descontado
      alert(
        `Compra realizada correctamente.\nPedido N° ${pedido.id} - Total: ${formatearPrecio(pedido.total)}`
      );
      return true;
    } catch (error) {
      // 400/409: el backend rechazó la compra (por ejemplo, otro cliente se llevó el stock).
      // Ese intento quedó cerrado, así que el próximo usará una clave nueva.
      if (error.status === 400 || error.status === 409) claveCompra.current = null;
      await recargarProductos();
      alert(error.message);
      return false;
    } finally {
      setComprando(false);
    }
  }

  return (
    <TiendaContext.Provider
      value={{
        productos,
        cargandoProductos,
        errorProductos,
        recargarProductos,
        carrito,
        total,
        cantidadTotal,
        comprando,
        buscarProducto,
        agregarAlCarrito,
        aumentarCantidad,
        disminuirCantidad,
        eliminarProducto,
        vaciarCarrito,
        finalizarCompra,
      }}
    >
      {children}
    </TiendaContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useTienda() {
  const context = useContext(TiendaContext);
  if (!context) {
    throw new Error('useTienda debe ser utilizado dentro de un TiendaProvider');
  }
  return context;
}
