import { useEffect, useState } from 'react';

export function leerJSON(clave, valorPorDefecto) {
  try {
    const crudo = localStorage.getItem(clave);
    return crudo ? JSON.parse(crudo) : valorPorDefecto;
  } catch {
    return valorPorDefecto;
  }
}

export default function useLocalStorage(clave, valorInicial) {
  const [valor, setValor] = useState(() => leerJSON(clave, valorInicial));

  useEffect(() => {
    localStorage.setItem(clave, JSON.stringify(valor));
  }, [clave, valor]);

  return [valor, setValor];
}