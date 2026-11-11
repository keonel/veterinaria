package cl.sanmarcos.pedidos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Compra de un cliente (tabla "pedidos").
 *
 * <p>usuarioId referencia a una cuenta del microservicio de Usuarios, pero se guarda como un
 * simple identificador: no existe @ManyToOne ni join hacia la base de otro servicio. Por eso
 * también se copian el nombre y el correo del cliente en el momento de la compra.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id", nullable = false)
  private Long usuarioId;

  @Column(name = "cliente_nombre", length = 150)
  private String clienteNombre;

  @Column(name = "cliente_correo", length = 120)
  private String clienteCorreo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoPedido estado = EstadoPedido.PENDIENTE;

  @Column(nullable = false)
  private int total;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  /** Clave que envía el frontend para que un reintento no genere una compra duplicada. */
  @Column(name = "idempotency_key", nullable = false, unique = true, length = 80)
  private String idempotencyKey;

  /** Las líneas se guardan en la tabla pedido_items, unidas por pedido_id. */
  @ElementCollection
  @CollectionTable(name = "pedido_items", joinColumns = @JoinColumn(name = "pedido_id"))
  private List<PedidoLinea> items = new ArrayList<>();

  public Long getId() { return id; }
  public Long getUsuarioId() { return usuarioId; }
  public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
  public String getClienteNombre() { return clienteNombre; }
  public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
  public String getClienteCorreo() { return clienteCorreo; }
  public void setClienteCorreo(String clienteCorreo) { this.clienteCorreo = clienteCorreo; }
  public EstadoPedido getEstado() { return estado; }
  public void setEstado(EstadoPedido estado) { this.estado = estado; }
  public int getTotal() { return total; }
  public void setTotal(int total) { this.total = total; }
  public Instant getCreadoEn() { return creadoEn; }
  public String getIdempotencyKey() { return idempotencyKey; }
  public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
  public List<PedidoLinea> getItems() { return items; }
}
