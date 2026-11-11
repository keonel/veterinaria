import { useState } from 'react';
import useRecurso from '../../hooks/useRecurso.js';
import { api } from '../../services/api.js';
import { normalizarEstado, textoEstadoCita } from '../../utils/citas.js';

export default function AdminCitas() {
  const { datos: citas, error, recargar } = useRecurso('/citas');
  const [filtro, setFiltro] = useState('todas');

  // PUT /citas/{id}/estado  (solo ADMIN: el backend responde 403 a cualquier otro rol)
  async function cambiarEstado(cita, estado) {
    try {
      await api(`/citas/${cita.id}/estado`, { method: 'PUT', body: { estado } });
      await recargar();
    } catch (e) {
      alert(e.message);
    }
  }

  async function eliminar(cita) {
    if (!window.confirm(`¿Eliminar la cita ${cita.codigo}?`)) return;
    try {
      await api(`/citas/${cita.id}`, { method: 'DELETE' });
      await recargar();
    } catch (e) {
      alert(e.message);
    }
  }

  
  const totalCitas = citas.length;

  const pendientes = citas.filter(
    (c) => normalizarEstado(c.estado) === 'pendiente'
  ).length;

  const confirmadas = citas.filter(
    (c) => normalizarEstado(c.estado) === 'confirmada'
  ).length;

  const rechazadas = citas.filter(
    (c) => normalizarEstado(c.estado) === 'rechazada'
  ).length;

  
  const visibles = citas.filter(
    (c) =>
      filtro === 'todas' ||
      normalizarEstado(c.estado) === filtro
  );

  return (
    <div className="admin-citas">

      
      <div className="admin-header">
        <div>
          <h1>Gestión de Citas</h1>
          <p>
            Revisa y administra las citas solicitadas por los clientes.
          </p>
        </div>

        <div className="admin-usuario">
          Administrador
        </div>
      </div>

      
      <section className="admin-resumen">

        <div className="resumen-card">
          <h3>Total</h3>
          <div className="resumen-numero">{totalCitas}</div>
          <span>Citas registradas</span>
        </div>

        <div className="resumen-card">
          <h3>Pendientes</h3>
          <div className="resumen-numero">{pendientes}</div>
          <span>Por confirmar</span>
        </div>

        <div className="resumen-card">
          <h3>Confirmadas</h3>
          <div className="resumen-numero">{confirmadas}</div>
          <span>Citas confirmadas</span>
        </div>

        <div className="resumen-card">
          <h3>Rechazadas</h3>
          <div className="resumen-numero">{rechazadas}</div>
          <span>Citas rechazadas</span>
        </div>

      </section>

      
      <section className="admin-seccion">

        <div className="admin-citas-encabezado">
          <h2>Todas las citas</h2>

          <div className="filtro-citas">
            <label htmlFor="filtroEstado">
              Filtrar por estado:
            </label>

            <select
              id="filtroEstado"
              value={filtro}
              onChange={(e) => setFiltro(e.target.value)}
            >
              <option value="todas">Todas</option>
              <option value="pendiente">Pendientes</option>
              <option value="confirmada">Confirmadas</option>
              <option value="rechazada">Rechazadas</option>
            </select>
          </div>
        </div>

        <div className="tabla-contenedor">
          <table className="tabla-admin">
            <thead>
              <tr>
                <th>Código</th>
                <th>Fecha</th>
                <th>Hora</th>
                <th>Mascota</th>
                <th>Tutor</th>
                <th>Servicio</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>

            <tbody>
              {visibles.length === 0 ? (
                <tr>
                  <td colSpan={8} className="citas-vacio">
                    {error
                      ? error
                      : citas.length === 0
                      ? 'No hay citas registradas.'
                      : 'No hay citas que coincidan con el filtro.'}
                  </td>
                </tr>
              ) : (
                visibles.map((c) => (
                  <tr key={c.codigo}>
                    <td>{c.codigo}</td>
                    <td>{c.fecha}</td>
                    <td>{c.hora}</td>
                    <td>{c.mascota}</td>
                    <td>{c.cliente}</td>
                    <td>{c.servicio || 'No especificado'}</td>

                    <td>
                      <span
                        className={`estado-cita estado-${normalizarEstado(c.estado)}`}
                      >
                        {textoEstadoCita(c.estado)}
                      </span>
                    </td>

                    <td>
                      <div className="acciones-cita">
                        <button
                          type="button"
                          className="btn-confirmar-cita"
                          onClick={() => cambiarEstado(c, 'CONFIRMADA')}
                        >
                          Confirmar
                        </button>

                        <button
                          type="button"
                          className="btn-rechazar-cita"
                          onClick={() => cambiarEstado(c, 'RECHAZADA')}
                        >
                          Rechazar
                        </button>

                        <button
                          type="button"
                          className="btn-rechazar-cita"
                          onClick={() => eliminar(c)}
                        >
                          Eliminar
                        </button>
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
