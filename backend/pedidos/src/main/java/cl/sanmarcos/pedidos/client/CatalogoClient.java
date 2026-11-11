package cl.sanmarcos.pedidos.client;

import cl.sanmarcos.common.exception.ApiException;
import cl.sanmarcos.pedidos.dto.InventarioResponse;
import cl.sanmarcos.pedidos.dto.PedidoItemRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP hacia el microservicio de Catálogo. Aquí ocurre el salto de red entre servicios:
 * Pedidos no lee la tabla productos, le PIDE a Catálogo que descuente el stock.
 */
@Component
public class CatalogoClient {
  private final RestClient client;
  private final String claveInterna;
  private final ObjectMapper json = new ObjectMapper();

  public CatalogoClient(
      @Value("${app.catalogo-url}") String catalogoUrl,
      @Value("${app.internal-key}") String claveInterna) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(10000);
    this.client = RestClient.builder().baseUrl(catalogoUrl).requestFactory(factory).build();
    this.claveInterna = claveInterna;
  }

  /** Descuenta stock en Catálogo y devuelve el comprobante con los precios vigentes. */
  public InventarioResponse vender(Long pedidoId, List<PedidoItemRequest> items) {
    try {
      return client
          .post()
          .uri("/internal/ventas/{id}", pedidoId)
          .header("X-Internal-Key", claveInterna)
          .body(items)
          .retrieve()
          .onStatus(
              HttpStatusCode::isError,
              (req, res) -> {
                int status = res.getStatusCode().value();
                String mensaje = "No se pudo confirmar el inventario.";
                try {
                  JsonNode body = json.readTree(res.getBody());
                  if (body.hasNonNull("mensaje")) mensaje = body.get("mensaje").asText();
                } catch (Exception ignorada) {
                  // se conserva el mensaje genérico
                }
                // 400/409 son errores de negocio (sin stock, producto inactivo): se propagan.
                // Cualquier otro error del catálogo se informa como 502 (falla entre servicios).
                throw new ApiException(status == 400 || status == 409 ? status : 502, mensaje);
              })
          .body(InventarioResponse.class);
    } catch (ResourceAccessException e) {
      throw new ApiException(
          503, "El catálogo no está disponible en este momento. Intenta nuevamente.");
    }
  }
}
