package cl.sanmarcos.usuarios.config;

import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Datos iniciales: crea una cuenta por cada rol la primera vez que arranca el servicio.
 * Las contraseñas vienen de variables de entorno (.env) y se guardan con BCrypt.
 */
@Component
public class Seed implements CommandLineRunner {
  private final UsuariosRepository usuarios;
  private final PasswordEncoder passwords;
  private final String claveAdmin;
  private final String claveVendedor;
  private final String claveCliente;

  public Seed(
      UsuariosRepository usuarios,
      PasswordEncoder passwords,
      @Value("${app.admin-password}") String claveAdmin,
      @Value("${app.vendedor-password}") String claveVendedor,
      @Value("${app.cliente-password}") String claveCliente) {
    this.usuarios = usuarios;
    this.passwords = passwords;
    this.claveAdmin = claveAdmin;
    this.claveVendedor = claveVendedor;
    this.claveCliente = claveCliente;
  }

  @Override
  public void run(String... args) {
    crear("11111111-1", "Admin", "San Marcos", "admin@sanmarcos.cl", claveAdmin, Rol.ADMIN);
    crear("22222222-2", "Valentina", "Vendedora", "vendedor@sanmarcos.cl", claveVendedor,
        Rol.VENDEDOR);
    crear("33333333-3", "Camila", "Cliente", "cliente@gmail.com", claveCliente, Rol.CLIENTE);
  }

  private void crear(
      String run, String nombre, String apellidos, String correo, String clave, Rol rol) {
    if (usuarios.existsByCorreoIgnoreCase(correo) || usuarios.existsByRun(run)) return;
    Usuario u = new Usuario();
    u.setRun(run);
    u.setNombre(nombre);
    u.setApellidos(apellidos);
    u.setCorreo(correo);
    u.setTelefono("+56 9 1234 5678");
    u.setRegion("Valparaíso");
    u.setComuna("Viña del Mar");
    u.setDireccion("Av. San Marcos 123");
    u.setPasswordHash(passwords.encode(clave));
    u.setRol(rol);
    usuarios.save(u);
  }
}
