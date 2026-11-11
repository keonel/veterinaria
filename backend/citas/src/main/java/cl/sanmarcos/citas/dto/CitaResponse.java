package cl.sanmarcos.citas.dto;

import cl.sanmarcos.citas.model.Cita;
import cl.sanmarcos.citas.model.EstadoCita;
import java.time.LocalDate;

public record CitaResponse(
    Long id,
    String codigo,
    Long usuarioId,
    String cliente,
    String rut,
    String telefono,
    String correo,
    String mascota,
    String especie,
    String raza,
    LocalDate fechaNacimiento,
    String sexo,
    String esterilizado,
    String servicio,
    String descripcion,
    String veterinario,
    LocalDate fecha,
    String hora,
    EstadoCita estado) {

  public static CitaResponse de(Cita c) {
    return new CitaResponse(
        c.getId(),
        c.getCodigo(),
        c.getUsuarioId(),
        c.getCliente(),
        c.getRut(),
        c.getTelefono(),
        c.getCorreo(),
        c.getMascota(),
        c.getEspecie(),
        c.getRaza(),
        c.getFechaNacimiento(),
        c.getSexo(),
        c.getEsterilizado(),
        c.getServicio(),
        c.getDescripcion(),
        c.getVeterinario(),
        c.getFecha(),
        c.getHora(),
        c.getEstado());
  }
}
