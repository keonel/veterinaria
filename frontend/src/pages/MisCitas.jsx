import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api.js';
import { normalizarEstado } from '../utils/citas.js';

const textoEstado = {
  pendiente: 'Pendiente de Confirmación',
  confirmada: 'Confirmada',
  rechazada: 'Rechazada',
};

export default function MisCitas() {
  const [misCitas, setMisCitas] = useState([]);
  const [error, setError] = useState('');

  // GET /citas/mis-citas: el backend devuelve solo las citas del usuario del JWT.
  // El filtro ya no se hace en el navegador, así nadie puede ver citas ajenas.
  useEffect(() => {
    let vigente = true;
    api('/citas/mis-citas')
      .then((lista) => vigente && setMisCitas(lista))
      .catch((e) => vigente && setError(e.message));
    return () => {
      vigente = false;
    };
  }, []);

  
  const contar = (estado) =>
    misCitas.filter(
      (c) => normalizarEstado(c.estado) === estado
    ).length;

  return (
    <section className="mis-citas-pagina">
      <div className="mis-citas-contenedor">

        <header className="mis-citas-encabezado">
          <h1>Mis Citas</h1>
          <p>
            Aquí puedes revisar las citas veterinarias
            que has solicitado.
          </p>
        </header>

        
        <div className="mis-citas-estadisticas">

          <div className="mis-citas-estadistica">
            <span>Total</span>
            <strong id="totalMisCitas">
              {misCitas.length}
            </strong>
          </div>

          <div className="mis-citas-estadistica">
            <span>Pendientes</span>
            <strong id="totalPendientes">
              {contar('pendiente')}
            </strong>
          </div>

          <div className="mis-citas-estadistica">
            <span>Confirmadas</span>
            <strong id="totalConfirmadas">
              {contar('confirmada')}
            </strong>
          </div>

          <div className="mis-citas-estadistica">
            <span>Rechazadas</span>
            <strong id="totalRechazadas">
              {contar('rechazada')}
            </strong>
          </div>

        </div>

        
        <div className="mis-citas-historial">

          <div className="mis-citas-historial-header">
            <h2>Historial de citas</h2>

            <Link
              to="/solicitar-cita"
              className="mis-citas-nueva"
            >
              + Solicitar nueva cita
            </Link>
          </div>

          <div className="mis-citas-tabla-contenedor">
            <table className="mis-citas-tabla">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Mascota</th>
                  <th>Servicio</th>
                  <th>Fecha</th>
                  <th>Hora</th>
                  <th>Veterinario</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody id="tablaMisCitas">
                {misCitas.length === 0 ? (
                  <tr>
                    <td
                      colSpan={7}
                      className="mis-citas-vacio"
                    >
                      {error || (
                        <>
                          Todavía no tienes citas registradas.
                          <br />
                          Puedes solicitar una desde
                          "Solicitar Cita".
                        </>
                      )}
                    </td>
                  </tr>
                ) : (
                  misCitas.map((cita) => {
                    const estado = normalizarEstado(
                      cita.estado
                    );

                    return (
                      <tr key={cita.codigo}>
                        <td>{cita.codigo}</td>
                        <td>{cita.mascota}</td>
                        <td>{cita.servicio}</td>
                        <td>{cita.fecha}</td>
                        <td>{cita.hora}</td>
                        <td>
                          {cita.veterinario ||
                            'Sin preferencia'}
                        </td>
                        <td>
                          <span
                            className={`estado estado-${estado}`}
                          >
                            {textoEstado[estado] ||
                              cita.estado}
                          </span>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </section>
  );
}
