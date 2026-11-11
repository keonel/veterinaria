package cl.sanmarcos.catalogo.controller;

import cl.sanmarcos.catalogo.dto.ProductoRequest;
import cl.sanmarcos.catalogo.dto.ProductoResponse;
import cl.sanmarcos.catalogo.service.CatalogoService;
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

/**
 * Capa controller: recibe HTTP, valida el DTO con @Valid y llama al servicio.
 * No contiene reglas de negocio. La ruta lleva la versión de la API (/api/v1).
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de productos y servicios")
public class ProductosController {
  private final CatalogoService service;

  public ProductosController(CatalogoService service) {
    this.service = service;
  }

  @Operation(summary = "Lista productos y servicios (público)")
  @SecurityRequirements
  @GetMapping
  public List<ProductoResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Detalle de un producto (público)")
  @SecurityRequirements
  @GetMapping("/{id}")
  public ProductoResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crea un producto o servicio (ADMIN)")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductoResponse crear(@Valid @RequestBody ProductoRequest form) {
    // id null indica creación
    return service.guardar(null, form);
  }

  @Operation(summary = "Actualiza un producto o servicio (ADMIN)")
  @PutMapping("/{id}")
  public ProductoResponse actualizar(
      @PathVariable Long id, @Valid @RequestBody ProductoRequest form) {
    return service.guardar(id, form);
  }

  @Operation(summary = "Elimina un producto o servicio (ADMIN)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }
}
