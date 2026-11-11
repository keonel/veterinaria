package cl.sanmarcos.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** scanBasePackages incluye cl.sanmarcos.common para cargar la configuración compartida. */
@SpringBootApplication(scanBasePackages = "cl.sanmarcos")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
