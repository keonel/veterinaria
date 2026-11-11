package cl.sanmarcos.pedidos.repository;

import cl.sanmarcos.pedidos.model.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidosRepository extends JpaRepository<Pedido, Long> {
  Optional<Pedido> findByIdempotencyKey(String idempotencyKey);

  List<Pedido> findAllByOrderByIdDesc();

  List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);
}
