package cl.sanmarcos.usuarios.controller;

import cl.sanmarcos.usuarios.dto.AuthResultado;
import cl.sanmarcos.usuarios.dto.LoginRequest;
import cl.sanmarcos.usuarios.dto.PerfilRequest;
import cl.sanmarcos.usuarios.dto.RegistroRequest;
import cl.sanmarcos.usuarios.dto.SesionResponse;
import cl.sanmarcos.usuarios.dto.UsuarioResponse;
import cl.sanmarcos.usuarios.security.JwtKeys;
import cl.sanmarcos.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Recibe las solicitudes HTTP de autenticación y delega la lógica en AuthService. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión, renovación y perfil")
public class AuthController {
  public static final String COOKIE_REFRESH = "sm_refresh";

  private final AuthService service;
  private final JwtKeys keys;
  private final boolean cookieSegura;

  public AuthController(
      AuthService service, JwtKeys keys, @Value("${app.cookie-secure}") boolean cookieSegura) {
    this.service = service;
    this.keys = keys;
    this.cookieSegura = cookieSegura;
  }

  @Operation(summary = "Registra un cliente nuevo (siempre con rol CLIENTE)")
  @SecurityRequirements
  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse registro(@Valid @RequestBody RegistroRequest form) {
    return service.registrar(form);
  }

  @Operation(summary = "Inicia sesión: devuelve el access token y deja el refresh en una cookie")
  @SecurityRequirements
  @PostMapping("/login")
  public SesionResponse login(@Valid @RequestBody LoginRequest form, HttpServletResponse res) {
    AuthResultado r = service.login(form.correo(), form.contrasena());
    cookie(res, r.refreshToken(), AuthService.DURACION_REFRESH);
    return r.sesion();
  }

  @Operation(summary = "Renueva la sesión usando la cookie de refresh (rota el token)")
  @SecurityRequirements
  @PostMapping("/refresh")
  public SesionResponse refresh(
      @CookieValue(value = COOKIE_REFRESH, required = false) String token,
      HttpServletResponse res) {
    AuthResultado r = service.refresh(token);
    cookie(res, r.refreshToken(), AuthService.DURACION_REFRESH);
    return r.sesion();
  }

  @Operation(summary = "Cierra la sesión: revoca el refresh token y borra la cookie")
  @SecurityRequirements
  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @CookieValue(value = COOKIE_REFRESH, required = false) String token,
      HttpServletResponse res) {
    service.logout(token);
    cookie(res, "", Duration.ZERO);
  }

  @Operation(summary = "Datos del usuario autenticado")
  @GetMapping("/me")
  public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
    return UsuarioResponse.de(service.usuario(Long.valueOf(jwt.getSubject())));
  }

  @Operation(summary = "El usuario autenticado actualiza su propio perfil")
  @PutMapping("/me")
  public UsuarioResponse actualizarPerfil(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest form) {
    return service.actualizarPerfil(Long.valueOf(jwt.getSubject()), form);
  }

  @Operation(summary = "Clave pública (JWKS) con la que los demás servicios validan los JWT")
  @SecurityRequirements
  @GetMapping("/jwks")
  public Map<String, Object> jwks() {
    return keys.publicJwks();
  }

  /**
   * HttpOnly: JavaScript no puede leerla (protege contra robo por XSS). SameSite=Strict: el
   * navegador no la envía desde otros sitios (protege contra CSRF). Path: solo viaja a /auth.
   */
  private void cookie(HttpServletResponse res, String valor, Duration duracion) {
    ResponseCookie cookie =
        ResponseCookie.from(COOKIE_REFRESH, valor)
            .httpOnly(true)
            .secure(cookieSegura)
            .sameSite("Strict")
            .path("/api/v1/auth")
            .maxAge(duracion)
            .build();
    res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
