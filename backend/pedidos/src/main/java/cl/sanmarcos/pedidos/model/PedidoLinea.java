package cl.sanmarcos.pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Línea de un pedido. Es @Embeddable porque no existe por sí sola: forma parte del pedido.
 * Guarda el nombre y el PRECIO DE COMPRA: si el catálogo cambia después, el pedido no cambia.
 */
@Embeddable
public class PedidoLinea {
  @Column(name = "producto_id", nullable = false)
  private Long productoId;

  @Column(nullable = false, length = 20)
  private String codigo;

  @Column(nullable = false, length = 200)
  private String nombre;

  @Column(name = "precio_unitario", nullable = false)
  private int precioUnitario;

  @Column(nullable = false)
  private int cantidad;

  public PedidoLinea() {}

  public PedidoLinea(
      Long productoId, String codigo, String nombre, int precioUnitario, int cantidad) {
    this.productoId = productoId;
    this.codigo = codigo;
    this.nombre = nombre;
    this.precioUnitario = precioUnitario;
    this.cantidad = cantidad;
  }

  public Long getProductoId() { return productoId; }
  public String getCodigo() { return codigo; }
  public String getNombre() { return nombre; }
  public int getPrecioUnitario() { return precioUnitario; }
  public int getCantidad() { return cantidad; }
}
