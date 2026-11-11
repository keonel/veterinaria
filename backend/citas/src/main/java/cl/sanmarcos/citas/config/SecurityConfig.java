package cl.sanmarcos.citas.config;

import cl.sanmarcos.common.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Reglas de acceso de citas y mensajes. El VENDEDOR no tiene acceso a nada de este servicio. */
@Configuration
public class SecurityConfig {
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
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health")
                .permitAll()
                // --- Citas ---
                .requestMatchers(HttpMethod.POST, "/api/v1/citas")
                .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/v1/citas/mis-citas")
                .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/v1/citas")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/citas/*")
                .hasAnyRole("ADMIN", "CLIENTE")
                .requestMatchers("/api/v1/citas/**")
                .hasRole("ADMIN")
                // --- Mensajes de contacto ---
                // Cualquier visitante puede escribir; solo el administrador los lee
                .requestMatchers(HttpMethod.POST, "/api/v1/mensajes")
                .permitAll()
                .requestMatchers("/api/v1/mensajes/**")
                .hasRole("ADMIN")
                .anyRequest()
                .denyAll());
    return http.build();
  }
}
