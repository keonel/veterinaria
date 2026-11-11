import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { TiendaProvider, useTienda } from './TiendaContext.jsx';

// Dobles de prueba del cliente HTTP y de la sesión
vi.mock('../services/api.js', () => ({ api: vi.fn() }));
vi.mock('./AuthContext.jsx', () => ({ useAuth: vi.fn() }));

import { api } from '../services/api.js';
import { useAuth } from './AuthContext.jsx';

const catalogo = [
  { id: 1, codigo: 'SM001', tipo: 'producto', nombre: 'Milpro', precio: 15000, stockActual: 10, stockCritico: 3, activo: true, imagen: '' },
  { id: 2, codigo: 'SM002', tipo: 'producto', nombre: 'Shampoo', precio: 12000, stockActual: 2, stockCritico: 1, activo: true, imagen: '' },
  { id: 3, codigo: 'INAC', tipo: 'producto', nombre: 'Inactivo', precio: 100, stockActual: 5, stockCritico: 1, activo: false, imagen: '' },
];

// Simula el backend: GET /productos devuelve el catálogo y POST /pedidos confirma la compra
function backend({ pedido = { id: 77, total: 15000 }, errorPedido = null, despues = catalogo } = {}) {
  let compras = 0;
  api.mockImplementation(async (ruta, opciones) => {
    if (ruta === '/productos') return compras > 0 ? despues : catalogo;
    if (ruta === '/pedidos' && opciones?.method === 'POST') {
      if (errorPedido) throw errorPedido;
      compras += 1;
      return pedido;
    }
    throw new Error(`Ruta no esperada: ${ruta}`);
  });
}

async function montar() {
  const hook = renderHook(() => useTienda(), { wrapper: TiendaProvider });
  await waitFor(() => expect(hook.result.current.productos.length).toBe(3));
  return hook;
}

describe('Pruebas en TiendaContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    vi.stubGlobal('alert', vi.fn());
    vi.stubGlobal('confirm', vi.fn(() => true));
    useAuth.mockReturnValue({ esCliente: true });
    backend();
  });

  it('debe lanzar error si useTienda se usa fuera de TiendaProvider', () => {
    expect(() => renderHook(() => useTienda())).toThrow(
      'useTienda debe ser utilizado dentro de un TiendaProvider'
    );
  });

  it('carga los productos desde el backend', async () => {
    const { result } = await montar();
    expect(api).toHaveBeenCalledWith('/productos');
    expect(result.current.productos[0].codigo).toBe('SM001');
    expect(result.current.cargandoProductos).toBe(false);
  });

  it('informa el error si el backend no responde', async () => {
    api.mockRejectedValue(new Error('No se pudo conectar con el servidor.'));
    const { result } = renderHook(() => useTienda(), { wrapper: TiendaProvider });
    await waitFor(() => expect(result.current.cargandoProductos).toBe(false));
    expect(result.current.errorProductos).toBe('No se pudo conectar con el servidor.');
    expect(result.current.productos).toEqual([]);
  });

  it('debe agregar un producto al carrito y calcular total', async () => {
    const { result } = await montar();
    const prod = result.current.productos[0];

    act(() => {
      result.current.agregarAlCarrito(prod);
    });

    expect(result.current.carrito.length).toBe(1);
    expect(result.current.cantidadTotal).toBe(1);
    expect(result.current.total).toBe(15000);
  });

  it('no permite agregar si el producto está inactivo', async () => {
    const { result } = await montar();

    let respuesta;
    act(() => {
      respuesta = result.current.agregarAlCarrito(result.current.productos[2]);
    });

    expect(respuesta).toBe(false);
    expect(window.alert).toHaveBeenCalledWith('Este producto ya no se encuentra disponible.');
  });

  it('no permite superar el stock disponible', async () => {
    const { result } = await montar();
    const shampoo = result.current.productos[1]; // stock 2

    act(() => {
      result.current.agregarAlCarrito(shampoo);
    });
    act(() => {
      result.current.aumentarCantidad('SM002');
    });
    act(() => {
      result.current.aumentarCantidad('SM002');
    });

    expect(result.current.carrito[0].cantidad).toBe(2);
    expect(window.alert).toHaveBeenCalledWith(
      'Has alcanzado el stock disponible. Solo quedan 2 unidades.'
    );
  });

  it('aumenta, disminuye y elimina productos del carrito', async () => {
    const { result } = await montar();
    const prod = result.current.productos[0];

    act(() => {
      result.current.agregarAlCarrito(prod);
    });

    act(() => {
      result.current.aumentarCantidad(prod.codigo);
    });
    expect(result.current.carrito[0].cantidad).toBe(2);

    act(() => {
      result.current.disminuirCantidad(prod.codigo);
    });
    expect(result.current.carrito[0].cantidad).toBe(1);

    act(() => {
      result.current.eliminarProducto(prod.codigo);
    });
    expect(result.current.carrito.length).toBe(0);
  });

  it('permite vaciar el carrito tras confirmar', async () => {
    const { result } = await montar();

    act(() => {
      result.current.agregarAlCarrito(result.current.productos[0]);
    });

    act(() => {
      result.current.vaciarCarrito();
    });

    expect(result.current.carrito.length).toBe(0);
  });

  it('finalizarCompra envía solo id y cantidad al backend y actualiza el stock', async () => {
    const conStockDescontado = [{ ...catalogo[0], stockActual: 9 }, catalogo[1], catalogo[2]];
    backend({ despues: conStockDescontado });
    const { result } = await montar();

    act(() => {
      result.current.agregarAlCarrito(result.current.productos[0]);
    });

    let exito;
    await act(async () => {
      exito = await result.current.finalizarCompra();
    });

    expect(exito).toBe(true);
    const llamada = api.mock.calls.find(([ruta]) => ruta === '/pedidos');
    // El precio no viaja: lo decide el backend
    expect(llamada[1].body).toEqual({ items: [{ productoId: 1, cantidad: 1 }] });
    expect(llamada[1].headers['Idempotency-Key']).toBeTruthy();
    expect(result.current.carrito.length).toBe(0);
    expect(result.current.buscarProducto('SM001').stockActual).toBe(9);
  });

  it('finalizarCompra exige una sesión de cliente', async () => {
    useAuth.mockReturnValue({ esCliente: false });
    const { result } = await montar();

    act(() => {
      result.current.agregarAlCarrito(result.current.productos[0]);
    });

    let exito;
    await act(async () => {
      exito = await result.current.finalizarCompra();
    });

    expect(exito).toBe(false);
    expect(api.mock.calls.some(([ruta]) => ruta === '/pedidos')).toBe(false);
    expect(window.alert).toHaveBeenCalledWith(
      'Debes iniciar sesión con una cuenta de cliente para finalizar la compra.'
    );
  });

  it('si el backend rechaza la compra se muestra su mensaje y el carrito se conserva', async () => {
    const error = Object.assign(new Error('Stock insuficiente para Milpro. Disponible: 0.'), {
      status: 409,
    });
    backend({ errorPedido: error });
    const { result } = await montar();

    act(() => {
      result.current.agregarAlCarrito(result.current.productos[0]);
    });

    let exito;
    await act(async () => {
      exito = await result.current.finalizarCompra();
    });

    expect(exito).toBe(false);
    expect(result.current.carrito.length).toBe(1);
    expect(window.alert).toHaveBeenCalledWith('Stock insuficiente para Milpro. Disponible: 0.');
  });

  it('no compra con el carrito vacío', async () => {
    const { result } = await montar();

    let exito;
    await act(async () => {
      exito = await result.current.finalizarCompra();
    });

    expect(exito).toBe(false);
    expect(api.mock.calls.some(([ruta]) => ruta === '/pedidos')).toBe(false);
  });
});
