import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const opciones = [
  { nombre: 'Ir a la tienda', descripcion: 'Ver productos para tu mascota', tipo: 'Tienda', ruta: '/#tienda' },
  { nombre: 'Milpro 2.5 mg', descripcion: 'Antiparasitario para perros y gatos', tipo: 'Producto', ruta: '/#tienda' },
  { nombre: 'Shampoo Medicado FLT', descripcion: 'Shampoo para perros y gatos', tipo: 'Producto', ruta: '/#tienda' },
  { nombre: 'Alimento para conejos Puki', descripcion: 'Alimento balanceado de 1 kg', tipo: 'Producto', ruta: '/#tienda' },
  { nombre: 'Ver servicios', descripcion: 'Conoce nuestros servicios veterinarios', tipo: 'Servicios', ruta: '/#servicios' },
  { nombre: 'Consulta General', descripcion: 'Evaluación médica veterinaria', tipo: 'Servicio', ruta: '/#servicios' },
  { nombre: 'Vacunación y Desparasitación', descripcion: 'Protección y prevención para tu mascota', tipo: 'Servicio', ruta: '/#servicios' },
  { nombre: 'Cirugía Menor', descripcion: 'Procedimientos veterinarios', tipo: 'Servicio', ruta: '/#servicios' },
  { nombre: 'Agendar cita', descripcion: 'Reserva una atención veterinaria', tipo: 'Cita', ruta: '/solicitar-cita' },
  { nombre: 'Mis citas', descripcion: 'Revisa tus citas veterinarias', tipo: 'Página', ruta: '/mis-citas' },
  { nombre: 'Contactarnos', descripcion: 'Envíanos un mensaje', tipo: 'Contacto', ruta: '/contacto' },
  { nombre: 'Acerca de nosotros', descripcion: 'Conoce más sobre Veterinaria San Marcos', tipo: 'Página', ruta: '/acerca-de' },
  { nombre: 'Ver ubicación', descripcion: 'Encuentra nuestra veterinaria', tipo: 'Ubicación', ruta: '/#ubicacion' },
];

const recomendaciones = [
  { nombre: '🛒 Ir a la tienda', descripcion: 'Ver productos para tu mascota', tipo: 'Tienda', ruta: '/#tienda' },
  { nombre: '🩺 Ver servicios', descripcion: 'Conoce nuestros servicios veterinarios', tipo: 'Servicios', ruta: '/#servicios' },
  { nombre: '📅 Agendar cita', descripcion: 'Reserva una atención veterinaria', tipo: 'Cita', ruta: '/solicitar-cita' },
  { nombre: '📍 Ver ubicación', descripcion: 'Encuentra nuestra veterinaria', tipo: 'Ubicación', ruta: '/#ubicacion' },
];

// Ignora mayúsculas y tildes ("cirugia" encuentra "Cirugía")
function normalizar(texto) {
  return texto.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

export default function Buscador() {
  const [texto, setTexto] = useState('');
  const [abierto, setAbierto] = useState(false);
  const contenedor = useRef(null);
  const navegar = useNavigate();

  const consulta = normalizar(texto.trim());
  const sugerencias =
    consulta === ''
      ? recomendaciones
      : opciones.filter((o) =>
          normalizar(`${o.nombre} ${o.descripcion} ${o.tipo}`).includes(consulta)
        );

  function ir(ruta) {
    setAbierto(false);
    setTexto('');
    navegar(ruta);
  }

  function buscarPrimerResultado() {
    if (sugerencias.length > 0) ir(sugerencias[0].ruta);
  }

  // Cierra el panel al hacer clic fuera del buscador
  useEffect(() => {
    function alHacerClicFuera(evento) {
      if (contenedor.current && !contenedor.current.contains(evento.target)) {
        setAbierto(false);
      }
    }
    document.addEventListener('mousedown', alHacerClicFuera);
    return () => document.removeEventListener('mousedown', alHacerClicFuera);
  }, []);

  return (
    <div className="search-box" ref={contenedor}>
      <input
        type="text"
        id="searchInput"
        placeholder="Buscar..."
        autoComplete="off"
        value={texto}
        onChange={(e) => {
          setTexto(e.target.value);
          setAbierto(true);
        }}
        onFocus={() => setAbierto(true)}
        onKeyDown={(e) => e.key === 'Enter' && buscarPrimerResultado()}
      />
      <button
        type="button"
        id="searchButton"
        aria-label="Buscar"
        onClick={buscarPrimerResultado}
      >
        🔍
      </button>

      <div
        id="searchSuggestions"
        className={`search-suggestions ${abierto ? 'active' : ''}`}
      >
        {sugerencias.length === 0 ? (
          <div className="search-suggestion-item">
            <span className="search-suggestion-info">
              <strong>Sin resultados</strong>
              <small>Prueba con otra palabra</small>
            </span>
          </div>
        ) : (
          sugerencias.map((opcion) => (
            <div
              key={opcion.nombre}
              className="search-suggestion-item"
              onClick={() => ir(opcion.ruta)}
            >
              <span className="search-suggestion-info">
                <strong>{opcion.nombre}</strong>
                <small>{opcion.descripcion}</small>
              </span>
              <span className="search-suggestion-type">{opcion.tipo}</span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}