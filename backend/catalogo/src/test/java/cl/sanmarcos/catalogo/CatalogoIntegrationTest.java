package cl.sanmarcos.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.sanmarcos.catalogo.dto.ProductoRequest;
import cl.sanmarcos.catalogo.dto.ProductoResponse;
import cl.sanmarcos.catalogo.repository.ProductosRepository;
import cl.sanmarcos.catalogo.service.CatalogoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de integración del catálogo: CRUD, permisos por rol y descuento de stock. */
@ActiveProfiles("h2")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:catalogo-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class CatalogoIntegrationTest {
  private static final String CLAVE = "local-cambiar-clave-interna-32-caracteres";

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired CatalogoService service;
  @Autowired ProductosRepository productos;

  private ProductoRequest producto(String codigo, int precio, int stock) {
    return new ProductoRequest(
        codigo, "producto", "Producto " + codigo, "Pruebas", "", precio, stock, 1, "", true);
  }

  private String cuerpo(String codigo, int precio, int stock) throws Exception {
    return json.writeValueAsString(producto(codigo, precio, stock));
  }

  private int stock(Long id) {
    return productos.findById(id).orElseThrow().getStockActual();
  }

  @Test
  void elCatalogoEsPublico() throws Exception {
    mvc.perform(get("/api/v1/productos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].codigo").value("SM001"));
  }

  @Test
  void crearSinSesionDevuelve401() throws Exception {
    mvc.perform(
            post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("T401", 1000, 5)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "CLIENTE")
  void elClienteNoPuedeCrearProductos() throws Exception {
    mvc.perform(
            post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("T403", 1000, 5)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "VENDEDOR")
  void elVendedorVePeroNoModificaNiElimina() throws Exception {
    mvc.perform(get("/api/v1/productos/1")).andExpect(status().isOk());
    mvc.perform(
            put("/api/v1/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("SM001", 1, 1)))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void elAdminCreaYUnCodigoDuplicadoDevuelve409() throws Exception {
    mvc.perform(
            post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("dup01", 5000, 3)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.codigo").value("DUP01"));
    // mismo código con otras mayúsculas: sigue siendo duplicado
    mvc.perform(
            post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("Dup01", 7000, 1)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void losDatosInvalidosDevuelven400ConElCampo() throws Exception {
    mvc.perform(
            post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("NEG01", -10, 3)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.precio").exists());
    mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("{mal"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void elAdminActualizaYElimina() throws Exception {
    ProductoResponse creado = service.guardar(null, producto("EDIT01", 1000, 5));
    mvc.perform(
            put("/api/v1/productos/" + creado.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("EDIT01", 2500, 9)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.precio").value(2500))
        .andExpect(jsonPath("$.stockActual").value(9));
    mvc.perform(delete("/api/v1/productos/" + creado.id())).andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/productos/" + creado.id())).andExpect(status().isNotFound());
  }

  @Test
  void laVentaDescuentaStockUnaSolaVez() throws Exception {
    ProductoResponse p = service.guardar(null, producto("VENTA01", 4000, 10));
    String items = json.writeValueAsString(List.of(Map.of("productoId", p.id(), "cantidad", 3)));

    mvc.perform(
            post("/internal/ventas/9001")
                .header("X-Internal-Key", CLAVE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(items))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(12000))
        .andExpect(jsonPath("$.lineas[0].precioUnitario").value(4000));
    assertEquals(7, stock(p.id()));

    // Reintento del mismo pedido: mismo comprobante, sin descontar otra vez (idempotencia)
    mvc.perform(
            post("/internal/ventas/9001")
                .header("X-Internal-Key", CLAVE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(items))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(12000));
    assertEquals(7, stock(p.id()));
  }

  @Test
  void siFaltaStockDeUnProductoNoSeDescuentaNinguno() throws Exception {
    ProductoResponse conStock = service.guardar(null, producto("VENTA02", 1000, 10));
    ProductoResponse sinStock = service.guardar(null, producto("VENTA03", 1000, 1));
    String items =
        json.writeValueAsString(
            List.of(
                Map.of("productoId", conStock.id(), "cantidad", 2),
                Map.of("productoId", sinStock.id(), "cantidad", 5)));

    mvc.perform(
            post("/internal/ventas/9002")
                .header("X-Internal-Key", CLAVE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(items))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.mensaje").exists());
    assertEquals(10, stock(conStock.id()));
    assertEquals(1, stock(sinStock.id()));
  }

  @Test
  void elEndpointInternoExigeLaClave() throws Exception {
    mvc.perform(
            post("/internal/ventas/9003")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"productoId\":1,\"cantidad\":1}]"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/internal/ventas/9003")
                .header("X-Internal-Key", "clave-incorrecta")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"productoId\":1,\"cantidad\":1}]"))
        .andExpect(status().isForbidden());
  }
}
