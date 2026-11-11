package cl.sanmarcos.catalogo.repository;

import cl.sanmarcos.catalogo.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductosRepository extends JpaRepository<Producto, Long> {
  Optional<Producto> findByCodigoIgnoreCase(String codigo);

  List<Producto> findAllByOrderByIdAsc();

  /**
   * Lee los productos bloqueando sus filas hasta que termine la transacción. Así dos compras
   * simultáneas del último producto no pueden descontar el mismo stock.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Producto p where p.id in :ids order by p.id")
  List<Producto> bloquearPorIds(Collection<Long> ids);
}
