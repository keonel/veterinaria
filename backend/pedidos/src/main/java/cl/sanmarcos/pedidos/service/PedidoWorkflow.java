package cl.sanmarcos.pedidos.service;

import cl.sanmarcos.common.exception.ApiException;
import cl.sanmarcos.pedidos.client.CatalogoClient;
import cl.sanmarcos.pedidos.dto.InventarioResponse;
import cl.sanmarcos.pedidos.dto.PedidoRequest;
import cl.sanmarcos.pedidos.dto.PedidoResponse;
import cl.sanmarcos.pedidos.model.EstadoPedido;
import org.springframework.stereotype.Service;

/**
 * Coordina una compra entre dos microservicios con bases de datos separadas:
 *
 * <ol>
 *   <li>Pedidos registra el pedido como PENDIENTE (transacción en su base).
 *   <li>Pedidos llama por HTTP a Catálogo, que valida y descuenta el stock (transacción en la
 *       base de Catálogo) y devuelve un comprobante con los precios.
 *   <li>Pedidos guarda las líneas y deja el pedido CONFIRMADO (otra transacción local).
 * </ol>
 *
 * <p>No existe una transacción única que abarque ambas bases. Si la respuesta de Catálogo se
 * pierde, el pedido queda PENDIENTE; al reintentar con la misma Idempotency-Key se retoma el
 * mismo pedido y Catálogo devuelve el comprobante guardado sin descontar dos veces.
 */
@Service
public class PedidoWorkflow {
  private final PedidoStore store;
  private final CatalogoClient catalogo;

  public PedidoWorkflow(PedidoStore store, CatalogoClient catalogo) {
    this.store = store;
    this.catalogo = catalogo;
  }

  public PedidoResponse comprar(
      Long usuarioId, String nombre, String correo, String clave, PedidoRequest form) {
    if (clave == null || clave.isBlank() || clave.length() > 80) {
      throw new ApiException(400, "La cabecera Idempotency-Key es obligatoria (máx. 80).");
    }

    Long pedidoId;
    PedidoResponse previo = store.buscarPorClave(clave).orElse(null);
    if (previo != null) {
      if (!previo.usuarioId().equals(usuarioId)) {
        throw new ApiException(409, "La clave de idempotencia ya fue utilizada.");
      }
      if (previo.estado() == EstadoPedido.CANCELADO) {
        throw new ApiException(409, "Este intento de compra fue cancelado. Vuelve a intentarlo.");
      }
      if (previo.estado() != EstadoPedido.PENDIENTE) {
        return previo; // ya estaba confirmado: se devuelve el mismo pedido, sin duplicar
      }
      pedidoId = previo.id(); // quedó a medias: se retoma
    } else {
      pedidoId = store.crearPendiente(usuarioId, nombre, correo, clave);
    }

    InventarioResponse comprobante;
    try {
      comprobante = catalogo.vender(pedidoId, form.items());
    } catch (ApiException e) {
      // 400/409: Catálogo rechazó la venta (sin stock, producto inactivo) y no descontó nada.
      // Con 502/503 no sabemos qué ocurrió: el pedido sigue PENDIENTE para poder reintentar.
      if (e.getStatus() == 400 || e.getStatus() == 409) store.cancelar(pedidoId);
      throw e;
    }
    return store.confirmar(pedidoId, comprobante);
  }
}
