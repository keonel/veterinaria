package cl.sanmarcos.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Línea del comprobante: guarda el precio vigente en el momento de la venta. */
@Embeddable
public class VentaLinea {
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

  public VentaLinea() {}

  public VentaLinea(Long productoId, String codigo, String nombre, int precioUnitario, int cantidad) {
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
