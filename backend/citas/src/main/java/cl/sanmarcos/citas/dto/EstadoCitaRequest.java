package cl.sanmarcos.citas.dto;

import cl.sanmarcos.citas.model.EstadoCita;
import jakarta.validation.constraints.NotNull;

public record EstadoCitaRequest(@NotNull(message = "Indica el nuevo estado") EstadoCita estado) {}
