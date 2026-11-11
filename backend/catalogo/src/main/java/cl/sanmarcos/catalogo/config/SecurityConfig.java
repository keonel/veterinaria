package cl.sanmarcos.catalogo.config;

import cl.sanmarcos.common.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Reglas de acceso del catálogo. */
@Configuration
public class SecurityConfig {
  /** Valida los JWT con la clave pública que publica el servicio de usuarios. */
  @Bean
  JwtDecoder decoder(@Value("${app.jwks}") String jwks, @Value("${app.issuer}") String issuer) {
    return SecuritySupport.decoder(jwks, issuer);
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    SecuritySupport.base(http);
    http.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    // protegido con X-Internal-Key en InternalController
                    "/internal/ventas/**")
                .permitAll()
                // Ver el catálogo es público: la tienda, el VENDEDOR y el CLIENTE solo leen
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**")
                .permitAll()
                // Crear, editar y eliminar: solo ADMIN
                .requestMatchers("/api/v1/productos/**")
                .hasRole("ADMIN")
                .anyRequest()
                .denyAll());
    return http.build();
  }
}
