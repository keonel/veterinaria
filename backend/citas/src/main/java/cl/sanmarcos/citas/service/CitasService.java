package cl.sanmarcos.citas.service;

import cl.sanmarcos.citas.dto.CitaRequest;
import cl.sanmarcos.citas.dto.CitaResponse;
import cl.sanmarcos.citas.model.Cita;
import cl.sanmarcos.citas.model.EstadoCita;
import cl.sanmarcos.citas.repository.CitasRepository;
import cl.sanmarcos.common.exception.ApiException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio de las citas veterinarias. */
@Service
public class CitasService {
  public static final String SIN_PREFERENCIA = "Sin preferencia";
  private static final ZoneId ZONA = ZoneId.of("America/Santiago");

  private final CitasRepository citas;

  public CitasService(CitasRepository citas) {
    this.citas = citas;
  }

  /**
   * Reglas: la fecha no puede ser pasada; un veterinario no puede tener dos citas vigentes en
   * el mismo horario; y un cliente no puede repetir la misma cita para la misma mascota.
   * La cita siempre nace PENDIENTE: el cliente no puede confirmársela a sí mismo.
   */
  @Transactional
  public CitaResponse solicitar(Long usuarioId, CitaRequest form) {
    if (form.fecha().isBefore(LocalDate.now(ZONA))) {
      throw new ApiException(400, "La fecha de la cita no puede ser anterior a hoy.");
    }
    String veterinario =
        form.veterinario() == null || form.veterinario().isBlank()
            ? SIN_PREFERENCIA
            : form.veterinario().trim();
    String mascota = form.mascota().trim();

    if (!SIN_PREFERENCIA.equals(veterinario)
        && citas.existsByVeterinarioAndFechaAndHoraAndEstadoNot(
            veterinario, form.fecha(), form.hora(), EstadoCita.RECHAZADA)) {
      throw new ApiException(
          409, veterinario + " ya tiene una cita en ese horario. Elige otra hora.");
    }
    if (citas.existsByUsuarioIdAndMascotaIgnoreCaseAndFechaAndHoraAndEstadoNot(
        usuarioId, mascota, form.fecha(), form.hora(), EstadoCita.RECHAZADA)) {
      throw new ApiException(409, "Ya solicitaste una cita para " + mascota + " en ese horario.");
    }

    Cita c = new Cita();
    c.setUsuarioId(usuarioId);
    c.setCliente(form.cliente().trim());
    c.setRut(form.rut().trim());
    c.setTelefono(form.telefono().trim());
    c.setCorreo(form.correo().trim().toLowerCase());
    c.setMascota(mascota);
    c.setEspecie(form.especie());
    c.setRaza(form.raza() == null ? "" : form.raza().trim());
    c.setFechaNacimiento(form.fechaNacimiento());
    c.setSexo(form.sexo());
    c.setEsterilizado(form.esterilizado());
    c.setServicio(form.servicio());
    c.setDescripcion(form.descripcion() == null ? "" : form.descripcion().trim());
    c.setVeterinario(veterinario);
    c.setFecha(form.fecha());
    c.setHora(form.hora());
    c.setEstado(EstadoCita.PENDIENTE);
    citas.save(c);
    // El código se arma con el id que generó la base de datos: CITA-001, CITA-002...
    c.setCodigo(String.format("CITA-%03d", c.getId()));
    return CitaResponse.de(c);
  }

  @Transactional(readOnly = true)
  public List<CitaResponse> listar() {
    return citas.findAllByOrderByFechaDescHoraDesc().stream().map(CitaResponse::de).toList();
  }

  @Transactional(readOnly = true)
  public List<CitaResponse> listarDe(Long usuarioId) {
    return citas.findByUsuarioIdOrderByFechaAscHoraAsc(usuarioId).stream()
        .map(CitaResponse::de)
        .toList();
  }

  /** @param soloDeUsuario si no es null, la cita debe pertenecer a ese usuario. */
  @Transactional(readOnly = true)
  public CitaResponse obtener(Long id, Long soloDeUsuario) {
    Cita c = buscar(id);
    if (soloDeUsuario != null && !soloDeUsuario.equals(c.getUsuarioId())) {
      throw new ApiException(404, "La cita no existe.");
    }
    return CitaResponse.de(c);
  }

  @Transactional
  public CitaResponse cambiarEstado(Long id, EstadoCita estado) {
    Cita c = buscar(id);
    c.setEstado(estado);
    return CitaResponse.de(c);
  }

  @Transactional
  public void eliminar(Long id) {
    citas.delete(buscar(id));
  }

  private Cita buscar(Long id) {
    return citas.findById(id).orElseThrow(() -> new ApiException(404, "La cita no existe."));
  }
}
