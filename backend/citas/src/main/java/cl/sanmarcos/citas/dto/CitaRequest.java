package cl.sanmarcos.citas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Datos del formulario "Solicitar cita". El estado y el código los define el backend. */
public record CitaRequest(
    @NotBlank(message = "Ingresa el nombre del tutor") @Size(max = 150) String cliente,
    @NotBlank(message = "Ingresa el RUN del tutor") @Size(max = 15) String rut,
    @NotBlank(message = "Ingresa un teléfono") @Size(max = 20) String telefono,
    @NotBlank(message = "Ingresa un correo") @Email(message = "Correo no válido") String correo,
    @NotBlank(message = "Ingresa el nombre de la mascota") @Size(max = 60) String mascota,
    @NotBlank(message = "Selecciona la especie") @Size(max = 30) String especie,
    @Size(max = 60) String raza,
    LocalDate fechaNacimiento,
    @NotBlank(message = "Selecciona el sexo") @Size(max = 10) String sexo,
    @Size(max = 5) String esterilizado,
    @NotBlank(message = "Selecciona el motivo") @Size(max = 80) String servicio,
    @Size(max = 1000) String descripcion,
    @Size(max = 80) String veterinario,
    @NotNull(message = "Selecciona la fecha") LocalDate fecha,
    @NotBlank(message = "Selecciona la hora")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Hora inválida (HH:mm)")
        String hora) {}
