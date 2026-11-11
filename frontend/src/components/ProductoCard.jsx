import { useEffect, useRef, useState } from 'react';
import { useTienda } from '../context/TiendaContext.jsx';
import { formatearPrecio } from '../utils/formatos.js';

export default function ProductoCard({ producto }) {
  const { agregarAlCarrito } = useTienda();

  const [agregado, setAgregado] = useState(false);
  const temporizador = useRef(null);

  const tieneStock = Number(producto.stockActual) > 0;
  const imagen = producto.imagen || '/assets/img/placeholder.png';

  function manejarClic() {
    if (!tieneStock) return;


    agregarAlCarrito(producto);


    setAgregado(true);

  
    clearTimeout(temporizador.current);

    temporizador.current = setTimeout(() => {
      setAgregado(false);
    }, 1200);
  }

  useEffect(() => {
    return () => clearTimeout(temporizador.current);
  }, []);

  return (
    <article className="card producto-card">
      <div className="producto-imagen">
        <img
          src={imagen}
          alt={producto.nombre}
          className="card-img"
        />
      </div>

      <div className="card-body">
        <span className="producto-categoria">
          {producto.categoria}
        </span>

        <h3 className="card-title">
          {producto.nombre}
        </h3>

        <p className="card-text">
          {producto.descripcion}
        </p>

        <p className="card-precio">
          {formatearPrecio(producto.precio)}
        </p>

        <button
          type="button"
          className={`producto-boton ${agregado ? 'producto-boton-agregado' : ''}`}
          onClick={manejarClic}
          disabled={!tieneStock}
        >
          {tieneStock
            ? agregado
              ? '✓ ¡Agregado!'
              : 'Agregar al carrito'
            : 'Sin stock'}
        </button>
      </div>
    </article>
  );
}
