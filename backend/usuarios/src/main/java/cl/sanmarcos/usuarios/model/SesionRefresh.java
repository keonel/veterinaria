package cl.sanmarcos.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Sesión de renovación. Se guarda el HASH (SHA-256) del refresh token, no el token:
 * si alguien lee la base de datos no puede reutilizar las sesiones.
 */
@Entity
@Table(name = "sesiones_refresh")
public class SesionRefresh {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "usuario_id", nullable = false)
  private Long usuarioId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expira_en", nullable = false)
  private Instant expiraEn;

  @Column(nullable = false)
  private boolean revocada = false;

  public SesionRefresh() {}

  public SesionRefresh(Long usuarioId, String tokenHash, Instant expiraEn) {
    this.usuarioId = usuarioId;
    this.tokenHash = tokenHash;
    this.expiraEn = expiraEn;
  }

  public Long getId() { return id; }
  public Long getUsuarioId() { return usuarioId; }
  public String getTokenHash() { return tokenHash; }
  public Instant getExpiraEn() { return expiraEn; }
  public boolean isRevocada() { return revocada; }
  public void setRevocada(boolean revocada) { this.revocada = revocada; }
}
