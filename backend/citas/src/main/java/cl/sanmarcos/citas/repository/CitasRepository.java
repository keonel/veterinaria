package cl.sanmarcos.citas.repository;

import cl.sanmarcos.citas.model.Cita;
import cl.sanmarcos.citas.model.EstadoCita;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitasRepository extends JpaRepository<Cita, Long> {
  List<Cita> findAllByOrderByFechaDescHoraDesc();

  List<Cita> findByUsuarioIdOrderByFechaAscHoraAsc(Long usuarioId);

  /** ¿El veterinario ya tiene una cita vigente (no rechazada) en ese horario? */
  boolean existsByVeterinarioAndFechaAndHoraAndEstadoNot(
      String veterinario, LocalDate fecha, String hora, EstadoCita estado);

  /** ¿Este usuario ya pidió hora para la misma mascota en ese mismo horario? */
  boolean existsByUsuarioIdAndMascotaIgnoreCaseAndFechaAndHoraAndEstadoNot(
      Long usuarioId, String mascota, LocalDate fecha, String hora, EstadoCita estado);
}
