import { useState } from 'react';
import useRecurso from '../../hooks/useRecurso.js';
import { api } from '../../services/api.js';
import { formatearFechaHora } from '../../utils/citas.js';

export default function AdminMensajes() {
  const { datos: mensajes, error, recargar } = useRecurso('/mensajes');
  const [busqueda, setBusqueda] = useState('');

  function obtenerEstado(mensaje) {
    const estado = String(mensaje.estado || 'Nuevo').toLowerCase();

    if (estado === 'respondido' || estado === 'respondida') {
      return 'Respondido';
    }

    if (estado === 'leido' || estado === 'leído' || estado === 'leida' || estado === 'leída') {
      return 'Leído';
    }

    return 'Nuevo';
  }

  // El backend usa los estados NUEVO, LEIDO y RESPONDIDO
  async function cambiarEstado(id, nuevoEstado) {
    try {
      await api(`/mensajes/${id}/estado`, {
        method: 'PUT',
        body: { estado: nuevoEstado },
      });
      await recargar();
    } catch (e) {
      alert(e.message);
    }
  }

  async function eliminar(id) {
    if (!window.confirm('¿Eliminar este mensaje?')) return;

    try {
      await api(`/mensajes/${id}`, { method: 'DELETE' });
      await recargar();
    } catch (e) {
      alert(e.message);
    }
  }

  const total = mensajes.length;

  const nuevos = mensajes.filter(
    (mensaje) => obtenerEstado(mensaje) === 'Nuevo'
  ).length;

  const leidos = mensajes.filter(
    (mensaje) => obtenerEstado(mensaje) === 'Leído'
  ).length;

  const respondidos = mensajes.filter(
    (mensaje) => obtenerEstado(mensaje) === 'Respondido'
  ).length;

  const mensajesFiltrados = mensajes.filter((mensaje) => {
    const texto = [
      mensaje.nombre,
      mensaje.correo,
      mensaje.telefono,
      mensaje.asunto,
      mensaje.mensaje,
      mensaje.fecha,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return texto.includes(busqueda.trim().toLowerCase());
  });

  return (
    <div className="admin-mensajes">
      <div className="admin-header">
        <div>
          <h1>Mensajes de Contacto</h1>
          <p>
            Revisa los mensajes enviados desde el formulario de contacto.
          </p>
        </div>
      </div>

      <section className="admin-mensajes-resumen">
        <div className="admin-mensajes-estadistica">
          <h3>Total mensajes</h3>
          <strong>{total}</strong>
        </div>

        <div className="admin-mensajes-estadistica">
          <h3>Nuevos</h3>
          <strong>{nuevos}</strong>
        </div>

        <div className="admin-mensajes-estadistica">
          <h3>Leídos</h3>
          <strong>{leidos}</strong>
        </div>

        <div className="admin-mensajes-estadistica">
          <h3>Respondidos</h3>
          <strong>{respondidos}</strong>
        </div>
      </section>

      <section className="admin-mensajes-panel">
        <div className="admin-mensajes-panel-header">
          <h2>Mensajes recibidos</h2>

          <input
            type="search"
            placeholder="Buscar mensaje..."
            aria-label="Buscar mensajes"
            value={busqueda}
            onChange={(evento) => setBusqueda(evento.target.value)}
          />
        </div>

        <div className="admin-mensajes-tabla-contenedor">
          <table className="admin-mensajes-tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Teléfono</th>
                <th>Mensaje</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>

            <tbody>
              {mensajesFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={7} className="admin-mensajes-vacio">
                    {error
                      ? error
                      : total === 0
                      ? 'No hay mensajes registrados.'
                      : 'No se encontraron mensajes con esa búsqueda.'}
                  </td>
                </tr>
              ) : (
                mensajesFiltrados.map((mensaje, indice) => {
                  const estado = obtenerEstado(mensaje);

                  return (
                    <tr key={mensaje.id ?? indice}>
                      <td>{mensaje.nombre || '-'}</td>

                      <td>{mensaje.correo || '-'}</td>

                      <td>{mensaje.telefono || '-'}</td>

                      <td className="admin-mensajes-texto">
                        {mensaje.asunto && (
                          <strong>{mensaje.asunto}</strong>
                        )}
                        <p>{mensaje.mensaje || '-'}</p>
                      </td>

                      <td>{formatearFechaHora(mensaje.fecha)}</td>

                      <td>
                        <span
                          className={`admin-mensajes-estado estado-${estado.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')}`}
                        >
                          {estado}
                        </span>
                      </td>

                      <td>
                        <div className="admin-mensajes-acciones">
                          {estado === 'Nuevo' && (
                            <button
                              type="button"
                              className="admin-mensajes-btn-leer"
                              onClick={() => cambiarEstado(mensaje.id, 'LEIDO')}
                            >
                              Marcar leído
                            </button>
                          )}

                          {estado !== 'Respondido' && (
                            <button
                              type="button"
                              className="admin-mensajes-btn-responder"
                              onClick={() => cambiarEstado(mensaje.id, 'RESPONDIDO')}
                            >
                              Respondido
                            </button>
                          )}

                          <button
                            type="button"
                            className="admin-mensajes-btn-eliminar"
                            onClick={() => eliminar(mensaje.id)}
                          >
                            Eliminar
                          </button>
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
