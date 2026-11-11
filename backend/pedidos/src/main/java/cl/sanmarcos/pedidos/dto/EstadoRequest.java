package cl.sanmarcos.pedidos.dto;

import cl.sanmarcos.pedidos.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull(message = "Indica el nuevo estado") EstadoPedido estado) {}
