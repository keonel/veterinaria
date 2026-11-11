import { useState } from 'react';
import { useTienda } from '../../context/TiendaContext.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { api } from '../../services/api.js';
import { formatearPrecio } from '../../utils/formatos.js';

const formularioVacio = {
  codigo: '',
  tipo: '',
  nombre: '',
  categoria: '',
  imagen: '',
  precio: '',
  stockActual: '0',
  stockCritico: '0',
  descripcion: '',
  activo: true,
};

export default function AdminProductos() {
  const { productos, recargarProductos, errorProductos } = useTienda();
  // Restricción por rol: el ADMIN gestiona; el VENDEDOR solo ve la lista y el detalle.
  // (Si el vendedor llamara igual a POST/PUT/DELETE, el backend respondería 403.)
  const { esAdmin } = useAuth();

  const [form, setForm] = useState({ ...formularioVacio });
  const [codigoEditando, setCodigoEditando] = useState(null);
  const [idEditando, setIdEditando] = useState(null);
  const [errores, setErrores] = useState({});
  const [errorServidor, setErrorServidor] = useState('');
  const [detalle, setDetalle] = useState(null);

  // GET /productos/{id}: detalle de un producto
  async function verDetalle(producto) {
    try {
      setDetalle(await api(`/productos/${producto.id}`));
    } catch (e) {
      window.alert(e.message);
    }
  }

  
  const total = productos.length;

  const totalProductos = productos.filter(
    (p) => p.tipo !== 'servicio'
  ).length;

  const totalServicios = productos.filter(
    (p) => p.tipo === 'servicio'
  ).length;

  const stockCritico = productos.filter(
    (p) =>
      p.tipo !== 'servicio' &&
      Number(p.stockActual) <= Number(p.stockCritico)
  ).length;

  
  function cambiar(evento) {
    const { name, value, type, checked } = evento.target;

    setForm((actual) => ({
      ...actual,
      [name]: type === 'checkbox' ? checked : value,
    }));

    setErrores((actual) => ({
      ...actual,
      [name]: undefined,
    }));
  }

  
  function validar() {
    const nuevosErrores = {};
    const codigo = form.codigo.trim().toLowerCase();

    if (!codigo) {
      nuevosErrores.codigo = 'El código es obligatorio';
    } else if (
      codigoEditando === null &&
      productos.some(
        (p) => String(p.codigo).toLowerCase() === codigo
      )
    ) {
      nuevosErrores.codigo = 'Ya existe un registro con ese código';
    }

    if (!form.tipo) {
      nuevosErrores.tipo = 'Selecciona un tipo';
    }

    if (!form.nombre.trim()) {
      nuevosErrores.nombre = 'El nombre es obligatorio';
    }

    if (!form.categoria.trim()) {
      nuevosErrores.categoria = 'La categoría es obligatoria';
    }

    if (
      form.precio === '' ||
      !Number.isFinite(Number(form.precio)) ||
      Number(form.precio) < 0
    ) {
      nuevosErrores.precio = 'Ingresa un precio válido';
    }

    if (form.tipo === 'producto') {
      if (
        form.stockActual === '' ||
        !Number.isInteger(Number(form.stockActual)) ||
        Number(form.stockActual) < 0
      ) {
        nuevosErrores.stockActual =
          'Ingresa un stock entero de 0 o más';
      }

      if (
        form.stockCritico === '' ||
        !Number.isInteger(Number(form.stockCritico)) ||
        Number(form.stockCritico) < 0
      ) {
        nuevosErrores.stockCritico =
          'Ingresa un stock crítico entero de 0 o más';
      }
    }

    return nuevosErrores;
  }

  
  function cancelar() {
    setForm({ ...formularioVacio });
    setCodigoEditando(null);
    setIdEditando(null);
    setErrores({});
    setErrorServidor('');
  }


  async function guardar(evento) {
    evento.preventDefault();

    const nuevosErrores = validar();
    setErrores(nuevosErrores);
    setErrorServidor('');

    if (Object.keys(nuevosErrores).length > 0) return;

    const registro = {
      codigo: form.codigo.trim(),
      tipo: form.tipo,
      nombre: form.nombre.trim(),
      categoria: form.categoria.trim(),
      imagen: form.imagen.trim(),
      descripcion: form.descripcion.trim(),
      precio: Number(form.precio),
      stockActual:
        form.tipo === 'servicio' ? 0 : Number(form.stockActual),
      stockCritico:
        form.tipo === 'servicio' ? 0 : Number(form.stockCritico),
      activo: form.activo !== false,
    };

    try {
      if (idEditando === null) {
        // Crear: POST /productos -> 201
        await api('/productos', { method: 'POST', body: registro });
      } else {
        // Actualizar: PUT /productos/{id} -> 200
        await api(`/productos/${idEditando}`, { method: 'PUT', body: registro });
      }
      await recargarProductos();
      cancelar();
    } catch (error) {
      // 400: errores por campo que detectó @Valid. 409: código duplicado.
      setErrores(error.campos || {});
      setErrorServidor(error.message);
    }
  }

  
  function editar(producto) {
    setForm({
      ...formularioVacio,
      ...producto,
      tipo: producto.tipo || 'producto',
    });

    setCodigoEditando(producto.codigo);
    setIdEditando(producto.id);
    setErrores({});
    setErrorServidor('');

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  
  async function alternarActivo(producto) {
    try {
      await api(`/productos/${producto.id}`, {
        method: 'PUT',
        body: { ...producto, activo: producto.activo === false },
      });
      await recargarProductos();
    } catch (e) {
      window.alert(e.message);
    }
  }


  async function eliminar(producto) {
    if (
      !window.confirm(
        `¿Eliminar ${producto.nombre} del sistema?`
      )
    ) {
      return;
    }

    try {
      // Eliminar: DELETE /productos/{id} -> 204
      await api(`/productos/${producto.id}`, { method: 'DELETE' });
      await recargarProductos();
    } catch (e) {
      window.alert(e.message);
    }

    if (codigoEditando === producto.codigo) {
      cancelar();
    }
  }

  
  function campo({
    name,
    etiqueta,
    tipo = 'text',
    placeholder = '',
    requerido = false,
    minimo,
  }) {
    return (
      <div className="productos-campo" key={name}>
        <label htmlFor={`producto-${name}`}>
          {etiqueta} {requerido && '*'}
        </label>

        <input
          id={`producto-${name}`}
          name={name}
          type={tipo}
          value={form[name]}
          placeholder={placeholder}
          min={minimo}
          onChange={cambiar}
          disabled={
            name === 'codigo' && codigoEditando !== null
          }
        />

        {errores[name] && (
          <small className="mensaje-error">
            {errores[name]}
          </small>
        )}
      </div>
    );
  }

  return (
    <div className="productos-admin">

      
      <div className="admin-header">
        <div>
          <h1>Productos y Servicios</h1>
          <p>
            Administra los productos y servicios disponibles
            en Veterinaria San Marcos.
          </p>
        </div>

        <div className="productos-admin-etiqueta">
          {esAdmin ? 'Administrador' : 'Vendedor · solo lectura'}
        </div>
      </div>

      
      <section className="productos-resumen">

        <div className="producto-resumen-card">
          <h3>Total</h3>
          <strong>{total}</strong>
          <p>Productos y servicios</p>
        </div>

        <div className="producto-resumen-card">
          <h3>Productos</h3>
          <strong>{totalProductos}</strong>
          <p>Productos registrados</p>
        </div>

        <div className="producto-resumen-card">
          <h3>Servicios</h3>
          <strong>{totalServicios}</strong>
          <p>Servicios registrados</p>
        </div>

        <div className="producto-resumen-card">
          <h3>Stock crítico</h3>
          <strong>{stockCritico}</strong>
          <p>Productos con alerta</p>
        </div>

      </section>

      
      {/* El formulario de creación/edición solo existe para el ADMIN */}
      {esAdmin && (
      <section className="productos-panel">

        <h2>
          {codigoEditando === null
            ? 'Agregar producto o servicio'
            : `Editar producto o servicio: ${codigoEditando}`}
        </h2>

        <form onSubmit={guardar} noValidate>

          <div className="productos-form-grid">

            {campo({
              name: 'codigo',
              etiqueta: 'Código',
              placeholder: 'Ej: PROD001',
              requerido: true,
            })}

            <div className="productos-campo">
              <label htmlFor="producto-tipo">
                Tipo *
              </label>

              <select
                id="producto-tipo"
                name="tipo"
                value={form.tipo}
                onChange={cambiar}
              >
                <option value="">Selecciona</option>
                <option value="producto">Producto</option>
                <option value="servicio">Servicio</option>
              </select>

              {errores.tipo && (
                <small className="mensaje-error">
                  {errores.tipo}
                </small>
              )}
            </div>

            {campo({
              name: 'nombre',
              etiqueta: 'Nombre',
              placeholder: 'Nombre del producto o servicio',
              requerido: true,
            })}

            {campo({
              name: 'categoria',
              etiqueta: 'Categoría',
              placeholder: 'Ej: Alimentos, Higiene, Consulta...',
              requerido: true,
            })}

            <div className="productos-campo">
              <label htmlFor="producto-imagen">
                Imagen
              </label>

              <input
                id="producto-imagen"
                name="imagen"
                type="text"
                value={form.imagen}
                onChange={cambiar}
                placeholder="Ej: /assets/img/pelota-perro.jpg"
              />

              <small>
                Ingresa la ruta de una imagen guardada en
                assets/img/
              </small>
            </div>

            {campo({
              name: 'precio',
              etiqueta: 'Precio CLP',
              tipo: 'number',
              placeholder: 'Ej: 15990',
              requerido: true,
              minimo: 0,
            })}

            {form.tipo !== 'servicio' && (
              <>
                {campo({
                  name: 'stockActual',
                  etiqueta: 'Stock actual',
                  tipo: 'number',
                  requerido: true,
                  minimo: 0,
                })}

                {campo({
                  name: 'stockCritico',
                  etiqueta: 'Stock crítico',
                  tipo: 'number',
                  requerido: true,
                  minimo: 0,
                })}
              </>
            )}

            <div className="productos-campo productos-ancho-completo">
              <label htmlFor="producto-descripcion">
                Descripción
              </label>

              <textarea
                id="producto-descripcion"
                name="descripcion"
                rows="4"
                placeholder="Descripción del producto o servicio..."
                value={form.descripcion}
                onChange={cambiar}
              />
            </div>

          </div>

          <div className="productos-form-footer">

            <label className="productos-check">
              <input
                type="checkbox"
                name="activo"
                checked={form.activo !== false}
                onChange={cambiar}
              />
              Activo
            </label>

            <div className="productos-form-acciones">
              {codigoEditando !== null && (
                <button
                  type="button"
                  className="productos-btn-cancelar"
                  onClick={cancelar}
                >
                  Cancelar
                </button>
              )}

              <button
                type="submit"
                className="productos-btn-guardar"
              >
                {codigoEditando === null
                  ? 'Guardar'
                  : 'Guardar cambios'}
              </button>
            </div>

          </div>

          {errorServidor && (
            <p className="mensaje-error" role="alert">
              {errorServidor}
            </p>
          )}

        </form>
      </section>
      )}

      {detalle && (
        <section className="productos-panel" aria-live="polite">
          <h2>Detalle: {detalle.nombre}</h2>
          <p><strong>Código:</strong> {detalle.codigo}</p>
          <p><strong>Tipo:</strong> {detalle.tipo}</p>
          <p><strong>Categoría:</strong> {detalle.categoria}</p>
          <p><strong>Precio:</strong> {formatearPrecio(detalle.precio)}</p>
          {detalle.tipo !== 'servicio' && (
            <p>
              <strong>Stock:</strong> {detalle.stockActual} (crítico: {detalle.stockCritico})
            </p>
          )}
          <p><strong>Estado:</strong> {detalle.activo ? 'Activo' : 'Inactivo'}</p>
          <p><strong>Descripción:</strong> {detalle.descripcion || '-'}</p>
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
                <th>Código</th>
                <th>Tipo</th>
                <th>Nombre</th>
                <th>Categoría</th>
                <th>Precio</th>
                <th>Stock actual</th>
                <th>Stock crítico</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>

            <tbody>
              {productos.length === 0 ? (
                <tr>
                  <td colSpan={9} className="productos-vacio">
                    {errorProductos || 'No hay productos ni servicios registrados.'}
                  </td>
                </tr>
              ) : (
                productos.map((p) => {
                  const esServicio = p.tipo === 'servicio';

                  const stockBajo =
                    !esServicio &&
                    Number(p.stockActual) <=
                    Number(p.stockCritico);

                  const sinStock =
                    !esServicio &&
                    Number(p.stockActual) === 0;

                  return (
                    <tr key={p.codigo}>

                      <td>{p.codigo}</td>

                      <td>
                        {esServicio ? 'Servicio' : 'Producto'}
                      </td>

                      <td>{p.nombre}</td>

                      <td>{p.categoria}</td>

                      <td>{formatearPrecio(p.precio)}</td>

                      <td>
                        {esServicio ? '-' : p.stockActual}
                      </td>

                      <td>
                        {esServicio ? '-' : p.stockCritico}
                      </td>

                      <td>
                        {p.activo === false ? (
                          <span className="producto-estado inactivo">
                            Inactivo
                          </span>
                        ) : esServicio ? (
                          <span className="producto-estado normal">
                            Disponible
                          </span>
                        ) : sinStock ? (
                          <span className="producto-estado critico">
                            Sin stock
                          </span>
                        ) : stockBajo ? (
                          <span className="producto-estado critico">
                            Stock crítico
                          </span>
                        ) : (
                          <span className="producto-estado normal">
                            Stock normal
                          </span>
                        )}
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
                          {esAdmin && (
                            <>
                              <button
                                type="button"
                                className="productos-btn-editar"
                                onClick={() => editar(p)}
                              >
                                Editar
                              </button>

                              <button
                                type="button"
                                className="productos-btn-estado"
                                onClick={() => alternarActivo(p)}
                              >
                                {p.activo === false
                                  ? 'Activar'
                                  : 'Desactivar'}
                              </button>

                              <button
                                type="button"
                                className="productos-btn-eliminar"
                                onClick={() => eliminar(p)}
                              >
                                Eliminar
                              </button>
                            </>
                          )}

                        </div>
                      </td>

                    </tr>
                  );
                })
              )}
            </tbody>

          </table>
        </div>

      </section>

    </div>
  );
}
