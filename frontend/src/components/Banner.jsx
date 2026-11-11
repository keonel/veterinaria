import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

const slides = [
  {
    clase: "slide-1",
    subtitulo: "Veterinaria San Marcos",
    titulo: "Cuidamos la salud de tu mascota",
    texto: "Atención integral para perros, gatos, conejos en Viña del Mar.",
    boton: "Agendar Cita en Línea",
    destino: "/solicitar-cita",
  },
  {
    clase: "slide-2",
    subtitulo: "Atención profesional",
    titulo: "La salud de tu mascota es nuestra prioridad",
    texto: "Entregamos atención veterinaria responsable y cercana.",
    boton: "Solicitar Cita",
    destino: "/solicitar-cita",
  },
  {
    clase: "slide-3",
    subtitulo: "Nuestros servicios",
    titulo: "Todo lo que tu mascota necesita",
    texto: "Consulta general, vacunación, desparasitación y cirugía menor.",
    boton: "Ver Servicios",
    destino: "/#servicios",
  },
];

export default function Banner() {
  const [actual, setActual] = useState(0);


  useEffect(() => {
    const intervalo = setInterval(() => {
      setActual((anterior) => (anterior + 1) % slides.length);
    }, 5000);
    return () => clearInterval(intervalo);
  }, [actual]);

  return (
    <section className="banner">
      {slides.map((slide, indice) => (
        <div
          key={slide.clase}
          className={`banner-slide ${slide.clase} ${indice === actual ? "active" : ""}`}
        >
          <div className="banner-content">
            <p className="banner-subtitle">{slide.subtitulo}</p>
            <h1>{slide.titulo}</h1>
            <p>{slide.texto}</p>
            <Link to={slide.destino} className="banner-boton">
              {slide.boton}
            </Link>
          </div>
        </div>
      ))}

      <div className="banner-indicators">
        {slides.map((slide, indice) => (
          <span
            key={slide.clase}
            className={`indicator ${indice === actual ? "active" : ""}`}
            onClick={() => setActual(indice)}
          ></span>
        ))}
      </div>
    </section>
  );
}
