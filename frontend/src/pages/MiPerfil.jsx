import { useAuth } from '../context/AuthContext.jsx';
import useFormulario from '../hooks/useFormulario.js';
import { api } from '../services/api.js';
import { regiones } from '../data/regiones.js';
import { validarCorreo, validarTelefono } from '../utils/validaciones.js';

function validar(v) {
  const e = {};

  if (!v.nombre.trim()) {
    e.nombre = 'El nombre es obligatorio';
  }

  if (!v.apellidos.trim()) {
    e.apellidos = 'Los apellidos son obligatorios';
  }

  if (!validarCorreo(v.correo.trim())) {
    e.correo = 'Correo no válido';
  }

  if (
    v.telefono.trim() !== '' &&
    !validarTelefono(v.telefono.trim())
  ) {
    e.telefono = 'Teléfono no válido (entre 8 y 11 dígitos)';
  }

  if (!v.region) e.region = 'Selecciona una región';
  if (!v.comuna) e.comuna = 'Selecciona una comuna';

  if (!v.direccion.trim()) {
    e.direccion = 'La dirección es obligatoria';
  }

  return e;
}

export default function MiPerfil() {
  const { usuario, actualizarUsuario } = useAuth();

  const inicial = {
    run: usuario?.run || usuario?.rut || '',
    nombre: usuario?.nombre || '',
    apellidos: usuario?.apellidos || '',
    correo: usuario?.correo || '',
    telefono: usuario?.telefono || '',
    region: usuario?.region || '',
    comuna: usuario?.comuna || '',
    direccion: usuario?.direccion || '',
  };

  const {
    valores,
    setValores,
    errores,
    manejarCambio,
    enviar,
  } = useFormulario(inicial, validar);

  const comunas = regiones[valores.region] || [];

  function cambiarRegion(evento) {
    setValores((v) => ({
      ...v,
      region: evento.target.value,
      comuna: '',
    }));
  }

  const guardar = enviar(async (v) => {
    try {
      // PUT /auth/me: el backend sabe quién es el usuario por el JWT, no por el formulario.
      // Las citas siguen vinculadas porque se asocian al id del usuario, no a su correo.
      const actualizado = await api('/auth/me', {
        method: 'PUT',
        body: {
          nombre: v.nombre.trim(),
          apellidos: v.apellidos.trim(),
          correo: v.correo.trim().toLowerCase(),
          telefono: v.telefono.trim(),
          region: v.region,
          comuna: v.comuna,
          direccion: v.direccion.trim(),
        },
      });

      actualizarUsuario(actualizado);
      alert('Perfil actualizado correctamente.');
    } catch (error) {
      alert([error.message, ...Object.values(error.campos || {})].join('\n'));
    }
  });

  function campo({
    id,
    titulo,
    tipo = 'text',
    deshabilitado = false,
  }) {
    return (
      <div className="perfil-campo">
        <label htmlFor={id}>{titulo}</label>

        <input
          id={id}
          name={id}
          type={tipo}
          value={valores[id] ?? ''}
          onChange={manejarCambio}
          disabled={deshabilitado}
          className={errores[id] ? 'perfil-error' : ''}
        />

        {errores[id] && (
          <small className="mensaje-error">
            {errores[id]}
          </small>
        )}
      </div>
    );
  }

  return (
    <section className="perfil-pagina">
      <div className="perfil-contenedor">
        <div className="perfil-encabezado">
          <h1>Mi Perfil</h1>
          <p>Revisa y actualiza tus datos personales.</p>
        </div>

        <form
          id="formPerfil"
          className="perfil-tarjeta"
          onSubmit={guardar}
          noValidate
        >
          <div className="perfil-grid">
            {campo({
              id: 'run',
              titulo: 'RUN',
              deshabilitado: true,
            })}

            {campo({
              id: 'nombre',
              titulo: 'Nombre',
            })}

            {campo({
              id: 'apellidos',
              titulo: 'Apellidos',
            })}

            {campo({
              id: 'correo',
              titulo: 'Correo',
              tipo: 'email',
            })}

            {campo({
              id: 'telefono',
              titulo: 'Teléfono',
              tipo: 'tel',
            })}

            <div className="perfil-campo">
              <label htmlFor="region">Región</label>

              <select
                id="region"
                name="region"
                value={valores.region}
                onChange={cambiarRegion}
                className={
                  errores.region ? 'perfil-error' : ''
                }
              >
                <option value="">
                  Selecciona una región
                </option>

                {Object.keys(regiones).map((region) => (
                  <option key={region} value={region}>
                    {region}
                  </option>
                ))}
              </select>

              {errores.region && (
                <small className="mensaje-error">
                  {errores.region}
                </small>
              )}
            </div>

            <div className="perfil-campo">
              <label htmlFor="comuna">Comuna</label>

              <select
                id="comuna"
                name="comuna"
                value={valores.comuna}
                onChange={manejarCambio}
                disabled={!valores.region}
                className={
                  errores.comuna ? 'perfil-error' : ''
                }
              >
                <option value="">
                  Selecciona una comuna
                </option>

                {comunas.map((comuna) => (
                  <option key={comuna} value={comuna}>
                    {comuna}
                  </option>
                ))}
              </select>

              {errores.comuna && (
                <small className="mensaje-error">
                  {errores.comuna}
                </small>
              )}
            </div>

            <div className="perfil-campo perfil-ancho-completo">
              <label htmlFor="direccion">Dirección</label>

              <input
                id="direccion"
                name="direccion"
                type="text"
                value={valores.direccion}
                onChange={manejarCambio}
                className={
                  errores.direccion ? 'perfil-error' : ''
                }
              />

              {errores.direccion && (
                <small className="mensaje-error">
                  {errores.direccion}
                </small>
              )}
            </div>
          </div>

          <div className="perfil-acciones">
            <button
              type="button"
              className="perfil-btn-cancelar"
              onClick={() => setValores({
                run: usuario?.run || usuario?.rut || '',
                nombre: usuario?.nombre || '',
                apellidos: usuario?.apellidos || '',
                correo: usuario?.correo || '',
                telefono: usuario?.telefono || '',
                region: usuario?.region || '',
                comuna: usuario?.comuna || '',
                direccion: usuario?.direccion || '',
              })}
            >
              Cancelar
            </button>

            <button
              type="submit"
              className="perfil-btn-guardar"
            >
              Guardar cambios
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}
