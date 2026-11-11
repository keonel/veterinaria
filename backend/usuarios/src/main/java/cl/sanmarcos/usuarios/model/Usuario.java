package cl.sanmarcos.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Cuenta de usuario. Cada objeto es una fila de la tabla "usuarios". */
@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** RUN normalizado, por ejemplo 12345678-9. */
  @Column(nullable = false, unique = true, length = 12)
  private String run;

  @Column(nullable = false, length = 60)
  private String nombre;

  @Column(nullable = false, length = 80)
  private String apellidos;

  @Column(nullable = false, unique = true, length = 120)
  private String correo;

  @Column(length = 20)
  private String telefono;

  @Column(length = 80)
  private String region;

  @Column(length = 80)
  private String comuna;

  @Column(length = 150)
  private String direccion;

  /** Hash BCrypt. La contraseña en texto plano nunca se guarda ni se devuelve. */
  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Rol rol = Rol.CLIENTE;

  @Column(nullable = false)
  private boolean activo = true;

  @Column(name = "creado_en", nullable = false)
  private Instant creadoEn = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRun() { return run; }
  public void setRun(String run) { this.run = run; }
  public String getNombre() { return nombre; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public String getApellidos() { return apellidos; }
  public void setApellidos(String apellidos) { this.apellidos = apellidos; }
  public String getCorreo() { return correo; }
  public void setCorreo(String correo) { this.correo = correo; }
  public String getTelefono() { return telefono; }
  public void setTelefono(String telefono) { this.telefono = telefono; }
  public String getRegion() { return region; }
  public void setRegion(String region) { this.region = region; }
  public String getComuna() { return comuna; }
  public void setComuna(String comuna) { this.comuna = comuna; }
  public String getDireccion() { return direccion; }
  public void setDireccion(String direccion) { this.direccion = direccion; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public Rol getRol() { return rol; }
  public void setRol(Rol rol) { this.rol = rol; }
  public boolean isActivo() { return activo; }
  public void setActivo(boolean activo) { this.activo = activo; }
  public Instant getCreadoEn() { return creadoEn; }
  public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
