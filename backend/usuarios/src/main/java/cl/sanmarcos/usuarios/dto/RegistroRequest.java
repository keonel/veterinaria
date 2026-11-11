package cl.sanmarcos.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del registro público. No existe el campo "rol": aunque alguien lo envíe en el JSON
 * se ignora, y el servicio siempre crea la cuenta como CLIENTE.
 */
public record RegistroRequest(
    @NotBlank(message = "El RUN es obligatorio") String run,
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,
    @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
    @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo no válido")
        @Pattern(
            regexp = "(?i)^[A-Za-z0-9._%+-]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
            message = "El correo debe ser @duoc.cl, @profesor.duoc.cl o @gmail.com")
        String correo,
    @NotBlank(message = "El teléfono es obligatorio") @Size(max = 20) String telefono,
    @NotBlank(message = "Selecciona una región") @Size(max = 80) String region,
    @NotBlank(message = "Selecciona una comuna") @Size(max = 80) String comuna,
    @NotBlank(message = "La dirección es obligatoria") @Size(max = 150) String direccion,
    @NotBlank(message = "La contraseña es obligatoria") String contrasena) {}
