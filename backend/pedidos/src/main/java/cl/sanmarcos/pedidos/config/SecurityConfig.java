package cl.sanmarcos.pedidos.config;

import cl.sanmarcos.common.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Reglas de acceso de pedidos. El orden importa: gana la primera regla que coincide. */
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
                // Comprar y ver "mis pedidos": solo el cliente
                .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                .hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/mis-pedidos")
                .hasRole("CLIENTE")
                // Lista completa: administrador y vendedor (el vendedor solo lee)
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos")
                .hasAnyRole("ADMIN", "VENDEDOR")
                // Detalle: el servicio comprueba además que el cliente sea el dueño
                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/*")
                .hasAnyRole("ADMIN", "VENDEDOR", "CLIENTE")
                // Cambiar estado y eliminar: solo administrador
                .requestMatchers("/api/v1/pedidos/**")
                .hasRole("ADMIN")
                .anyRequest()
                .denyAll());
    return http.build();
  }
}
