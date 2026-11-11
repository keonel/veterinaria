// El backend guarda el estado como PENDIENTE, CONFIRMADA o RECHAZADA.
// normalizarEstado lo convierte a minúsculas para usarlo en clases CSS y filtros.
export function normalizarEstado(estado) {
  const texto = String(estado || '').toLowerCase();
  if (texto.includes('confirmad')) return 'confirmada';
  if (texto.includes('rechazad')) return 'rechazada';
  return 'pendiente';
}

const TEXTO_ESTADO = {
  pendiente: 'Pendiente de Confirmación',
  confirmada: 'Confirmada',
  rechazada: 'Rechazada',
};

// Texto que se muestra al usuario para un estado del backend
export function textoEstadoCita(estado) {
  return TEXTO_ESTADO[normalizarEstado(estado)];
}

// "2026-11-10T14:30:00Z" -> "10-11-2026, 11:30" (hora local)
export function formatearFechaHora(instante) {
  if (!instante) return '-';
  const fecha = new Date(instante);
  return Number.isNaN(fecha.getTime()) ? '-' : fecha.toLocaleString('es-CL');
}
