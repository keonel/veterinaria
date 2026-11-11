package cl.sanmarcos.catalogo.controller;

import cl.sanmarcos.catalogo.dto.VentaItemRequest;
import cl.sanmarcos.catalogo.dto.VentaResponse;
import cl.sanmarcos.catalogo.service.CatalogoService;
import cl.sanmarcos.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint INTERNO entre microservicios: solo lo llama Pedidos.
 * No tiene ruta en el Gateway (el navegador no puede alcanzarlo) y exige una clave
 * compartida en la cabecera X-Internal-Key.
 */
@Hidden
@RestController
@RequestMapping("/internal/ventas")
public class InternalController {
  private final CatalogoService service;
  private final String claveInterna;

  public InternalController(
      CatalogoService service, @Value("${app.internal-key}") String claveInterna) {
    this.service = service;
    this.claveInterna = claveInterna;
  }

  @PostMapping("/{pedidoId}")
  public VentaResponse vender(
      @PathVariable Long pedidoId,
      @RequestHeader(value = "X-Internal-Key", required = false) String clave,
      @RequestBody List<@Valid VentaItemRequest> items) {
    if (clave == null
        || !MessageDigest.isEqual(
            clave.getBytes(StandardCharsets.UTF_8),
            claveInterna.getBytes(StandardCharsets.UTF_8))) {
      throw new ApiException(403, "Clave interna inválida.");
    }
    return service.vender(pedidoId, items);
  }
}
