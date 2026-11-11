package cl.sanmarcos.usuarios.repository;

import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data implementa esta interfaz: genera el SQL a partir del nombre de los métodos. */
public interface UsuariosRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByCorreoIgnoreCase(String correo);

  boolean existsByCorreoIgnoreCase(String correo);

  boolean existsByRun(String run);

  long countByRolAndActivoTrue(Rol rol);
}
