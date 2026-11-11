package cl.sanmarcos.usuarios;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.UsuariosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Pruebas de integración: levantan el servicio completo con una base H2 en memoria y envían
 * solicitudes HTTP simuladas (MockMvc). Los tokens son reales: se obtienen haciendo login.
 */
@ActiveProfiles("h2")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:usuarios-test;DB_CLOSE_DELAY=-1",
      "app.keys-directory=target/test-keys"
    })
@AutoConfigureMockMvc
class UsuariosIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuariosRepository usuarios;

  private String registro(String run, String correo, String clave) throws Exception {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("run", run);
    body.put("nombre", "Ana");
    body.put("apellidos", "Pérez");
    body.put("correo", correo);
    body.put("telefono", "+56911112222");
    body.put("region", "Valparaíso");
    body.put("comuna", "Viña del Mar");
    body.put("direccion", "Calle 1");
    body.put("contrasena", clave);
    body.put("rol", "ADMIN"); // intento de escalar privilegios: debe ignorarse
    return json.writeValueAsString(body);
  }

  private MvcResult login(String correo, String clave) throws Exception {
    String body = json.writeValueAsString(Map.of("correo", correo, "contrasena", clave));
    return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn();
  }

  private String token(String correo, String clave) throws Exception {
    MvcResult r = login(correo, clave);
    return json.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
  }

  @Test
  void registroCreaSiempreUnClienteConClaveCifrada() throws Exception {
    mvc.perform(
            post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro("12.345.678-5", "ana@gmail.com", "Clave123")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rol").value("CLIENTE"))
        .andExpect(jsonPath("$.run").value("12345678-5"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());

    Usuario guardado = usuarios.findByCorreoIgnoreCase("ana@gmail.com").orElseThrow();
    assertTrue(guardado.getPasswordHash().startsWith("$2"), "La clave debe guardarse con BCrypt");
    assertNotEquals("Clave123", guardado.getPasswordHash());
  }

  @Test
  void registroConCorreoDuplicadoDevuelve409() throws Exception {
    mvc.perform(
            post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro("44444444-4", "repetido@gmail.com", "Clave123")))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro("55555555-5", "repetido@gmail.com", "Clave123")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.mensaje").exists());
  }

  @Test
  void registroRechazaRunInvalidoYClaveDebil() throws Exception {
    mvc.perform(
            post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro("12345678-9", "run@gmail.com", "Clave123")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro("66666666-6", "debil@gmail.com", "clave")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro("66666666-6", "otro@hotmail.com", "Clave123")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.correo").exists());
  }

  @Test
  void loginConClaveIncorrectaDevuelve401() throws Exception {
    MvcResult r = login("admin@sanmarcos.cl", "Incorrecta1");
    org.junit.jupiter.api.Assertions.assertEquals(401, r.getResponse().getStatus());
  }

  @Test
  void loginEntregaJwtYCookieHttpOnly() throws Exception {
    MvcResult r = login("cliente@gmail.com", "Cliente123");
    org.junit.jupiter.api.Assertions.assertEquals(200, r.getResponse().getStatus());
    String setCookie = r.getResponse().getHeader("Set-Cookie");
    assertNotNull(setCookie);
    assertTrue(setCookie.contains("HttpOnly"), "El refresh token debe ir en cookie HttpOnly");
    assertTrue(setCookie.contains("SameSite=Strict"));

    String access = json.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + access))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correo").value("cliente@gmail.com"))
        .andExpect(jsonPath("$.rol").value("CLIENTE"));
  }

  @Test
  void sinTokenNoSePuedeConsultarLaSesion() throws Exception {
    mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer token-falso"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshRotaElTokenYElAnteriorDejaDeServir() throws Exception {
    Cookie primera = login("vendedor@sanmarcos.cl", "Vende1234").getResponse().getCookie("sm_refresh");
    assertNotNull(primera);

    MvcResult renovado =
        mvc.perform(post("/api/v1/auth/refresh").cookie(primera))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.usuario.rol").value("VENDEDOR"))
            .andReturn();
    Cookie segunda = renovado.getResponse().getCookie("sm_refresh");
    assertNotNull(segunda);
    assertNotEquals(primera.getValue(), segunda.getValue());

    // El refresh token ya usado quedó revocado
    mvc.perform(post("/api/v1/auth/refresh").cookie(primera)).andExpect(status().isUnauthorized());
    // Sin cookie tampoco hay sesión
    mvc.perform(post("/api/v1/auth/refresh")).andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRevocaLaSesion() throws Exception {
    Cookie cookie = login("cliente@gmail.com", "Cliente123").getResponse().getCookie("sm_refresh");
    mvc.perform(post("/api/v1/auth/logout").cookie(cookie)).andExpect(status().isNoContent());
    mvc.perform(post("/api/v1/auth/refresh").cookie(cookie)).andExpect(status().isUnauthorized());
  }

  @Test
  void soloElAdministradorGestionaUsuarios() throws Exception {
    mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
    mvc.perform(
            get("/api/v1/usuarios")
                .header("Authorization", "Bearer " + token("cliente@gmail.com", "Cliente123")))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/v1/usuarios")
                .header("Authorization", "Bearer " + token("vendedor@sanmarcos.cl", "Vende1234")))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/v1/usuarios")
                .header("Authorization", "Bearer " + token("admin@sanmarcos.cl", "Admin1234")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].correo").exists())
        .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
  }

  @Test
  void elAdministradorNoPuedeEliminarSuPropiaCuenta() throws Exception {
    Long idAdmin = usuarios.findByCorreoIgnoreCase("admin@sanmarcos.cl").orElseThrow().getId();
    mvc.perform(
            delete("/api/v1/usuarios/" + idAdmin)
                .header("Authorization", "Bearer " + token("admin@sanmarcos.cl", "Admin1234")))
        .andExpect(status().isConflict());
  }
}
