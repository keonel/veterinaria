package cl.sanmarcos.pedidos.mapper;

import cl.sanmarcos.pedidos.dto.PedidoResponse;
import cl.sanmarcos.pedidos.model.Pedido;
import java.util.List;

/** Convierte la entidad Pedido en el DTO de salida. */
public final class PedidoMapper {
  private PedidoMapper() {}

  public static PedidoResponse aRespuesta(Pedido p) {
    List<PedidoResponse.Linea> lineas =
        p.getItems().stream()
            .map(
                l ->
                    new PedidoResponse.Linea(
                        l.getProductoId(),
                        l.getCodigo(),
                        l.getNombre(),
                        l.getPrecioUnitario(),
                        l.getCantidad(),
                        l.getPrecioUnitario() * l.getCantidad()))
            .toList();
    return new PedidoResponse(
        p.getId(),
        p.getUsuarioId(),
        p.getClienteNombre(),
        p.getClienteCorreo(),
        p.getEstado(),
        p.getTotal(),
        p.getCreadoEn(),
        lineas);
  }
}
