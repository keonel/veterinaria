package cl.sanmarcos.citas.controller;

import cl.sanmarcos.citas.dto.CitaRequest;
import cl.sanmarcos.citas.dto.CitaResponse;
import cl.sanmarcos.citas.dto.EstadoCitaRequest;
import cl.sanmarcos.citas.service.CitasService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
@Tag(name = "Citas", description = "Citas veterinarias")
public class CitasController {
  private final CitasService service;

  public CitasController(CitasService service) {
    this.service = service;
  }

  @Operation(summary = "Solicita una cita (CLIENTE). Nace en estado PENDIENTE")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CitaResponse solicitar(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CitaRequest form) {
    // El dueño de la cita sale del token, no del formulario
    return service.solicitar(Long.valueOf(jwt.getSubject()), form);
  }

  @Operation(summary = "Citas del cliente autenticado (CLIENTE)")
  @GetMapping("/mis-citas")
  public List<CitaResponse> misCitas(@AuthenticationPrincipal Jwt jwt) {
    return service.listarDe(Long.valueOf(jwt.getSubject()));
  }

  @Operation(summary = "Lista todas las citas (ADMIN)")
  @GetMapping
  public List<CitaResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Detalle de una cita (ADMIN o el CLIENTE dueño)")
  @GetMapping("/{id}")
  public CitaResponse obtener(
      @PathVariable Long id, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
    boolean esCliente =
        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    return service.obtener(id, esCliente ? Long.valueOf(jwt.getSubject()) : null);
  }

  @Operation(summary = "Confirma o rechaza una cita (ADMIN)")
  @PutMapping("/{id}/estado")
  public CitaResponse cambiarEstado(
      @PathVariable Long id, @Valid @RequestBody EstadoCitaRequest form) {
    return service.cambiarEstado(id, form.estado());
  }

  @Operation(summary = "Elimina una cita (ADMIN)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }
}
