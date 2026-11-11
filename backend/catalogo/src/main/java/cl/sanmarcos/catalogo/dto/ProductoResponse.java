package cl.sanmarcos.catalogo.dto;

import cl.sanmarcos.catalogo.model.Producto;

/** Datos del producto que recibe React. */
public record ProductoResponse(
    Long id,
    String codigo,
    String tipo,
    String nombre,
    String categoria,
    String imagen,
    int precio,
    int stockActual,
    int stockCritico,
    String descripcion,
    boolean activo) {

  public static ProductoResponse de(Producto p) {
    return new ProductoResponse(
        p.getId(),
        p.getCodigo(),
        p.getTipo(),
        p.getNombre(),
        p.getCategoria(),
        p.getImagen(),
        p.getPrecio(),
        p.getStockActual(),
        p.getStockCritico(),
        p.getDescripcion(),
        p.isActivo());
  }
}
