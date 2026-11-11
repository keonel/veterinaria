export const CORREO_PERMITIDO =
  /^[A-Za-z0-9._%+-]+@(duoc\.cl|profesor\.duoc\.cl|gmail\.com)$/i;

export function validarCorreo(correo) {
  return CORREO_PERMITIDO.test(correo);
}

export function validarRun(run) {
  if (!run) return false;

  const limpio = run.replace(/[^0-9kK]/g, "").toUpperCase();

  if (limpio.length < 8 || limpio.length > 9) return false;

  const cuerpo = limpio.slice(0, -1);
  const dv = limpio.slice(-1);

  let suma = 0;
  let multiplo = 2;

  for (let i = cuerpo.length - 1; i >= 0; i--) {
    suma += multiplo * Number(cuerpo.charAt(i));
    multiplo = multiplo < 7 ? multiplo + 1 : 2;
  }

  const esperado = 11 - (suma % 11);

  const dvEsperado =
    esperado === 11 ? "0" : esperado === 10 ? "K" : String(esperado);

  return dv === dvEsperado;
}

export function validarContrasena(clave) {
  return (
    typeof clave === "string" &&
    clave.length >= 5 &&
    clave.length <= 10 &&
    /[A-Z]/.test(clave) &&
    /[0-9]/.test(clave)
  );
}

export function validarTelefono(tel) {
  if (!tel) return false;

  const limpio = tel.replace(/[^0-9]/g, "");

  return limpio.length >= 8 && limpio.length <= 11;
}
