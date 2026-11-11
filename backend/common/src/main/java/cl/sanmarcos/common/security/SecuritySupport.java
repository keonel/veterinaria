package cl.sanmarcos.common.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Configuración de seguridad que comparten todos los microservicios:
 * API sin estado (stateless), validación del JWT y respuestas 401/403 en JSON.
 */
public final class SecuritySupport {
  /** Audiencia que deben traer los tokens emitidos por el servicio de usuarios. */
  public static final String AUDIENCIA = "sanmarcos-api";

  private SecuritySupport() {}

  /** Valida emisor, vencimiento y audiencia del token. */
  public static OAuth2TokenValidator<Jwt> validador(String issuer) {
    OAuth2TokenValidator<Jwt> audiencia =
        jwt ->
            jwt.getAudience() != null && jwt.getAudience().contains(AUDIENCIA)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Audiencia incorrecta", null));
    return new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(issuer), audiencia);
  }

  /**
   * Decodificador para catálogo, pedidos y citas: obtiene la clave PÚBLICA desde el
   * endpoint JWKS de usuarios. Estos servicios nunca conocen la clave privada.
   */
  public static JwtDecoder decoder(String jwksUrl, String issuer) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
    decoder.setJwtValidator(validador(issuer));
    return decoder;
  }

  /** Convierte el claim "roles": ["ADMIN"] en la autoridad ROLE_ADMIN de Spring Security. */
  public static JwtAuthenticationConverter converter() {
    JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
    roles.setAuthoritiesClaimName("roles");
    roles.setAuthorityPrefix("ROLE_");
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(roles);
    return converter;
  }

  /** Base común: sin CSRF (no hay sesión de servidor), sin estado y errores en JSON. */
  public static void base(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(
            oauth ->
                oauth
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(converter()))
                    .authenticationEntryPoint(
                        (req, res, e) ->
                            escribir(res, 401, "Debes iniciar sesión o renovar tu sesión.")))
        .exceptionHandling(
            e ->
                e.accessDeniedHandler(
                    (req, res, ex) -> escribir(res, 403, "No tienes permisos para esta acción.")));
  }

  private static void escribir(HttpServletResponse res, int status, String mensaje)
      throws IOException {
    res.setStatus(status);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    res.getWriter().write("{\"status\":" + status + ",\"mensaje\":\"" + mensaje + "\"}");
  }
}
