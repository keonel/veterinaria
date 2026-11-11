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
import java.time.LocalDate;

/**
 * Cita veterinaria (tabla "citas"). usuarioId identifica a la cuenta del microservicio de
 * Usuarios que la solicitó; se guarda solo el identificador, sin relación JPA entre servicios.
 */
@Entity
@Table(name = "citas")
public class Cita {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Código visible para el cliente, por ejemplo CITA-001. */
  @Column(unique = true, length = 20)
  private String codigo;

  @Column(name = "usuario_id", nullable = false)
  private Long usuarioId;

  // Datos del tutor
  @Column(nullable = false, length = 150)
  private String cliente;

  @Column(nullable = false, length = 15)
  private String rut;

  @Column(nullable = false, length = 20)
  private String telefono;

  @Column(nullable = false, length = 120)
  private String correo;

  // Datos de la mascota
  @Column(nullable = false, length = 60)
  private String mascota;

  @Column(nullable = false, length = 30)
  private String especie;

  @Column(length = 60)
  private String raza;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  @Column(nullable = false, length = 10)
  private String sexo;

  @Column(length = 5)
  private String esterilizado;

  // Datos de la atención
  @Column(nullable = false, length = 80)
  private String servicio;

  @Column(length = 1000)
  private String descripcion;

  @Column(nullable = false, length = 80)
  private String veterinario;

  @Column(nullable = false)
  private LocalDate fecha;

  /** Hora en formato HH:mm. */
  @Column(nullable = false, length = 5)
  private String hora;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoCita estado = EstadoCita.PENDIENTE;

  @Column(name = "creada_en", nullable = false)
  private Instant creadaEn = Instant.now();

  public Long getId() { return id; }
  public String getCodigo() { return codigo; }
  public void setCodigo(String codigo) { this.codigo = codigo; }
  public Long getUsuarioId() { return usuarioId; }
  public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
  public String getCliente() { return cliente; }
  public void setCliente(String cliente) { this.cliente = cliente; }
  public String getRut() { return rut; }
  public void setRut(String rut) { this.rut = rut; }
  public String getTelefono() { return telefono; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }
  public String getMascota() { return mascota; }
  public void setMascota(String mascota) { this.mascota = mascota; }
  public String getEspecie() { return especie; }
  public void setEspecie(String especie) { this.especie = especie; }
  public String getRaza() { return raza; }
  public void setRaza(String raza) { this.raza = raza; }
  public LocalDate getFechaNacimiento() { return fechaNacimiento; }
  public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
  public String getSexo() { return sexo; }
  public void setSexo(String sexo) { this.sexo = sexo; }
  public String getEsterilizado() { return esterilizado; }
  public void setEsterilizado(String esterilizado) { this.esterilizado = esterilizado; }
  public String getServicio() { return servicio; }
  public void setServicio(String servicio) { this.servicio = servicio; }
  public String getDescripcion() { return descripcion; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public String getVeterinario() { return veterinario; }
  public void setVeterinario(String veterinario) { this.veterinario = veterinario; }
  public LocalDate getFecha() { return fecha; }
  public void setFecha(LocalDate fecha) { this.fecha = fecha; }
  public String getHora() { return hora; }
  public void setHora(String hora) { this.hora = hora; }
  public EstadoCita getEstado() { return estado; }
  public void setEstado(EstadoCita estado) { this.estado = estado; }
  public Instant getCreadaEn() { return creadaEn; }
}
