package cl.sanmarcos.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos que un usuario puede cambiar de su propio perfil (no puede cambiar RUN ni rol). */
public record PerfilRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,
    @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
    @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo no válido")
        String correo,
    @Size(max = 20) String telefono,
    @Size(max = 80) String region,
    @Size(max = 80) String comuna,
    @Size(max = 150) String direccion) {}
