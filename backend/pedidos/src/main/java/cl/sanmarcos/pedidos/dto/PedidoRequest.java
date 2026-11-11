package cl.sanmarcos.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PedidoRequest(
    @NotEmpty(message = "El carrito está vacío") List<@Valid PedidoItemRequest> items) {}
