import { useState } from 'react';

// valoresIniciales: objeto con un valor por campo
// validar(valores): devuelve un objeto SOLO con los campos que tienen error,
// ej. { correo: 'Correo no válido' }
export default function useFormulario(valoresIniciales, validar) {
  const [valores, setValores] = useState(valoresIniciales);
  const [errores, setErrores] = useState({});

  function manejarCambio(evento) {
    const { name, value, type, checked } = evento.target;
    setValores((actual) => ({
      ...actual,
      [name]: type === 'checkbox' ? checked : value,
    }));
    // limpiarError(): al escribir, desaparece el error de ese campo
    setErrores((actual) => ({ ...actual, [name]: undefined }));
  }

  // Uso: <form onSubmit={enviar((datos) => { ... })}>
  function enviar(alSerValido) {
    return (evento) => {
      evento.preventDefault();
      const nuevosErrores = validar(valores);
      setErrores(nuevosErrores);
      if (Object.keys(nuevosErrores).length === 0) {
        alSerValido(valores);
      }
    };
  }

  return { valores, setValores, errores, manejarCambio, enviar };
}