import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import useFormulario from '../hooks/useFormulario.js';
import { api } from '../services/api.js';


function fechaHoy() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

function validar(v) {
  const e = {};
  if (!v.nombreTutor.trim()) e.nombreTutor = 'Ingresa el nombre del tutor';
  if (!v.rut.trim()) e.rut = 'Ingresa el RUN del tutor';
  if (!v.telefono.trim()) e.telefono = 'Ingresa un teléfono';
  if (!v.correo.trim()) e.correo = 'Ingresa un correo';
  if (!v.nombreMascota.trim()) e.nombreMascota = 'Ingresa el nombre de la mascota';
  if (!v.especie) e.especie = 'Selecciona la especie';
  if (!v.sexo) e.sexo = 'Selecciona el sexo';
  if (!v.motivo) e.motivo = 'Selecciona el motivo';
  if (!v.fechaCita) e.fechaCita = 'Selecciona la fecha';
  if (!v.horaCita) e.horaCita = 'Selecciona la hora';
  if (!v.terminos) e.terminos = 'Debes aceptar los términos y condiciones';
  return e;
}

export default function SolicitarCita() {
  const { usuario } = useAuth();
  const navegar = useNavigate();

  const inicial = {
    nombreTutor: `${usuario?.nombre || ''} ${usuario?.apellidos || ''}`.trim(),
    rut: usuario?.run || usuario?.rut || '',
    telefono: usuario?.telefono || '',
    correo: usuario?.correo || '',
    nombreMascota: '',
    especie: '',
    raza: '',
    fechaNacimiento: '',
    sexo: '',
    esterilizado: '',
    motivo: '',
    descripcion: '',
    veterinario: 'Sin preferencia',
    fechaCita: '',
    horaCita: '',
    terminos: false,
  };

  const { valores, errores, manejarCambio, enviar } = useFormulario(inicial, validar);

  const guardar = enviar(async (v) => {
    try {
      // El backend genera el código, deja la cita PENDIENTE y la asocia al usuario del JWT.
      // También aplica las reglas: fecha no pasada y veterinario sin tope de horario (409).
      const cita = await api('/citas', {
        method: 'POST',
        body: {
          cliente: v.nombreTutor.trim(),
          rut: v.rut.trim(),
          telefono: v.telefono.trim(),
          correo: v.correo.trim(),
          mascota: v.nombreMascota.trim(),
          especie: v.especie,
          raza: v.raza.trim(),
          fechaNacimiento: v.fechaNacimiento || null,
          sexo: v.sexo,
          esterilizado: v.esterilizado,
          servicio: v.motivo,
          descripcion: v.descripcion.trim(),
          veterinario: v.veterinario,
          fecha: v.fechaCita,
          hora: v.horaCita,
        },
      });
      alert(`Cita solicitada correctamente. Código: ${cita.codigo}`);
      navegar('/mis-citas');
    } catch (error) {
      alert([error.message, ...Object.values(error.campos || {})].join('\n'));
    }
  });

  function campo({ id, titulo, tipo = 'text', placeholder = '', requerido = false, min, opciones, filas = 4 }) {
    const error = errores[id];
    const propiedades = {
      id, name: id, value: valores[id] ?? '', onChange: manejarCambio,
      className: error ? 'cita-input-error' : '',
      'aria-invalid': Boolean(error),
      'aria-describedby': error ? `error-${id}` : undefined,
    };
    return (
      <div className="cita-campo" key={id}>
        <label htmlFor={id}>{titulo}{requerido && <span className="cita-asterisco"> *</span>}</label>
        {opciones ? (
          <select {...propiedades}>
            {opciones.map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
          </select>
        ) : tipo === 'textarea' ? (
          <textarea {...propiedades} rows={filas} placeholder={placeholder} />
        ) : (
          <input {...propiedades} type={tipo} placeholder={placeholder} min={min} />
        )}
        {error && <small id={`error-${id}`} className="mensaje-error">{error}</small>}
      </div>
    );
  }

  return (
    <section className="cita-pagina">
      <header className="cita-intro">
        <h1>Solicitar Cita</h1>
        <p>Completa el formulario para agendar una cita para tu mascota.</p>
        <small><span className="cita-asterisco">*</span> Campos obligatorios</small>
      </header>

      <form className="cita-tarjeta" id="formCita" onSubmit={guardar} noValidate>
        <fieldset className="cita-seccion">
          <legend>Datos del tutor</legend>
          <div className="cita-grid">
            {campo({ id: 'nombreTutor', titulo: 'Nombre completo', requerido: true })}
            {campo({ id: 'rut', titulo: 'RUT', requerido: true })}
            {campo({ id: 'telefono', titulo: 'Teléfono', tipo: 'tel', requerido: true })}
            {campo({ id: 'correo', titulo: 'Correo electrónico', tipo: 'email', requerido: true })}
          </div>
        </fieldset>

        <fieldset className="cita-seccion">
          <legend>Datos de la mascota</legend>
          <div className="cita-grid">
            {campo({ id: 'nombreMascota', titulo: 'Nombre', requerido: true, placeholder: 'Nombre de tu mascota' })}
            {campo({ id: 'especie', titulo: 'Especie', requerido: true, opciones: [['', 'Selecciona una especie'], ['Perro', 'Perro'], ['Gato', 'Gato'], ['Conejo', 'Conejo'], ['Otro', 'Otro']] })}
            {campo({ id: 'raza', titulo: 'Raza', placeholder: 'Ej: Labrador' })}
            {campo({ id: 'fechaNacimiento', titulo: 'Fecha de nacimiento', tipo: 'date', min: undefined })}
            {campo({ id: 'sexo', titulo: 'Sexo', requerido: true, opciones: [['', 'Selecciona una opción'], ['Macho', 'Macho'], ['Hembra', 'Hembra']] })}
            {campo({ id: 'esterilizado', titulo: '¿Está esterilizado/a?', opciones: [['', 'Selecciona una opción'], ['Sí', 'Sí'], ['No', 'No']] })}
          </div>
        </fieldset>

        <fieldset className="cita-seccion">
          <legend>Datos de la cita</legend>
          <div className="cita-grid">
            <div className="cita-ancho-completo">
              {campo({ id: 'motivo', titulo: 'Motivo de consulta', requerido: true, opciones: [['', 'Selecciona un motivo'], ['Consulta General', 'Consulta General'], ['Vacunación y Desparasitación', 'Vacunación y Desparasitación'], ['Cirugía Menor', 'Cirugía Menor']] })}
            </div>
            <div className="cita-ancho-completo">
              {campo({ id: 'descripcion', titulo: 'Descripción del motivo', tipo: 'textarea', placeholder: 'Cuéntanos brevemente el motivo de la consulta...' })}
            </div>
            {campo({ id: 'veterinario', titulo: 'Veterinario/a', opciones: [['Sin preferencia', 'Sin preferencia'], ['Dr. Roberto Soto', 'Dr. Roberto Soto']] })}
            {campo({ id: 'fechaCita', titulo: 'Fecha', tipo: 'date', min: fechaHoy(), requerido: true })}
            {campo({ id: 'horaCita', titulo: 'Hora', tipo: 'time', requerido: true })}
          </div>
        </fieldset>

        <div className="cita-terminos">
          <label htmlFor="terminos">
            <input id="terminos" name="terminos" type="checkbox" checked={Boolean(valores.terminos)} onChange={manejarCambio} />
            <span>Acepto los términos y condiciones <span className="cita-asterisco">*</span></span>
          </label>
          {errores.terminos && <small className="mensaje-error">{errores.terminos}</small>}
        </div>
        <button className="cita-boton" type="submit">Agendar Cita</button>
      </form>
    </section>
  );
}
