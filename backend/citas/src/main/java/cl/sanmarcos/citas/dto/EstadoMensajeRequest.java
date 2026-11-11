package cl.sanmarcos.citas.dto;

import cl.sanmarcos.citas.model.EstadoMensaje;
import jakarta.validation.constraints.NotNull;

public record EstadoMensajeRequest(
    @NotNull(message = "Indica el nuevo estado") EstadoMensaje estado) {}
