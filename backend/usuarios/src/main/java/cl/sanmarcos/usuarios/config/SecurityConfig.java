package cl.sanmarcos.usuarios.config;

import cl.sanmarcos.common.security.SecuritySupport;
import cl.sanmarcos.usuarios.security.JwtKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Reglas de acceso del microservicio de usuarios. */
@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwords() {
    return new BCryptPasswordEncoder();
  }

  /** Usuarios valida los tokens con su propia clave pública (no necesita llamar al JWKS). */
  @Bean
  JwtDecoder decoder(JwtKeys keys, @Value("${app.issuer}") String issuer) throws Exception {
    return keys.decoder(issuer);
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    SecuritySupport.base(http);
    http.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/registro",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/logout",
                    "/api/v1/auth/jwks",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health")
                .permitAll()
                // Solo el administrador gestiona las cuentas
                .requestMatchers("/api/v1/usuarios/**")
                .hasRole("ADMIN")
                // /auth/me: cualquier usuario con sesión
                .requestMatchers("/api/v1/auth/me")
                .authenticated()
                .anyRequest()
                .denyAll());
    return http.build();
  }
}
