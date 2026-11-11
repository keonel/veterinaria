package cl.sanmarcos.catalogo.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprobante de inventario: registra que el stock de un pedido YA fue descontado.
 * pedidoId es único: si Pedidos reintenta la misma venta, se devuelve este comprobante
 * y el stock no se descuenta dos veces (idempotencia).
 *
 * <p>pedidoId es solo un identificador: no hay relación JPA hacia la base de Pedidos.
 */
@Entity
@Table(name = "ventas_inventario")
public class Venta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pedido_id", nullable = false, unique = true)
  private Long pedidoId;

  @Column(name = "creada_en", nullable = false)
  private Instant creadaEn = Instant.now();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "venta_lineas", joinColumns = @JoinColumn(name = "venta_id"))
  private List<VentaLinea> lineas = new ArrayList<>();

  public Venta() {}

  public Venta(Long pedidoId) {
    this.pedidoId = pedidoId;
  }

  public Long getId() { return id; }
  public Long getPedidoId() { return pedidoId; }
  public Instant getCreadaEn() { return creadaEn; }
  public List<VentaLinea> getLineas() { return lineas; }
}
