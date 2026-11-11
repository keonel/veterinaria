package cl.sanmarcos.citas.controller;

import cl.sanmarcos.citas.dto.EstadoMensajeRequest;
import cl.sanmarcos.citas.dto.MensajeRequest;
import cl.sanmarcos.citas.dto.MensajeResponse;
import cl.sanmarcos.citas.service.MensajesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/mensajes")
@Tag(name = "Mensajes", description = "Mensajes del formulario de contacto")
public class MensajesController {
  private final MensajesService service;

  public MensajesController(MensajesService service) {
    this.service = service;
  }

  @Operation(summary = "Envía un mensaje de contacto (público)")
  @SecurityRequirements
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MensajeResponse enviar(@Valid @RequestBody MensajeRequest form) {
    return service.enviar(form);
  }

  @Operation(summary = "Lista los mensajes recibidos (ADMIN)")
  @GetMapping
  public List<MensajeResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Detalle de un mensaje (ADMIN)")
  @GetMapping("/{id}")
  public MensajeResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Marca un mensaje como leído o respondido (ADMIN)")
  @PutMapping("/{id}/estado")
  public MensajeResponse cambiarEstado(
      @PathVariable Long id, @Valid @RequestBody EstadoMensajeRequest form) {
    return service.cambiarEstado(id, form.estado());
  }

  @Operation(summary = "Elimina un mensaje (ADMIN)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }
}
