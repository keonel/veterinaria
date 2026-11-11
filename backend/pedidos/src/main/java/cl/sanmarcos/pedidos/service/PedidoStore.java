package cl.sanmarcos.pedidos.service;

import cl.sanmarcos.common.exception.ApiException;
import cl.sanmarcos.pedidos.dto.InventarioResponse;
import cl.sanmarcos.pedidos.dto.PedidoResponse;
import cl.sanmarcos.pedidos.mapper.PedidoMapper;
import cl.sanmarcos.pedidos.model.EstadoPedido;
import cl.sanmarcos.pedidos.model.Pedido;
import cl.sanmarcos.pedidos.model.PedidoLinea;
import cl.sanmarcos.pedidos.repository.PedidosRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operaciones sobre la base de Pedidos. Cada método es una transacción local corta.
 * El flujo de compra completo (que además llama a Catálogo) lo coordina PedidoWorkflow.
 */
@Service
public class PedidoStore {
  private final PedidosRepository pedidos;

  public PedidoStore(PedidosRepository pedidos) {
    this.pedidos = pedidos;
  }

  @Transactional(readOnly = true)
  public Optional<PedidoResponse> buscarPorClave(String clave) {
    return pedidos.findByIdempotencyKey(clave).map(PedidoMapper::aRespuesta);
  }

  @Transactional
  public Long crearPendiente(Long usuarioId, String nombre, String correo, String clave) {
    Pedido p = new Pedido();
    p.setUsuarioId(usuarioId);
    p.setClienteNombre(nombre);
    p.setClienteCorreo(correo);
    p.setIdempotencyKey(clave);
    p.setEstado(EstadoPedido.PENDIENTE);
    return pedidos.save(p).getId();
  }

  /** Guarda las líneas con el precio de compra informado por Catálogo y confirma el pedido. */
  @Transactional
  public PedidoResponse confirmar(Long id, InventarioResponse comprobante) {
    Pedido p = buscar(id);
    if (p.getEstado() == EstadoPedido.PENDIENTE) {
      p.getItems().clear();
      for (InventarioResponse.Linea l : comprobante.lineas()) {
        p.getItems()
            .add(
                new PedidoLinea(
                    l.productoId(), l.codigo(), l.nombre(), l.precioUnitario(), l.cantidad()));
      }
      p.setTotal(comprobante.total());
      p.setEstado(EstadoPedido.CONFIRMADO);
    }
    return PedidoMapper.aRespuesta(p);
  }

  @Transactional
  public void cancelar(Long id) {
    Pedido p = buscar(id);
    if (p.getEstado() == EstadoPedido.PENDIENTE) p.setEstado(EstadoPedido.CANCELADO);
  }

  @Transactional(readOnly = true)
  public List<PedidoResponse> listar() {
    return pedidos.findAllByOrderByIdDesc().stream().map(PedidoMapper::aRespuesta).toList();
  }

  @Transactional(readOnly = true)
  public List<PedidoResponse> listarDe(Long usuarioId) {
    return pedidos.findByUsuarioIdOrderByIdDesc(usuarioId).stream()
        .map(PedidoMapper::aRespuesta)
        .toList();
  }

  /**
   * @param soloDeUsuario si no es null, el pedido debe pertenecer a ese usuario (un CLIENTE
   *     solo ve sus compras). Se responde 404 para no revelar que el pedido existe.
   */
  @Transactional(readOnly = true)
  public PedidoResponse obtener(Long id, Long soloDeUsuario) {
    Pedido p = buscar(id);
    if (soloDeUsuario != null && !soloDeUsuario.equals(p.getUsuarioId())) {
      throw new ApiException(404, "El pedido no existe.");
    }
    return PedidoMapper.aRespuesta(p);
  }

  /** Transiciones permitidas: CONFIRMADO -> ENTREGADO y PENDIENTE -> CANCELADO. */
  @Transactional
  public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo) {
    Pedido p = buscar(id);
    EstadoPedido actual = p.getEstado();
    boolean permitido =
        (actual == EstadoPedido.CONFIRMADO && nuevo == EstadoPedido.ENTREGADO)
            || (actual == EstadoPedido.PENDIENTE && nuevo == EstadoPedido.CANCELADO);
    if (!permitido) {
      throw new ApiException(
          409, "No se puede cambiar un pedido de " + actual + " a " + nuevo + ".");
    }
    p.setEstado(nuevo);
    return PedidoMapper.aRespuesta(p);
  }

  /** Un pedido confirmado es el registro de una venta: solo se eliminan los cancelados. */
  @Transactional
  public void eliminar(Long id) {
    Pedido p = buscar(id);
    if (p.getEstado() != EstadoPedido.CANCELADO) {
      throw new ApiException(409, "Solo se pueden eliminar pedidos cancelados.");
    }
    pedidos.delete(p);
  }

  private Pedido buscar(Long id) {
    return pedidos.findById(id).orElseThrow(() -> new ApiException(404, "El pedido no existe."));
  }
}
