import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import useFormulario from '../hooks/useFormulario.js';
import { api } from '../services/api.js';
import { validarCorreo, validarTelefono } from '../utils/validaciones.js';

function validar(v) {
  const e = {};

  if (v.nombre.trim() === '') {
    e.nombre = 'Ingresa tu nombre';
  }

  if (!validarCorreo(v.correo.trim())) {
    e.correo = 'Correo no válido';
  }

  if (v.telefono.trim() !== '' && !validarTelefono(v.telefono.trim())) {
    e.telefono = 'Teléfono no válido (entre 8 y 11 dígitos)';
  }

  if (v.asunto.trim() === '') {
    e.asunto = 'Ingresa un asunto';
  }

  if (v.mensaje.trim() === '') {
    e.mensaje = 'Escribe tu mensaje';
  }

  return e;
}

// Al recargar la página la sesión se recupera de forma asíncrona. Con "key", el formulario
// se vuelve a crear cuando llega el usuario y así aparece con sus datos precargados.
export default function Contacto() {
  const { usuario } = useAuth();
  return <FormularioContacto key={usuario?.id ?? 'visitante'} usuario={usuario} />;
}

function FormularioContacto({ usuario }) {

  const inicial = {
    nombre: `${usuario?.nombre || ''} ${usuario?.apellidos || ''}`.trim(),
    correo: usuario?.correo || '',
    telefono: usuario?.telefono || '',
    asunto: '',
    mensaje: '',
  };

  const {
    valores,
    setValores,
    errores,
    manejarCambio,
    enviar,
  } = useFormulario(inicial, validar);

  async function guardarMensaje(v) {
    try {
      // Endpoint público: cualquier visitante puede escribir, sin iniciar sesión
      await api('/mensajes', {
        method: 'POST',
        body: {
          nombre: v.nombre.trim(),
          correo: v.correo.trim(),
          telefono: v.telefono.trim(),
          asunto: v.asunto.trim(),
          mensaje: v.mensaje.trim(),
        },
      });

      alert('Mensaje enviado correctamente.');

      setValores((actual) => ({
        ...actual,
        asunto: '',
        mensaje: '',
      }));
    } catch (error) {
      alert([error.message, ...Object.values(error.campos || {})].join('\n'));
    }
  }

  function manejarEnvio(evento) {
    enviar(guardarMensaje)(evento);
  }

  return (
    <main className="contacto-pagina">
      <section className="contacto">
        <h1>Contáctanos</h1>

        <p>
          Estamos aquí para ayudarte y cuidar de tu mascota.
        </p>

        <div className="contacto-contenido">
          <div className="informacion-contacto">
            <h2>Información de contacto</h2>

            <p>📍 Viña del Mar, Chile</p>
            <p>📞 (+56) 9 3083 4722</p>
            <p>✉️ veterinaria@sanmarcos.cl</p>
            <p>🕐 Lunes a viernes: 09:00 - 18:00</p>

            <img
              src="/assets/img/kitten-gifs.gif"
              alt="Gatito de Veterinaria San Marcos"
              className="imagen-contacto"
            />
          </div>

          <div className="formulario-contacto">
            <h2>Envíanos un mensaje</h2>

            <form
              id="formContacto"
              onSubmit={manejarEnvio}
              noValidate
            >
              <div className="form-group">
                <label htmlFor="nombre">
                  Nombre *
                </label>

                <input
                  id="nombre"
                  name="nombre"
                  type="text"
                  value={valores.nombre}
                  onChange={manejarCambio}
                  className={errores.nombre ? 'error' : ''}
                />

                {errores.nombre && (
                  <small className="mensaje-error">
                    {errores.nombre}
                  </small>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="correo">
                  Correo electrónico *
                </label>

                <input
                  id="correo"
                  name="correo"
                  type="email"
                  value={valores.correo}
                  onChange={manejarCambio}
                  className={errores.correo ? 'error' : ''}
                />

                {errores.correo && (
                  <small className="mensaje-error">
                    {errores.correo}
                  </small>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="telefono">
                  Teléfono
                </label>

                <input
                  id="telefono"
                  name="telefono"
                  type="tel"
                  placeholder="+56 9 1234 5678"
                  value={valores.telefono}
                  onChange={manejarCambio}
                  className={errores.telefono ? 'error' : ''}
                />

                {errores.telefono && (
                  <small className="mensaje-error">
                    {errores.telefono}
                  </small>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="asunto">
                  Asunto *
                </label>

                <input
                  id="asunto"
                  name="asunto"
                  type="text"
                  value={valores.asunto}
                  onChange={manejarCambio}
                  className={errores.asunto ? 'error' : ''}
                />

                {errores.asunto && (
                  <small className="mensaje-error">
                    {errores.asunto}
                  </small>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="mensaje">
                  Mensaje *
                </label>

                <textarea
                  id="mensaje"
                  name="mensaje"
                  rows="4"
                  value={valores.mensaje}
                  onChange={manejarCambio}
                  className={errores.mensaje ? 'error' : ''}
                />

                {errores.mensaje && (
                  <small className="mensaje-error">
                    {errores.mensaje}
                  </small>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ marginTop: '1rem' }}
              >
                Enviar mensaje
              </button>
            </form>
          </div>
        </div>
      </section>

      <section className="ubicacion">
        <h2>Nuestra ubicación</h2>

        <div className="map-placeholder">
          <a
            href="https://maps.app.goo.gl/M1L8xR1CCYgmtftR7"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img
              src="/assets/img/mapa-ubicacion.png"
              alt="Mapa de ubicación de Veterinaria San Marcos"
            />
          </a>
        </div>
      </section>

      <section className="solicitar-cita">
        <h2>¿Necesitas una cita?</h2>

        <p>
          Agenda una atención para tu mascota con nuestro equipo.
        </p>

        <Link to="/solicitar-cita">
          Solicitar cita
        </Link>
      </section>
    </main>
  );
}
