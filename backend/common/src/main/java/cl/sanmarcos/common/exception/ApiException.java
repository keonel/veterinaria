package cl.sanmarcos.common.exception;

/**
 * Error de negocio conocido. El servicio la lanza con el estado HTTP que corresponde
 * (404 no encontrado, 409 conflicto, etc.) y ApiErrors la convierte en una respuesta JSON.
 */
public class ApiException extends RuntimeException {
  private final int status;

  public ApiException(int status, String mensaje) {
    super(mensaje);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
