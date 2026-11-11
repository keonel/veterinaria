package cl.sanmarcos.catalogo.dto;

import cl.sanmarcos.catalogo.model.Venta;
import java.util.List;

/** Comprobante que Catálogo devuelve a Pedidos, con los precios vigentes al vender. */
public record VentaResponse(Long pedidoId, List<Linea> lineas, int total) {

  public record Linea(
      Long productoId, String codigo, String nombre, int precioUnitario, int cantidad) {}

  public static VentaResponse de(Venta v) {
    List<Linea> lineas =
        v.getLineas().stream()
            .map(
                l ->
                    new Linea(
                        l.getProductoId(),
                        l.getCodigo(),
                        l.getNombre(),
                        l.getPrecioUnitario(),
                        l.getCantidad()))
            .toList();
    int total = lineas.stream().mapToInt(l -> l.precioUnitario() * l.cantidad()).sum();
    return new VentaResponse(v.getPedidoId(), lineas, total);
  }
}
