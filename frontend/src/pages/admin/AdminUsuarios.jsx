import { useState } from 'react';
import { createPortal } from 'react-dom';
import useRecurso from '../../hooks/useRecurso.js';
import { api } from '../../services/api.js';

const usuarioNuevo = {
  id: null,
  run: '',
  nombre: '',
  apellidos: '',
  correo: '',
  telefono: '',
  region: '',
  comuna: '',
  direccion: '',
  contrasena: '',
  rol: 'VENDEDOR',
  activo: true,
};

// Arma el cuerpo que espera el backend (UsuarioRequest)
function aSolicitud(u) {
  return {
    run: u.run,
    nombre: u.nombre,
    apellidos: u.apellidos,
    correo: u.correo,
    telefono: u.telefono || '',
    region: u.region || '',
    comuna: u.comuna || '',
    direccion: u.direccion || '',
    contrasena: u.contrasena || '',
    rol: u.rol,
    activo: u.activo !== false,
  };
}

function mensajeError(error) {
  return [error.message, ...Object.values(error.campos || {})].join('\n');
}

export default function AdminUsuarios() {
  // GET /usuarios (solo ADMIN). El backend nunca devuelve contraseñas ni hashes.
  const { datos: usuarios, error, recargar } = useRecurso('/usuarios');
  const [busqueda, setBusqueda] = useState('');
  const [usuarioEditando, setUsuarioEditando] = useState(null);

  const esNuevo = usuarioEditando?.id === null;

  async function alternarActivo(usuario) {
    try {
      await api(`/usuarios/${usuario.id}`, {
        method: 'PUT',
        body: aSolicitud({ ...usuario, activo: usuario.activo === false }),
      });
      await recargar();
    } catch (e) {
      window.alert(mensajeError(e));
    }
  }


  async function eliminar(usuario) {
    const confirmar = window.confirm(
      `¿Estás seguro de eliminar a ${usuario.nombre}?`
    );

    if (!confirmar) return;

    try {
      await api(`/usuarios/${usuario.id}`, { method: 'DELETE' });
      await recargar();
    } catch (e) {
      window.alert(mensajeError(e));
    }
  }


  function abrirEdicion(usuario) {
    setUsuarioEditando({ ...usuario, contrasena: '' });
  }

  function abrirCreacion() {
    setUsuarioEditando({ ...usuarioNuevo });
  }


  function cerrarEdicion() {
    setUsuarioEditando(null);
  }


  function cambiarCampo(e) {
    const { name, value } = e.target;

    setUsuarioEditando((actual) => ({
      ...actual,
      [name]: value,
    }));
  }


  async function guardarEdicion(e) {
    e.preventDefault();

    if (!usuarioEditando) return;

    try {
      if (esNuevo) {
        // POST /usuarios: el administrador sí puede elegir el rol (p. ej. crear un VENDEDOR)
        await api('/usuarios', { method: 'POST', body: aSolicitud(usuarioEditando) });
      } else {
        await api(`/usuarios/${usuarioEditando.id}`, {
          method: 'PUT',
          body: aSolicitud(usuarioEditando),
        });
      }
      await recargar();
      cerrarEdicion();
      window.alert(
        esNuevo ? 'Usuario creado correctamente' : 'Usuario actualizado correctamente'
      );
    } catch (error) {
      window.alert(mensajeError(error));
    }
  }

  
  const totalUsuarios = usuarios.length;

  const usuariosActivos = usuarios.filter(
    (u) => u.activo !== false
  ).length;

  const usuariosInactivos = usuarios.filter(
    (u) => u.activo === false
  ).length;

 
  const usuariosFiltrados = usuarios.filter((u) => {
    const texto = busqueda.toLowerCase().trim();

    const nombre = `${u.nombre || ''} ${u.apellidos || ''}`
      .toLowerCase();

    const correo = (u.correo || '').toLowerCase();

    const run = String(u.run || u.rut || '').toLowerCase();

    return (
      nombre.includes(texto) ||
      correo.includes(texto) ||
      run.includes(texto)
    );
  });

  return (
    <div className="usuarios-main">

     
      <div className="admin-header">
        <div>
          <h1>Usuarios</h1>
          <p>Administra las cuentas registradas en el sistema.</p>
        </div>

        <button
          type="button"
          onClick={abrirCreacion}
          style={{
            padding: '12px 22px',
            border: 'none',
            borderRadius: '10px',
            background: '#d9609a',
            color: '#fff',
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          + Nuevo usuario
        </button>
      </div>

      
      <section className="usuarios-resumen">

        <div className="usuario-resumen-card">
          <span>Total de usuarios</span>
          <strong>{totalUsuarios}</strong>
        </div>

        <div className="usuario-resumen-card">
          <span>Usuarios activos</span>
          <strong>{usuariosActivos}</strong>
        </div>

        <div className="usuario-resumen-card">
          <span>Usuarios inactivos</span>
          <strong>{usuariosInactivos}</strong>
        </div>

      </section>

      
      <section className="usuarios-panel">

        <div className="usuarios-panel-superior">
          <h2>Usuarios registrados</h2>

          <input
            type="search"
            className="usuarios-buscador"
            placeholder="Buscar por nombre, RUN o correo..."
            aria-label="Buscar usuarios"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

       
        <div className="usuarios-tabla-contenedor">
          <table className="usuarios-tabla">
            <thead>
              <tr>
                <th>RUN</th>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Teléfono</th>
                <th>Ubicación</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>

            <tbody>
              {usuariosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={8} className="usuario-vacio">
                    {error
                      ? error
                      : usuarios.length === 0
                      ? 'No hay usuarios registrados.'
                      : 'No se encontraron usuarios.'}
                  </td>
                </tr>
              ) : (
                usuariosFiltrados.map((u) => (
                  <tr key={u.id}>

                    <td>{u.run || u.rut || '-'}</td>

                    <td>
                      {`${u.nombre || ''} ${u.apellidos || ''}`.trim()}
                    </td>

                    <td>{u.correo}</td>

                    <td>{u.telefono || '-'}</td>

                    <td>
                      {[u.comuna, u.region]
                        .filter(Boolean)
                        .join(', ') || '-'}
                    </td>

                    <td>{u.rol}</td>

                    <td>
                      <span
                        className={`estado-usuario ${
                          u.activo === false
                            ? 'estado-inactivo'
                            : 'estado-activo'
                        }`}
                      >
                        {u.activo === false ? 'Inactivo' : 'Activo'}
                      </span>
                    </td>

                    <td>
                      <div className="acciones-usuario">

                        <button
                          type="button"
                          className="btn-editar-usuario"
                          onClick={() => abrirEdicion(u)}
                        >
                          Editar
                        </button>

                        <button
                          type="button"
                          className="btn-estado-usuario"
                          onClick={() => alternarActivo(u)}
                        >
                          {u.activo === false
                            ? 'Activar'
                            : 'Desactivar'}
                        </button>

                        <button
                          type="button"
                          className="btn-eliminar-usuario"
                          onClick={() => eliminar(u)}
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

      
      {usuarioEditando && createPortal(
        <div
          className="usuarios-modal-react"
          role="presentation"
          onClick={cerrarEdicion}
        >
          <div
            className="usuarios-modal-react-contenido"
            role="dialog"
            aria-modal="true"
            aria-labelledby="tituloEditarUsuario"
            onClick={(e) => e.stopPropagation()}
          >

            <div className="usuarios-modal-react-header">
              <h2 id="tituloEditarUsuario">
                {esNuevo ? 'Nuevo usuario' : 'Editar usuario'}
              </h2>

              <button
                type="button"
                onClick={cerrarEdicion}
                aria-label="Cerrar ventana"
              >
                ✕
              </button>
            </div>

            <form onSubmit={guardarEdicion}>

              <div className="usuarios-modal-react-grid">

                <div>
                  <label htmlFor="editar-run">RUN</label>
                  <input
                    id="editar-run"
                    name="run"
                    type="text"
                    placeholder="12345678-9"
                    value={usuarioEditando.run || ''}
                    onChange={cambiarCampo}
                    disabled={!esNuevo}
                    required
                  />
                </div>

                <div>
                  <label htmlFor="editar-rol">Rol</label>
                  <select
                    id="editar-rol"
                    name="rol"
                    value={usuarioEditando.rol}
                    onChange={cambiarCampo}
                    style={{
                      width: '100%',
                      padding: '12px 14px',
                      border: '1px solid #ddd',
                      borderRadius: '10px',
                      background: '#fff',
                    }}
                  >
                    <option value="ADMIN">Administrador</option>
                    <option value="VENDEDOR">Vendedor</option>
                    <option value="CLIENTE">Cliente</option>
                  </select>
                </div>

                <div>
                  <label htmlFor="editar-nombre">Nombre</label>
                  <input
                    id="editar-nombre"
                    name="nombre"
                    type="text"
                    value={usuarioEditando.nombre || ''}
                    onChange={cambiarCampo}
                    required
                  />
                </div>

                <div>
                  <label htmlFor="editar-apellidos">Apellidos</label>
                  <input
                    id="editar-apellidos"
                    name="apellidos"
                    type="text"
                    value={usuarioEditando.apellidos || ''}
                    onChange={cambiarCampo}
                    required
                  />
                </div>

                <div>
                  <label htmlFor="editar-correo">Correo</label>
                  <input
                    id="editar-correo"
                    name="correo"
                    type="email"
                    value={usuarioEditando.correo || ''}
                    onChange={cambiarCampo}
                    required
                  />
                </div>

                <div>
                  <label htmlFor="editar-contrasena">
                    {esNuevo ? 'Contraseña' : 'Nueva contraseña (opcional)'}
                  </label>
                  <input
                    id="editar-contrasena"
                    name="contrasena"
                    type="password"
                    autoComplete="new-password"
                    placeholder="5 a 10 caracteres, mayúscula y número"
                    value={usuarioEditando.contrasena || ''}
                    onChange={cambiarCampo}
                    required={esNuevo}
                  />
                </div>

                <div>
                  <label htmlFor="editar-telefono">Teléfono</label>
                  <input
                    id="editar-telefono"
                    name="telefono"
                    type="tel"
                    value={usuarioEditando.telefono || ''}
                    onChange={cambiarCampo}
                  />
                </div>

                <div>
                  <label htmlFor="editar-region">Región</label>
                  <input
                    id="editar-region"
                    name="region"
                    type="text"
                    value={usuarioEditando.region || ''}
                    onChange={cambiarCampo}
                  />
                </div>

                <div>
                  <label htmlFor="editar-comuna">Comuna</label>
                  <input
                    id="editar-comuna"
                    name="comuna"
                    type="text"
                    value={usuarioEditando.comuna || ''}
                    onChange={cambiarCampo}
                  />
                </div>

                <div>
                  <label htmlFor="editar-direccion">Dirección</label>
                  <input
                    id="editar-direccion"
                    name="direccion"
                    type="text"
                    value={usuarioEditando.direccion || ''}
                    onChange={cambiarCampo}
                  />
                </div>

              </div>

              <div className="usuarios-modal-react-botones">

                <button
                  type="button"
                  onClick={cerrarEdicion}
                >
                  Cancelar
                </button>

                <button type="submit">
                  Guardar cambios
                </button>

              </div>

            </form>
          </div>
        </div>,
        document.body
      )}

    </div>
  );
}
