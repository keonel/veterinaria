package cl.sanmarcos.citas;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Pruebas de integración de citas y mensajes: reglas de negocio y permisos por rol. */
@ActiveProfiles("h2")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:citas-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class CitasIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  private static RequestPostProcessor usuario(long id, String rol) {
    return jwt()
        .jwt(j -> j.subject(String.valueOf(id)))
        .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
  }

  private String cita(String mascota, String veterinario, LocalDate fecha, String hora)
      throws Exception {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("cliente", "Camila Cliente");
    body.put("rut", "33333333-3");
    body.put("telefono", "+56912345678");
    body.put("correo", "cliente@gmail.com");
    body.put("mascota", mascota);
    body.put("especie", "Perro");
    body.put("raza", "Quiltro");
    body.put("fechaNacimiento", null);
    body.put("sexo", "Macho");
    body.put("esterilizado", "Sí");
    body.put("servicio", "Consulta General");
    body.put("descripcion", "Control anual");
    body.put("veterinario", veterinario);
    body.put("fecha", fecha.toString());
    body.put("hora", hora);
    body.put("estado", "CONFIRMADA"); // el cliente no puede autoconfirmarse: se ignora
    return json.writeValueAsString(body);
  }

  private long solicitar(long usuarioId, String mascota, String veterinario, String hora)
      throws Exception {
    String body =
        mvc.perform(
                post("/api/v1/citas")
                    .with(usuario(usuarioId, "CLIENTE"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(cita(mascota, veterinario, LocalDate.now().plusDays(3), hora)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(body).get("id").asLong();
  }

  @Test
  void solicitarCitaExigeSesionDeCliente() throws Exception {
    String body = cita("Firulais", "Sin preferencia", LocalDate.now().plusDays(2), "10:00");
    mvc.perform(post("/api/v1/citas").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/v1/citas")
                .with(usuario(2, "VENDEDOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void laCitaNacePendienteYConCodigo() throws Exception {
    mvc.perform(
            post("/api/v1/citas")
                .with(usuario(20, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cita("Luna", "Sin preferencia", LocalDate.now().plusDays(5), "11:30")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.estado").value("PENDIENTE"))
        .andExpect(jsonPath("$.usuarioId").value(20))
        .andExpect(jsonPath("$.codigo").value(org.hamcrest.Matchers.startsWith("CITA-")));
  }

  @Test
  void noSeAceptanFechasPasadasNiDatosIncompletos() throws Exception {
    mvc.perform(
            post("/api/v1/citas")
                .with(usuario(21, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cita("Toby", "Sin preferencia", LocalDate.now().minusDays(1), "10:00")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/citas")
                .with(usuario(21, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cita("", "Sin preferencia", LocalDate.now().plusDays(1), "25:99")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.mascota").exists())
        .andExpect(jsonPath("$.campos.hora").exists());
  }

  @Test
  void unVeterinarioNoTieneDosCitasEnElMismoHorario() throws Exception {
    solicitar(22, "Max", "Dr. Roberto Soto", "15:00");
    mvc.perform(
            post("/api/v1/citas")
                .with(usuario(23, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cita("Nala", "Dr. Roberto Soto", LocalDate.now().plusDays(3), "15:00")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.mensaje").exists());
  }

  @Test
  void elClienteSoloVeSusCitas() throws Exception {
    long id = solicitar(24, "Coco", "Sin preferencia", "09:30");
    mvc.perform(get("/api/v1/citas/mis-citas").with(usuario(24, "CLIENTE")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/v1/citas/" + id).with(usuario(25, "CLIENTE")))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/citas").with(usuario(24, "CLIENTE")))
        .andExpect(status().isForbidden());
  }

  @Test
  void soloElAdminConfirmaYElimina() throws Exception {
    long id = solicitar(26, "Kira", "Sin preferencia", "12:00");
    String confirmar = "{\"estado\":\"CONFIRMADA\"}";
    mvc.perform(
            put("/api/v1/citas/" + id + "/estado")
                .with(usuario(26, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmar))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/citas").with(usuario(2, "VENDEDOR")))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/v1/citas/" + id + "/estado")
                .with(usuario(1, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmar))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    mvc.perform(delete("/api/v1/citas/" + id).with(usuario(1, "ADMIN")))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/citas/" + id).with(usuario(1, "ADMIN")))
        .andExpect(status().isNotFound());
  }

  @Test
  void elContactoEsPublicoPeroSoloElAdminLeeLosMensajes() throws Exception {
    String mensaje =
        json.writeValueAsString(
            Map.of(
                "nombre", "Visitante",
                "correo", "visita@gmail.com",
                "telefono", "",
                "asunto", "Horarios",
                "mensaje", "¿Atienden los sábados?"));
    String body =
        mvc.perform(post("/api/v1/mensajes").contentType(MediaType.APPLICATION_JSON).content(mensaje))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("NUEVO"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(body).get("id").asLong();

    mvc.perform(get("/api/v1/mensajes")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/mensajes").with(usuario(30, "CLIENTE")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/mensajes").with(usuario(1, "ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].asunto").value("Horarios"));
    mvc.perform(
            put("/api/v1/mensajes/" + id + "/estado")
                .with(usuario(1, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"RESPONDIDO\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("RESPONDIDO"));
    mvc.perform(delete("/api/v1/mensajes/" + id).with(usuario(1, "ADMIN")))
        .andExpect(status().isNoContent());
  }

  @Test
  void unMensajeIncompletoDevuelve400() throws Exception {
    mvc.perform(
            post("/api/v1/mensajes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\",\"correo\":\"no-es-correo\",\"asunto\":\"\",\"mensaje\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.correo").exists());
  }
}
