package cl.sanmarcos.usuarios.repository;

import cl.sanmarcos.usuarios.model.SesionRefresh;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SesionesRepository extends JpaRepository<SesionRefresh, Long> {
  Optional<SesionRefresh> findByTokenHash(String tokenHash);

  /** Cierra todas las sesiones de un usuario (al desactivarlo, eliminarlo o cambiar su clave). */
  @Modifying
  @Query("update SesionRefresh s set s.revocada = true where s.usuarioId = :usuarioId")
  void revocarTodas(Long usuarioId);

  @Modifying
  @Query("delete from SesionRefresh s where s.expiraEn < :ahora")
  void borrarVencidas(Instant ahora);
}
