package cl.sanmarcos.pedidos.dto;

import java.util.List;

/** Comprobante que devuelve Catálogo al descontar el stock (copia local del contrato). */
public record InventarioResponse(Long pedidoId, List<Linea> lineas, int total) {
  public record Linea(
      Long productoId, String codigo, String nombre, int precioUnitario, int cantidad) {}
}
