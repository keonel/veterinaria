package cl.sanmarcos.usuarios.service;

import cl.sanmarcos.common.exception.ApiException;
import cl.sanmarcos.usuarios.dto.UsuarioRequest;
import cl.sanmarcos.usuarios.dto.UsuarioResponse;
import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.SesionesRepository;
import cl.sanmarcos.usuarios.repository.UsuariosRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de usuarios que usa el administrador. */
@Service
public class UsuariosService {
  private final UsuariosRepository usuarios;
  private final SesionesRepository sesiones;
  private final PasswordEncoder passwords;

  public UsuariosService(
      UsuariosRepository usuarios, SesionesRepository sesiones, PasswordEncoder passwords) {
    this.usuarios = usuarios;
    this.sesiones = sesiones;
    this.passwords = passwords;
  }

  @Transactional(readOnly = true)
  public List<UsuarioResponse> listar() {
    return usuarios.findAll(Sort.by("id")).stream().map(UsuarioResponse::de).toList();
  }

  @Transactional(readOnly = true)
  public UsuarioResponse obtener(Long id) {
    return UsuarioResponse.de(buscar(id));
  }

  @Transactional
  public UsuarioResponse crear(UsuarioRequest form) {
    String run = ReglasUsuario.normalizarRun(form.run());
    String correo = ReglasUsuario.normalizarCorreo(form.correo());
    ReglasUsuario.validarContrasena(form.contrasena());
    if (usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ApiException(409, "Ya existe un usuario registrado con ese correo.");
    }
    if (usuarios.existsByRun(run)) {
      throw new ApiException(409, "Ya existe un usuario registrado con ese RUN.");
    }
    Usuario u = new Usuario();
    u.setRun(run);
    u.setCorreo(correo);
    u.setPasswordHash(passwords.encode(form.contrasena()));
    copiar(form, u);
    u.setRol(form.rol());
    u.setActivo(form.activo() == null || form.activo());
    return UsuarioResponse.de(usuarios.save(u));
  }

  /**
   * @param actorId id del administrador que hace el cambio (viene del JWT). Un administrador
   *     no puede quitarse a sí mismo el rol ni desactivarse: el sistema quedaría sin acceso.
   */
  @Transactional
  public UsuarioResponse actualizar(Long id, UsuarioRequest form, Long actorId) {
    Usuario u = buscar(id);
    String run = ReglasUsuario.normalizarRun(form.run());
    String correo = ReglasUsuario.normalizarCorreo(form.correo());
    if (!correo.equalsIgnoreCase(u.getCorreo()) && usuarios.existsByCorreoIgnoreCase(correo)) {
      throw new ApiException(409, "Ese correo ya está registrado por otro usuario.");
    }
    if (!run.equals(u.getRun()) && usuarios.existsByRun(run)) {
      throw new ApiException(409, "Ese RUN ya está registrado por otro usuario.");
    }
    boolean activo = form.activo() == null ? u.isActivo() : form.activo();
    if (id.equals(actorId) && (form.rol() != Rol.ADMIN || !activo)) {
      throw new ApiException(409, "No puedes quitarte el rol de administrador ni desactivarte.");
    }
    u.setRun(run);
    u.setCorreo(correo);
    copiar(form, u);
    boolean cambiaAcceso = u.getRol() != form.rol() || (u.isActivo() && !activo);
    u.setRol(form.rol());
    u.setActivo(activo);
    if (form.contrasena() != null && !form.contrasena().isBlank()) {
      ReglasUsuario.validarContrasena(form.contrasena());
      u.setPasswordHash(passwords.encode(form.contrasena()));
      cambiaAcceso = true;
    }
    // Si cambió el rol, la clave o se desactivó: se cierran sus sesiones abiertas.
    if (cambiaAcceso) sesiones.revocarTodas(id);
    return UsuarioResponse.de(u);
  }

  @Transactional
  public void eliminar(Long id, Long actorId) {
    Usuario u = buscar(id);
    if (id.equals(actorId)) {
      throw new ApiException(409, "No puedes eliminar tu propia cuenta.");
    }
    sesiones.revocarTodas(id);
    usuarios.delete(u);
  }

  private Usuario buscar(Long id) {
    return usuarios
        .findById(id)
        .orElseThrow(() -> new ApiException(404, "El usuario no existe."));
  }

  private void copiar(UsuarioRequest form, Usuario u) {
    u.setNombre(form.nombre().trim());
    u.setApellidos(form.apellidos().trim());
    u.setTelefono(ReglasUsuario.limpiar(form.telefono()));
    u.setRegion(ReglasUsuario.limpiar(form.region()));
    u.setComuna(ReglasUsuario.limpiar(form.comuna()));
    u.setDireccion(ReglasUsuario.limpiar(form.direccion()));
  }
}
