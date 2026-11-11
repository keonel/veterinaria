package cl.sanmarcos.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Ingresa tu correo") String correo,
    @NotBlank(message = "Ingresa tu contraseña") String contrasena) {}
