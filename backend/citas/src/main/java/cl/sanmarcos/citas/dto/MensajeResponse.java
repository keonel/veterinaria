package cl.sanmarcos.citas.dto;

import cl.sanmarcos.citas.model.EstadoMensaje;
import cl.sanmarcos.citas.model.MensajeContacto;
import java.time.Instant;

public record MensajeResponse(
    Long id,
    String nombre,
    String correo,
    String telefono,
    String asunto,
    String mensaje,
    Instant fecha,
    EstadoMensaje estado) {

  public static MensajeResponse de(MensajeContacto m) {
    return new MensajeResponse(
        m.getId(),
        m.getNombre(),
        m.getCorreo(),
        m.getTelefono(),
        m.getAsunto(),
        m.getMensaje(),
        m.getFecha(),
        m.getEstado());
  }
}
