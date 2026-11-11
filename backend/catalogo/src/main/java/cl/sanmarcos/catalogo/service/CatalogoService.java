package cl.sanmarcos.catalogo.service;

import cl.sanmarcos.catalogo.dto.ProductoRequest;
import cl.sanmarcos.catalogo.dto.ProductoResponse;
import cl.sanmarcos.catalogo.dto.VentaItemRequest;
import cl.sanmarcos.catalogo.dto.VentaResponse;
import cl.sanmarcos.catalogo.model.Producto;
import cl.sanmarcos.catalogo.model.Venta;
import cl.sanmarcos.catalogo.model.VentaLinea;
import cl.sanmarcos.catalogo.repository.ProductosRepository;
import cl.sanmarcos.catalogo.repository.VentasRepository;
import cl.sanmarcos.common.exception.ApiException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio del catálogo: productos, precios y stock. */
@Service
public class CatalogoService {
  private final ProductosRepository productos;
  private final VentasRepository ventas;

  public CatalogoService(ProductosRepository productos, VentasRepository ventas) {
    this.productos = productos;
    this.ventas = ventas;
  }

  @Transactional(readOnly = true)
  public List<ProductoResponse> listar() {
    return productos.findAllByOrderByIdAsc().stream().map(ProductoResponse::de).toList();
  }

  @Transactional(readOnly = true)
  public ProductoResponse obtener(Long id) {
    return ProductoResponse.de(buscar(id));
  }

  /**
   * Crea (id == null) o actualiza un producto.
   * Reglas: el código es único sin importar mayúsculas; un servicio no maneja stock.
   */
  @Transactional
  public ProductoResponse guardar(Long id, ProductoRequest form) {
    String codigo = form.codigo().trim().toUpperCase(Locale.ROOT);
    productos
        .findByCodigoIgnoreCase(codigo)
        .filter(existente -> !existente.getId().equals(id))
        .ifPresent(
            existente -> {
              throw new ApiException(409, "Ya existe un registro con el código " + codigo + ".");
            });

    Producto p = id == null ? new Producto() : buscar(id);
    boolean esServicio = "servicio".equals(form.tipo());
    p.setCodigo(codigo);
    p.setTipo(form.tipo());
    p.setNombre(form.nombre().trim());
    p.setCategoria(form.categoria().trim());
    p.setImagen(form.imagen() == null ? "" : form.imagen().trim());
    p.setPrecio(form.precio());
    p.setStockActual(esServicio || form.stockActual() == null ? 0 : form.stockActual());
    p.setStockCritico(esServicio || form.stockCritico() == null ? 0 : form.stockCritico());
    p.setDescripcion(form.descripcion() == null ? "" : form.descripcion().trim());
    p.setActivo(form.activo() == null || form.activo());
    return ProductoResponse.de(productos.saveAndFlush(p));
  }

  @Transactional
  public void eliminar(Long id) {
    productos.delete(buscar(id));
  }

  /**
   * Descuenta el stock de un pedido y devuelve el comprobante con los precios vigentes.
   *
   * <p>Todo ocurre en UNA transacción: primero se validan todas las líneas y solo si todas
   * tienen stock se descuenta. Si una falla, no se descuenta ninguna (todo o nada).
   * Si el pedido ya fue procesado se devuelve el comprobante guardado (idempotencia).
   */
  @Transactional
  public VentaResponse vender(Long pedidoId, List<VentaItemRequest> items) {
    Venta existente = ventas.findByPedidoId(pedidoId).orElse(null);
    if (existente != null) return VentaResponse.de(existente);
    if (items == null || items.isEmpty()) {
      throw new ApiException(400, "El pedido no tiene productos.");
    }

    // Si el mismo producto viene repetido, se suman sus cantidades
    Map<Long, Integer> cantidades = new LinkedHashMap<>();
    for (VentaItemRequest item : items) {
      cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
    }

    Map<Long, Producto> encontrados =
        productos.bloquearPorIds(cantidades.keySet()).stream()
            .collect(Collectors.toMap(Producto::getId, Function.identity()));

    // 1) Validar todo antes de modificar
    for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
      Producto p = encontrados.get(linea.getKey());
      if (p == null || !p.isActivo() || !"producto".equals(p.getTipo())) {
        String nombre = p == null ? "El producto " + linea.getKey() : p.getNombre();
        throw new ApiException(409, nombre + " ya no se encuentra disponible.");
      }
      if (p.getStockActual() < linea.getValue()) {
        throw new ApiException(
            409,
            "Stock insuficiente para " + p.getNombre() + ". Disponible: " + p.getStockActual() + ".");
      }
    }

    // 2) Descontar y registrar el comprobante
    Venta venta = new Venta(pedidoId);
    for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
      Producto p = encontrados.get(linea.getKey());
      p.setStockActual(p.getStockActual() - linea.getValue());
      venta
          .getLineas()
          .add(
              new VentaLinea(
                  p.getId(), p.getCodigo(), p.getNombre(), p.getPrecio(), linea.getValue()));
    }
    return VentaResponse.de(ventas.save(venta));
  }

  private Producto buscar(Long id) {
    return productos
        .findById(id)
        .orElseThrow(() -> new ApiException(404, "El producto no existe."));
  }
}
