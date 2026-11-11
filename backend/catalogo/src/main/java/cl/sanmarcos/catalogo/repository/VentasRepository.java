package cl.sanmarcos.catalogo.repository;

import cl.sanmarcos.catalogo.model.Venta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentasRepository extends JpaRepository<Venta, Long> {
  Optional<Venta> findByPedidoId(Long pedidoId);
}
