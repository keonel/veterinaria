package cl.sanmarcos.usuarios.service;

import cl.sanmarcos.common.exception.ApiException;
import cl.sanmarcos.common.security.SecuritySupport;
import cl.sanmarcos.usuarios.dto.AuthResultado;
import cl.sanmarcos.usuarios.dto.PerfilRequest;
import cl.sanmarcos.usuarios.dto.RegistroRequest;
import cl.sanmarcos.usuarios.dto.SesionResponse;
import cl.sanmarcos.usuarios.dto.UsuarioResponse;
import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.SesionRefresh;
import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.SesionesRepository;
import cl.sanmarcos.usuarios.repository.UsuariosRepository;
import cl.sanmarcos.usuarios.security.JwtKeys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de autenticación.
 *
 * <p>Se usan dos tokens: un ACCESS TOKEN (JWT de corta duración, 15 min) que se envía en la
 * cabecera Authorization, y un REFRESH TOKEN (aleatorio, 7 días) que vive en una cookie HttpOnly
 * y solo sirve para pedir un access token nuevo. Cada vez que se usa, el refresh se ROTA: el
 * anterior queda revocado y se entrega uno distinto.
 */
@Service
public class AuthService {
  public static final Duration DURACION_REFRESH = Duration.ofDays(7);

  private final UsuariosRepository usuarios;
  private final SesionesRepository sesiones;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final String kid;
  private final String issuer;
  private final long segundosAccess;
  private final SecureRandom random = new SecureRandom();

  public AuthService(
      UsuariosRepository usuarios,
      SesionesRepository sesiones,
      PasswordEncoder passwords,
      JwtKeys keys,
      @Value("${app.issuer}") String issuer,
      @Value("${app.access-seconds}") long segundosAccess) {
    this.usuarios = usuarios;
    this.sesiones = sesiones;
    this.passwords = passwords;
    this.encoder = keys.encoder();
    this.kid = keys.kid();
    this.issuer = issuer;
    this.segundosAccess = segundosAccess;
  }

  /** Registro público: siempre crea un CLIENTE y guarda la contraseña con BCrypt. */
  @Transactional
  public UsuarioResponse registrar(RegistroRequest form) {
    String run = ReglasUsuario.normalizarRun(form.run());
    String correo = ReglasUsuario.normalizarCorreo(form.correo());
    ReglasUsuario.validarContrasena(form.contrasena());
    if (usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ApiException(409, "Ya existe un usuario registrado con ese correo.");
    }
    if (usuarios.existsByRun(run)) {
      throw new ApiException(409, "Ya existe un usuario registrado con ese RUN.");
    }
    Usuario u = new Usuario();
    u.setRun(run);
    u.setNombre(form.nombre().trim());
    u.setApellidos(form.apellidos().trim());
    u.setCorreo(correo);
    u.setTelefono(form.telefono().trim());
    u.setRegion(form.region());
    u.setComuna(form.comuna());
    u.setDireccion(form.direccion().trim());
    u.setPasswordHash(passwords.encode(form.contrasena()));
    u.setRol(Rol.CLIENTE);
    return UsuarioResponse.de(usuarios.save(u));
  }

  @Transactional
  public AuthResultado login(String correo, String contrasena) {
    Usuario u =
        usuarios
            .findByCorreoIgnoreCase(ReglasUsuario.normalizarCorreo(correo))
            .orElse(null);
    // Mismo mensaje si no existe el correo o si la clave es incorrecta:
    // así no se revela qué correos están registrados.
    if (u == null || !passwords.matches(contrasena, u.getPasswordHash())) {
      throw new ApiException(401, "Correo o contraseña incorrectos.");
    }
    if (!u.isActivo()) {
      throw new ApiException(403, "Tu cuenta se encuentra desactivada. Contacta al administrador.");
    }
    sesiones.borrarVencidas(Instant.now());
    return abrirSesion(u);
  }

  /** Cambia un refresh token válido por un access token nuevo y un refresh token nuevo. */
  @Transactional
  public AuthResultado refresh(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new ApiException(401, "No hay una sesión activa.");
    }
    SesionRefresh sesion = sesiones.findByTokenHash(hash(refreshToken)).orElse(null);
    if (sesion == null || sesion.isRevocada() || sesion.getExpiraEn().isBefore(Instant.now())) {
      throw new ApiException(401, "La sesión expiró. Inicia sesión nuevamente.");
    }
    Usuario u = usuarios.findById(sesion.getUsuarioId()).orElse(null);
    if (u == null || !u.isActivo()) {
      throw new ApiException(401, "La sesión expiró. Inicia sesión nuevamente.");
    }
    sesion.setRevocada(true); // rotación: el token usado no vuelve a servir
    return abrirSesion(u);
  }

  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) return;
    sesiones.findByTokenHash(hash(refreshToken)).ifPresent(s -> s.setRevocada(true));
  }

  @Transactional(readOnly = true)
  public Usuario usuario(Long id) {
    return usuarios
        .findById(id)
        .filter(Usuario::isActivo)
        .orElseThrow(() -> new ApiException(401, "La cuenta ya no está disponible."));
  }

  /** El usuario autenticado edita sus propios datos. */
  @Transactional
  public UsuarioResponse actualizarPerfil(Long id, PerfilRequest form) {
    Usuario u = usuario(id);
    String correo = ReglasUsuario.normalizarCorreo(form.correo());
    if (!correo.equalsIgnoreCase(u.getCorreo()) && usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ApiException(409, "Ese correo ya está registrado por otro usuario.");
    }
    u.setNombre(form.nombre().trim());
    u.setApellidos(form.apellidos().trim());
    u.setCorreo(correo);
    u.setTelefono(ReglasUsuario.limpiar(form.telefono()));
    u.setRegion(form.region());
    u.setComuna(form.comuna());
    u.setDireccion(ReglasUsuario.limpiar(form.direccion()));
    return UsuarioResponse.de(u);
  }

  private AuthResultado abrirSesion(Usuario u) {
    String refresh = nuevoRefreshToken();
    sesiones.save(
        new SesionRefresh(u.getId(), hash(refresh), Instant.now().plus(DURACION_REFRESH)));
    SesionResponse sesion =
        new SesionResponse(accessToken(u), "Bearer", segundosAccess, UsuarioResponse.de(u));
    return new AuthResultado(sesion, refresh);
  }

  /** Construye y firma el JWT. El rol viaja en el claim "roles". */
  private String accessToken(Usuario u) {
    Instant ahora = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .audience(List.of(SecuritySupport.AUDIENCIA))
            .subject(String.valueOf(u.getId()))
            .issuedAt(ahora)
            .expiresAt(ahora.plusSeconds(segundosAccess))
            .claim("roles", List.of(u.getRol().name()))
            .claim("nombre", u.getNombre() + " " + u.getApellidos())
            .claim("correo", u.getCorreo())
            .build();
    JwsHeader cabecera = JwsHeader.with(SignatureAlgorithm.RS256).keyId(kid).build();
    return encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
  }

  private String nuevoRefreshToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hash(String token) {
    try {
      MessageDigest sha = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(sha.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
