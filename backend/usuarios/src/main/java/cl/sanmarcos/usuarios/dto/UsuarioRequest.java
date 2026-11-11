package cl.sanmarcos.usuarios.dto;

import cl.sanmarcos.usuarios.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos con los que el ADMIN crea o edita un usuario. Aquí sí se elige el rol.
 * La contraseña es obligatoria al crear y opcional al editar (vacía = no cambia).
 */
public record UsuarioRequest(
    @NotBlank(message = "El RUN es obligatorio") String run,
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,
    @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
    @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo no válido")
        String correo,
    @Size(max = 20) String telefono,
    @Size(max = 80) String region,
    @Size(max = 80) String comuna,
    @Size(max = 150) String direccion,
    String contrasena,
    @NotNull(message = "Selecciona un rol") Rol rol,
    Boolean activo) {}
