package cl.sanmarcos.pedidos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.sanmarcos.common.exception.ApiException;
import cl.sanmarcos.pedidos.client.CatalogoClient;
import cl.sanmarcos.pedidos.dto.InventarioResponse;
import cl.sanmarcos.pedidos.model.EstadoPedido;
import cl.sanmarcos.pedidos.repository.PedidosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Pruebas de integración de pedidos. El cliente HTTP hacia Catálogo se reemplaza por un doble
 * de prueba (@MockitoBean), así se prueba este servicio sin levantar el otro.
 */
@ActiveProfiles("h2")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pedidos-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class PedidosIntegrationTest {
  private static final String ITEMS = "{\"items\":[{\"productoId\":1,\"cantidad\":2}]}";

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired PedidosRepository pedidos;
  @MockitoBean CatalogoClient catalogo;

  /** Simula un JWT válido del usuario indicado con su rol. */
  private static RequestPostProcessor usuario(long id, String rol) {
    return jwt()
        .jwt(
            j ->
                j.subject(String.valueOf(id))
                    .claim("nombre", "Camila Cliente")
                    .claim("correo", "cliente@gmail.com"))
        .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
  }

  private static InventarioResponse comprobante() {
    return new InventarioResponse(
        1L, List.of(new InventarioResponse.Linea(1L, "SM001", "Milpro", 15000, 2)), 30000);
  }

  @BeforeEach
  void preparar() {
    when(catalogo.vender(anyLong(), any())).thenReturn(comprobante());
  }

  private long comprar(long usuarioId, String clave) throws Exception {
    String body =
        mvc.perform(
                post("/api/v1/pedidos")
                    .with(usuario(usuarioId, "CLIENTE"))
                    .header("Idempotency-Key", clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ITEMS))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(body).get("id").asLong();
  }

  @Test
  void comprarSinSesionDevuelve401() throws Exception {
    mvc.perform(
            post("/api/v1/pedidos")
                .header("Idempotency-Key", "k-401")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ITEMS))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void soloElClientePuedeComprar() throws Exception {
    mvc.perform(
            post("/api/v1/pedidos")
                .with(usuario(2, "VENDEDOR"))
                .header("Idempotency-Key", "k-403")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ITEMS))
        .andExpect(status().isForbidden());
  }

  @Test
  void laCompraQuedaConfirmadaConLosPreciosDelCatalogo() throws Exception {
    mvc.perform(
            post("/api/v1/pedidos")
                .with(usuario(10, "CLIENTE"))
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ITEMS))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
        .andExpect(jsonPath("$.usuarioId").value(10))
        .andExpect(jsonPath("$.total").value(30000))
        .andExpect(jsonPath("$.items[0].precioUnitario").value(15000))
        .andExpect(jsonPath("$.items[0].subtotal").value(30000));
  }

  @Test
  void repetirLaMismaClaveNoDuplicaLaCompra() throws Exception {
    String clave = UUID.randomUUID().toString();
    long primero = comprar(11, clave);
    long segundo = comprar(11, clave);
    assertEquals(primero, segundo);
    assertEquals(1, pedidos.findByUsuarioIdOrderByIdDesc(11L).size());
    // Catálogo fue llamado una sola vez: el stock no se descuenta dos veces
    verify(catalogo, times(1)).vender(anyLong(), any());
  }

  @Test
  void sinStockLaCompraSeRechazaYElPedidoQuedaCancelado() throws Exception {
    when(catalogo.vender(anyLong(), any()))
        .thenThrow(new ApiException(409, "Stock insuficiente para Milpro. Disponible: 0."));
    mvc.perform(
            post("/api/v1/pedidos")
                .with(usuario(12, "CLIENTE"))
                .header("Idempotency-Key", "k-sin-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ITEMS))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.mensaje").value("Stock insuficiente para Milpro. Disponible: 0."));
    assertEquals(
        EstadoPedido.CANCELADO, pedidos.findByIdempotencyKey("k-sin-stock").orElseThrow().getEstado());
  }

  @Test
  void faltaLaClaveDeIdempotenciaOElCarritoVacio() throws Exception {
    mvc.perform(
            post("/api/v1/pedidos")
                .with(usuario(13, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ITEMS))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/pedidos")
                .with(usuario(13, "CLIENTE"))
                .header("Idempotency-Key", "k-vacio")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[]}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.items").exists());
  }

  @Test
  void elVendedorVeListaYDetallePeroNoModifica() throws Exception {
    long id = comprar(14, UUID.randomUUID().toString());
    mvc.perform(get("/api/v1/pedidos").with(usuario(2, "VENDEDOR")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").exists());
    mvc.perform(get("/api/v1/pedidos/" + id).with(usuario(2, "VENDEDOR")))
        .andExpect(status().isOk());
    mvc.perform(
            put("/api/v1/pedidos/" + id + "/estado")
                .with(usuario(2, "VENDEDOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"ENTREGADO\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/v1/pedidos/" + id).with(usuario(2, "VENDEDOR")))
        .andExpect(status().isForbidden());
  }

  @Test
  void elClienteSoloVeSusPropiosPedidos() throws Exception {
    long id = comprar(15, UUID.randomUUID().toString());
    mvc.perform(get("/api/v1/pedidos/" + id).with(usuario(15, "CLIENTE")))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/pedidos/" + id).with(usuario(16, "CLIENTE")))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/pedidos").with(usuario(15, "CLIENTE")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/pedidos/mis-pedidos").with(usuario(15, "CLIENTE")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void elAdminEntregaPeroNoEliminaUnaVentaConfirmada() throws Exception {
    long id = comprar(17, UUID.randomUUID().toString());
    mvc.perform(
            put("/api/v1/pedidos/" + id + "/estado")
                .with(usuario(1, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"ENTREGADO\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("ENTREGADO"));
    // transición no permitida
    mvc.perform(
            put("/api/v1/pedidos/" + id + "/estado")
                .with(usuario(1, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"CANCELADO\"}"))
        .andExpect(status().isConflict());
    mvc.perform(delete("/api/v1/pedidos/" + id).with(usuario(1, "ADMIN")))
        .andExpect(status().isConflict());
  }
}
