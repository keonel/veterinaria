package cl.sanmarcos.citas.repository;

import cl.sanmarcos.citas.model.MensajeContacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajesRepository extends JpaRepository<MensajeContacto, Long> {
  List<MensajeContacto> findAllByOrderByIdDesc();
}
