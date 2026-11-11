package cl.sanmarcos.usuarios.controller;

import cl.sanmarcos.usuarios.dto.UsuarioRequest;
import cl.sanmarcos.usuarios.dto.UsuarioResponse;
import cl.sanmarcos.usuarios.service.UsuariosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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

/** CRUD de usuarios. SecurityConfig restringe toda la ruta /api/v1/usuarios al rol ADMIN. */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de cuentas (solo ADMIN)")
public class UsuariosController {
  private final UsuariosService service;

  public UsuariosController(UsuariosService service) {
    this.service = service;
  }

  @Operation(summary = "Lista todos los usuarios")
  @GetMapping
  public List<UsuarioResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Obtiene un usuario por id")
  @GetMapping("/{id}")
  public UsuarioResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crea un usuario con cualquier rol")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest form) {
    return service.crear(form);
  }

  @Operation(summary = "Actualiza datos, rol, estado o contraseña de un usuario")
  @PutMapping("/{id}")
  public UsuarioResponse actualizar(
      @PathVariable Long id,
      @Valid @RequestBody UsuarioRequest form,
      @AuthenticationPrincipal Jwt jwt) {
    return service.actualizar(id, form, Long.valueOf(jwt.getSubject()));
  }

  @Operation(summary = "Elimina un usuario")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
    service.eliminar(id, Long.valueOf(jwt.getSubject()));
  }
}
