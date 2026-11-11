import { useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Banner from '../components/Banner.jsx';
import ProductoCard from '../components/ProductoCard.jsx';
import { useTienda } from '../context/TiendaContext.jsx';

const servicios = [
  {
    imagen: '/assets/img/images.jpg',
    alt: 'Consulta médica veterinaria',
    titulo: 'Consulta General desde',
    precio: '$7.990',
    texto: 'Evaluación médica preventiva y diagnóstico oportuno para tu mascota.',
  },
  {
    imagen: '/assets/img/WhatsApp-Image-2023-12-01-at-11.50.35-2.jpeg',
    alt: 'Vacunación de mascotas',
    titulo: 'Vacunación y Desparasitación',
    precio: '$10.990',
    texto: 'Mantén el esquema al día para proteger la salud de tus animales domésticos.',
  },
  {
    imagen: '/assets/img/CIRUGIA.jpg',
    alt: 'Cirugía menor',
    titulo: 'Cirugía Menor desde',
    precio: '$50.990',
    texto: 'Procedimientos quirúrgicos seguros realizados por profesionales altamente calificados.',
  },
];

export default function Inicio() {
 const { productos, agregarAlCarrito } = useTienda();
  
  const { hash } = useLocation();

  
  useEffect(() => {
    if (hash) {
      document.querySelector(hash)?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [hash]);

  const productosTienda = productos.filter(
    (p) => p.tipo === 'producto' && p.activo !== false
  );

  return (
    <>
      <Banner />

      <section id="tienda" className="section">
        <div className="section-title">
          <p className="section-subtitle">Nuestra Tienda</p>
          <h2>Productos para tu Mascota</h2>
        </div>

        <div className="productos-grid">
          {productosTienda.map((producto) => (
            <ProductoCard
              key={producto.codigo}
              producto={producto}
              alAgregar={agregarAlCarrito}
            />
          ))}
        </div>
      </section>

      <section id="servicios" className="section">
        <div className="section-title">
          <p className="section-subtitle">Lo que ofrecemos</p>
          <h2>Nuestros Servicios</h2>
          <p>
            Contamos con diferentes servicios para cuidar la salud y
            bienestar de tus mascotas.
          </p>
        </div>

        <div className="grid-container">
          {servicios.map((servicio) => (
            <article className="card" key={servicio.titulo}>
              <div className="card-image">
                <img src={servicio.imagen} alt={servicio.alt} />
              </div>
              <div className="card-content">
                <h3>{servicio.titulo}</h3>
                <h3>{servicio.precio}</h3>
                <p>{servicio.texto}</p>
                <Link to="/solicitar-cita" className="card-link">
                  Solicitar cita →
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section id="ubicacion" className="section location-section">
        <div className="section-title">
          <p className="section-subtitle">Dónde encontrarnos</p>
          <h2>Ubicación</h2>
          <p>Encuéntranos en Viña del Mar, Región de Valparaíso.</p>
        </div>

        <div className="location-container">
          <div className="location-info">
            <h3>Veterinaria San Marcos</h3>
            <p style={{ color: 'rgb(61, 104, 153)' }}>📍 Av. Libertad #1112, Viña del Mar, Región de Valparaíso.</p>
            <p style={{ color: 'rgb(61, 104, 153)' }}>📞 (+56) 9 3083 4722</p>
            <p style={{ color: 'rgb(61, 104, 153)' }}>📩 vet.sanmarcos@gmail.com</p>
            <p style={{ color: 'rgb(61, 104, 153)' }}>📷 @Vet_sanmarcos_viña</p>
            <p style={{ color: 'rgb(61, 104, 153)' }}>Atención veterinaria para perros, gatos, conejos.</p>
          </div>

          <div className="map-placeholder">
            <a href="https://maps.app.goo.gl/M1L8xR1CCYgmtftR7">
              <img
                src="/assets/img/mapa-ubicacion.png"
                alt="Ubicación de Veterinaria San Marcos"
                width="600"
                height="400"
              />
            </a>
          </div>
        </div>
      </section>
    </>
  );
}