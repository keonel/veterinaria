package cl.sanmarcos.gateway.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Enrutamiento y CORS.
 *
 * <p>React solo conoce http://localhost:8080. Según el camino de la solicitud, el Gateway la
 * reenvía al microservicio correspondiente conservando la ruta. El Gateway NO valida permisos:
 * eso lo hace cada microservicio. Las rutas /internal/** no están publicadas aquí.
 */
@Configuration
public class RoutesConfig {
  @Bean
  RouteLocator routes(
      RouteLocatorBuilder builder,
      @Value("${app.usuarios-url}") String usuarios,
      @Value("${app.catalogo-url}") String catalogo,
      @Value("${app.pedidos-url}") String pedidos,
      @Value("${app.citas-url}") String citas) {
    return builder
        .routes()
        .route("usuarios", r -> r.path("/api/v1/auth/**", "/api/v1/usuarios/**").uri(usuarios))
        .route("catalogo", r -> r.path("/api/v1/productos/**").uri(catalogo))
        .route("pedidos", r -> r.path("/api/v1/pedidos/**").uri(pedidos))
        .route("citas", r -> r.path("/api/v1/citas/**", "/api/v1/mensajes/**").uri(citas))
        // Documentación: /openapi/<servicio> se traduce al /v3/api-docs de cada servicio
        .route(
            "docs-usuarios",
            r -> r.path("/openapi/usuarios").filters(f -> f.setPath("/v3/api-docs")).uri(usuarios))
        .route(
            "docs-catalogo",
            r -> r.path("/openapi/catalogo").filters(f -> f.setPath("/v3/api-docs")).uri(catalogo))
        .route(
            "docs-pedidos",
            r -> r.path("/openapi/pedidos").filters(f -> f.setPath("/v3/api-docs")).uri(pedidos))
        .route(
            "docs-citas",
            r -> r.path("/openapi/citas").filters(f -> f.setPath("/v3/api-docs")).uri(citas))
        .build();
  }

  /**
   * CORS: el frontend (localhost:5173) y el Gateway (localhost:8080) son orígenes distintos
   * porque cambia el puerto. Aquí se le indica al navegador qué orígenes, métodos y cabeceras
   * se aceptan. Con credenciales (cookie de refresh) no se puede usar el comodín "*".
   */
  @Bean
  CorsWebFilter cors(@Value("${app.allowed-origins}") String origenes) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(
        List.of("Content-Type", "Authorization", "Idempotency-Key", "X-Requested-With"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsWebFilter(source);
  }
}
