package cl.sanmarcos.pedidos.model;

/**
 * PENDIENTE: registrado, aún sin confirmar el stock. CONFIRMADO: stock descontado, compra válida.
 * ENTREGADO: el cliente recibió sus productos. CANCELADO: no se pudo completar (sin stock).
 */
public enum EstadoPedido {
  PENDIENTE,
  CONFIRMADO,
  ENTREGADO,
  CANCELADO
}
