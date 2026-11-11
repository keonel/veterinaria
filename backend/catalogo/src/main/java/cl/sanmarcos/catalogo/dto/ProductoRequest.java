package cl.sanmarcos.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de entrada para crear o editar un producto. @Valid revisa estas restricciones. */
public record ProductoRequest(
    @NotBlank(message = "El código es obligatorio") @Size(max = 20) String codigo,
    @NotBlank(message = "Selecciona un tipo")
        @Pattern(regexp = "producto|servicio", message = "El tipo debe ser producto o servicio")
        String tipo,
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 200) String nombre,
    @NotBlank(message = "La categoría es obligatoria") @Size(max = 80) String categoria,
    @Size(max = 300) String imagen,
    @NotNull(message = "Ingresa un precio válido")
        @Min(value = 0, message = "El precio no puede ser negativo")
        Integer precio,
    @Min(value = 0, message = "Ingresa un stock entero de 0 o más") Integer stockActual,
    @Min(value = 0, message = "Ingresa un stock crítico entero de 0 o más") Integer stockCritico,
    @Size(max = 2000) String descripcion,
    Boolean activo) {}
