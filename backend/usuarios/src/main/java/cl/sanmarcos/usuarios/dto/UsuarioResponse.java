package cl.sanmarcos.usuarios.dto;

import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;

/** Lo que la API devuelve de un usuario. No incluye passwordHash: la entidad no se expone. */
public record UsuarioResponse(
    Long id,
    String run,
    String nombre,
    String apellidos,
    String correo,
    String telefono,
    String region,
    String comuna,
    String direccion,
    Rol rol,
    boolean activo) {

  public static UsuarioResponse de(Usuario u) {
    return new UsuarioResponse(
        u.getId(),
        u.getRun(),
        u.getNombre(),
        u.getApellidos(),
        u.getCorreo(),
        u.getTelefono(),
        u.getRegion(),
        u.getComuna(),
        u.getDireccion(),
        u.getRol(),
        u.isActivo());
  }
}
