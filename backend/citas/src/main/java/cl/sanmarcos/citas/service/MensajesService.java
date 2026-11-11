package cl.sanmarcos.citas.service;

import cl.sanmarcos.citas.dto.MensajeRequest;
import cl.sanmarcos.citas.dto.MensajeResponse;
import cl.sanmarcos.citas.model.EstadoMensaje;
import cl.sanmarcos.citas.model.MensajeContacto;
import cl.sanmarcos.citas.repository.MensajesRepository;
import cl.sanmarcos.common.exception.ApiException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MensajesService {
  private final MensajesRepository mensajes;

  public MensajesService(MensajesRepository mensajes) {
    this.mensajes = mensajes;
  }

  @Transactional
  public MensajeResponse enviar(MensajeRequest form) {
    MensajeContacto m = new MensajeContacto();
    m.setNombre(form.nombre().trim());
    m.setCorreo(form.correo().trim().toLowerCase());
    m.setTelefono(form.telefono() == null ? "" : form.telefono().trim());
    m.setAsunto(form.asunto().trim());
    m.setMensaje(form.mensaje().trim());
    return MensajeResponse.de(mensajes.save(m));
  }

  @Transactional(readOnly = true)
  public List<MensajeResponse> listar() {
    return mensajes.findAllByOrderByIdDesc().stream().map(MensajeResponse::de).toList();
  }

  @Transactional(readOnly = true)
  public MensajeResponse obtener(Long id) {
    return MensajeResponse.de(buscar(id));
  }

  @Transactional
  public MensajeResponse cambiarEstado(Long id, EstadoMensaje estado) {
    MensajeContacto m = buscar(id);
    m.setEstado(estado);
    return MensajeResponse.de(m);
  }

  @Transactional
  public void eliminar(Long id) {
    mensajes.delete(buscar(id));
  }

  private MensajeContacto buscar(Long id) {
    return mensajes
        .findById(id)
        .orElseThrow(() -> new ApiException(404, "El mensaje no existe."));
  }
}
