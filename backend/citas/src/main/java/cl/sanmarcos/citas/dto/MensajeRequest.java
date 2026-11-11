package cl.sanmarcos.citas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensajeRequest(
    @NotBlank(message = "Ingresa tu nombre") @Size(max = 150) String nombre,
    @NotBlank(message = "Ingresa tu correo") @Email(message = "Correo no válido") String correo,
    @Size(max = 20) String telefono,
    @NotBlank(message = "Ingresa un asunto") @Size(max = 150) String asunto,
    @NotBlank(message = "Escribe tu mensaje") @Size(max = 2000) String mensaje) {}
