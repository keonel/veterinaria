export default function AcercaDe() {
  return (
    <main>
      <section className="acerca-presentacion">
        <div className="acerca-titulo">
          <h1>Acerca de Nosotros</h1>
          <p>
            Conoce más sobre Veterinaria San Marcos y nuestro compromiso con el
            bienestar de tus mascotas.
          </p>
        </div>
        <div className="acerca-contenido">
          <div className="acerca-imagen">
            <img
              alt="Veterinaria San Marcos"
              src="/assets/img/nuevavida-adopciones-protectora-madrid.png"
            />
          </div>
          <div className="acerca-texto">
            <h2>¿Quiénes somos?</h2>
            <p>
              Veterinaria San Marcos nace en 2026 con una idea tan sencilla como
              profunda: cuidar el bienestar de los animales que, con el tiempo,
              han dejado de ser solo mascotas para convertirse en parte esencial
              de nuestras familias. Desde el inicio, este proyecto se construye
              sobre un respeto genuino por la vida animal y sobre la convicción
              de que la medicina veterinaria no puede limitarse a diagnosticar y
              tratar enfermedades; debe también reconocer y acompañar el vínculo
              afectivo que une a cada paciente con quienes lo cuidan.
            </p>
            <p>
              Ubicada en el corazón de Viña del Mar, en Av. Libertad 1112, la
              clínica abre sus puertas con el propósito de ser un espacio donde
              la excelencia médica y la sensibilidad humana converjan de manera
              natural. Entendemos que una atención veterinaria de calidad exige
              más que conocimientos técnicos: requiere paciencia, empatía,
              comunicación clara y un trato digno hacia cada animal y su tutor.
            </p>

            <p>
              Nuestra labor se sustenta en una concepción integral del cuidado.
              Procuramos que cada consulta, procedimiento o instancia de
              atención se desarrolle en un ambiente seguro, respetuoso y lo
              menos estresante posible. Aspiramos a que la experiencia clínica
              no se asocie al miedo o la incertidumbre, sino a la confianza de
              saberse en un lugar donde la salud y el bienestar del animal son
              el centro de toda decisión.
            </p>
            
          </div>
        </div>
      </section>
      <section className="acerca-mision">
        <h2>Nuestra misión</h2>
        <p>
          Brindar atención veterinaria de calidad, preocupándonos por la salud y
          el bienestar de cada mascota que llega a nosotros.
        </p>
      </section>
      <section className="acerca-valores">
        <h2>Nuestros valores</h2>
        <div className="valores-container">
          <div className="valor-card">
            <img
              alt="Compromiso con las mascotas"
              className="imagen-valor"
              src="/assets/img/compromiso.jpg"
            />
            <h3>Compromiso</h3>
            <p>Nos comprometemos con el cuidado y la salud de cada mascota.</p>
          </div>
          <div className="valor-card">
            <img
              alt="Bienestar animal"
              className="imagen-valor"
              src="/assets/img/1787374654826-bienestar-animal-como-cuidar-la-salud-y-calidad-de-vida-de-tu-mascota-en-arauco-santa-juana-y-alrededores.webp"
            />
            <h3>Bienestar animal</h3>
            <p>El bienestar de los animales es nuestra principal prioridad.</p>
          </div>
          <div className="valor-card">
            <img
              alt="Atención cercana a las familias"
              className="imagen-valor"
              src="/assets/img/popa.jpeg"
            />
            <h3>Cercanía</h3>
            <p>
              Buscamos entregar una atención amable y cercana a cada familia.
            </p>
          </div>
        </div>
      </section>
      <section className="acerca-servicios">
        <h2>Nuestros servicios</h2>
        <p className="servicios-descripcion">
          Ofrecemos distintos servicios para cuidar la salud y bienestar de tu
          mascota.
        </p>
        <div className="servicios-acerca-container">
          <div className="servicio-acerca-card">
            <img
              alt="Consulta veterinaria"
              className="imagen-servicio-acerca"
              src="/assets/img/consulta_veterinaria.jpg"
            />
            <h3>Consulta veterinaria</h3>
            <p>
              Evaluamos la salud de tu mascota y entregamos la atención
              necesaria para su bienestar.
            </p>
          </div>
          <div className="servicio-acerca-card">
            <img
              alt="Vacunación de mascotas"
              className="imagen-servicio-acerca"
              src="/assets/img/vacunacion.png"
            />
            <h3>Vacunación</h3>
            <p>
              Protegemos a tu mascota mediante vacunas y controles preventivos.
            </p>
          </div>
          <div className="servicio-acerca-card">
            <img
              alt="Control preventivo de mascotas"
              className="imagen-servicio-acerca"
              src="/assets/img/VETERINARIO-CUATRO.jpg"
            />
            <h3>Control preventivo</h3>
            <p>
              Realizamos controles periódicos para ayudar a mantener a tu
              mascota saludable.
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}
