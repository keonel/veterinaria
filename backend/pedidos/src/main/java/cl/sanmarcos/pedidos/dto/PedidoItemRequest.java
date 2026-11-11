package cl.sanmarcos.pedidos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** El frontend envía solo id y cantidad. El precio lo decide el backend, nunca el navegador. */
public record PedidoItemRequest(
    @NotNull(message = "Falta el producto") Long productoId,
    @NotNull(message = "Falta la cantidad")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 99, message = "La cantidad máxima es 99")
        Integer cantidad) {}
