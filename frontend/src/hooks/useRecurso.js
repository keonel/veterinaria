import { useCallback, useEffect, useState } from 'react';
import { api } from '../services/api.js';

// Carga una lista desde el backend y expone su estado.
//   const { datos, cargando, error, recargar } = useRecurso('/citas');
// Reemplaza al antiguo useLocalStorage en las pantallas de administración:
// ahora la fuente de datos es la API y no el navegador.
export default function useRecurso(ruta) {
  const [datos, setDatos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  // Carga inicial al mostrar la pantalla
  useEffect(() => {
    let vigente = true; // evita actualizar el estado si la pantalla ya se cerró
    api(ruta)
      .then((respuesta) => {
        if (!vigente) return;
        setDatos(respuesta);
        setError('');
      })
      .catch((e) => {
        if (vigente) setError(e.message);
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });
    return () => {
      vigente = false;
    };
  }, [ruta]);

  // Vuelve a pedir la lista después de crear, editar o eliminar
  const recargar = useCallback(async () => {
    try {
      setDatos(await api(ruta));
      setError('');
    } catch (e) {
      setError(e.message);
    }
  }, [ruta]);

  return { datos, cargando, error, recargar };
}
