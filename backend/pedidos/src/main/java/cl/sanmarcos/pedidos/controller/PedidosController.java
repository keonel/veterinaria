package cl.sanmarcos.pedidos.controller;

import cl.sanmarcos.pedidos.dto.EstadoRequest;
import cl.sanmarcos.pedidos.dto.PedidoRequest;
import cl.sanmarcos.pedidos.dto.PedidoResponse;
import cl.sanmarcos.pedidos.service.PedidoStore;
import cl.sanmarcos.pedidos.service.PedidoWorkflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Compras de la tienda")
public class PedidosController {
  private final PedidoWorkflow workflow;
  private final PedidoStore store;

  public PedidosController(PedidoWorkflow workflow, PedidoStore store) {
    this.workflow = workflow;
    this.store = store;
  }

  @Operation(
      summary = "Confirma la compra del carrito (CLIENTE)",
      description =
          "El usuario se toma del JWT y los precios del catálogo. Repetir la solicitud con la"
              + " misma Idempotency-Key devuelve el mismo pedido sin volver a descontar stock.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PedidoResponse comprar(
      @AuthenticationPrincipal Jwt jwt,
      @RequestHeader("Idempotency-Key") String clave,
      @Valid @RequestBody PedidoRequest form) {
    return workflow.comprar(
        Long.valueOf(jwt.getSubject()),
        jwt.getClaimAsString("nombre"),
        jwt.getClaimAsString("correo"),
        clave,
        form);
  }

  @Operation(summary = "Historial de compras del cliente autenticado (CLIENTE)")
  @GetMapping("/mis-pedidos")
  public List<PedidoResponse> misPedidos(@AuthenticationPrincipal Jwt jwt) {
    return store.listarDe(Long.valueOf(jwt.getSubject()));
  }

  @Operation(summary = "Lista todos los pedidos (ADMIN y VENDEDOR)")
  @GetMapping
  public List<PedidoResponse> listar() {
    return store.listar();
  }

  @Operation(summary = "Detalle de un pedido (ADMIN, VENDEDOR o el CLIENTE dueño)")
  @GetMapping("/{id}")
  public PedidoResponse obtener(
      @PathVariable Long id, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
    boolean esCliente =
        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    return store.obtener(id, esCliente ? Long.valueOf(jwt.getSubject()) : null);
  }

  @Operation(summary = "Cambia el estado de un pedido (ADMIN)")
  @PutMapping("/{id}/estado")
  public PedidoResponse cambiarEstado(
      @PathVariable Long id, @Valid @RequestBody EstadoRequest form) {
    return store.cambiarEstado(id, form.estado());
  }

  @Operation(summary = "Elimina un pedido cancelado (ADMIN)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    store.eliminar(id);
  }
}
