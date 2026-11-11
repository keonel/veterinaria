package cl.sanmarcos.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Producto o servicio de la veterinaria. Cada objeto es una fila de la tabla "productos". */
@Entity
@Table(name = "productos")
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Código de negocio visible para el administrador, por ejemplo SM001. */
  @Column(nullable = false, unique = true, length = 20)
  private String codigo;

  /** "producto" (tiene stock y se vende en la tienda) o "servicio" (sin stock). */
  @Column(nullable = false, length = 10)
  private String tipo = "producto";

  @Column(nullable = false, length = 200)
  private String nombre;

  @Column(nullable = false, length = 80)
  private String categoria;

  @Column(length = 300)
  private String imagen;

  /** Precio en pesos chilenos (sin decimales). */
  @Column(nullable = false)
  private int precio;

  @Column(name = "stock_actual", nullable = false)
  private int stockActual;

  @Column(name = "stock_critico", nullable = false)
  private int stockCritico;

  @Column(length = 2000)
  private String descripcion;

  @Column(nullable = false)
  private boolean activo = true;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getCodigo() { return codigo; }
  public void setCodigo(String codigo) { this.codigo = codigo; }
  public String getTipo() { return tipo; }
  public void setTipo(String tipo) { this.tipo = tipo; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getCategoria() { return categoria; }
  public void setCategoria(String categoria) { this.categoria = categoria; }
  public String getImagen() { return imagen; }
  public void setImagen(String imagen) { this.imagen = imagen; }
  public int getPrecio() { return precio; }
  public void setPrecio(int precio) { this.precio = precio; }
  public int getStockActual() { return stockActual; }
  public void setStockActual(int stockActual) { this.stockActual = stockActual; }
  public int getStockCritico() { return stockCritico; }
  public void setStockCritico(int stockCritico) { this.stockCritico = stockCritico; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
}
