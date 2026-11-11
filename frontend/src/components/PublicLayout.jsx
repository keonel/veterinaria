import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header.jsx';
import Footer from './Footer.jsx';
import CarritoPanel from './CarritoPanel.jsx';
import { useTienda } from '../context/TiendaContext.jsx';

export default function PublicLayout() {
  const [carritoAbierto, setCarritoAbierto] = useState(false);
  const { cantidadTotal } = useTienda();

  return (
    <>
      <Header
        cantidadCarrito={cantidadTotal}
        alAbrirCarrito={() => setCarritoAbierto(true)}
      />
      <CarritoPanel
        abierto={carritoAbierto}
        alCerrar={() => setCarritoAbierto(false)}
      />
      <main>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}