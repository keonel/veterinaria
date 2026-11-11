package cl.sanmarcos.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Producto y cantidad que Pedidos solicita descontar. El precio NO viene en la solicitud. */
public record VentaItemRequest(
    @NotNull Long productoId, @NotNull @Min(1) Integer cantidad) {}
