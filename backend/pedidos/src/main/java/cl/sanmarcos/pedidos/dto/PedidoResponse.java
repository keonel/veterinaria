package cl.sanmarcos.pedidos.dto;

import cl.sanmarcos.pedidos.model.EstadoPedido;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
    Long id,
    Long usuarioId,
    String clienteNombre,
    String clienteCorreo,
    EstadoPedido estado,
    int total,
    Instant creadoEn,
    List<Linea> items) {

  public record Linea(
      Long productoId,
      String codigo,
      String nombre,
      int precioUnitario,
      int cantidad,
      int subtotal) {}
}
