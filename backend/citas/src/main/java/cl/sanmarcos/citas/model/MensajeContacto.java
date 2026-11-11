package cl.sanmarcos.citas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Mensaje enviado desde el formulario público de contacto (tabla "mensajes_contacto"). */
@Entity
@Table(name = "mensajes_contacto")
public class MensajeContacto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String nombre;

  @Column(nullable = false, length = 120)
  private String correo;

  @Column(length = 20)
  private String telefono;

  @Column(nullable = false, length = 150)
  private String asunto;

  @Column(nullable = false, length = 2000)
  private String mensaje;

  @Column(nullable = false)
  private Instant fecha = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoMensaje estado = EstadoMensaje.NUEVO;

  public Long getId() { return id; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }
  public String getTelefono() { return telefono; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public String getAsunto() { return asunto; }
  public void setAsunto(String asunto) { this.asunto = asunto; }
  public String getMensaje() { return mensaje; }
  public void setMensaje(String mensaje) { this.mensaje = mensaje; }
  public Instant getFecha() { return fecha; }
  public EstadoMensaje getEstado() { return estado; }
  public void setEstado(EstadoMensaje estado) { this.estado = estado; }
}
