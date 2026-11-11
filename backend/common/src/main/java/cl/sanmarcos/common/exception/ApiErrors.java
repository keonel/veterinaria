package cl.sanmarcos.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejadores de error compartidos. Cada microservicio crea un GlobalExceptionHandler
 * con @RestControllerAdvice que hereda de esta clase, así todas las APIs responden
 * los errores con el mismo formato: { "status": 409, "mensaje": "...", "campos": {...} }
 */
public class ApiErrors {
  private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

  public static Map<String, Object> cuerpo(int status, String mensaje) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status);
    body.put("mensaje", mensaje);
    return body;
  }

  private static ResponseEntity<Map<String, Object>> respuesta(int status, String mensaje) {
    return ResponseEntity.status(status).body(cuerpo(status, mensaje));
  }

  /** Errores de negocio lanzados por los servicios (404, 409, etc.). */
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> negocio(ApiException e) {
    return respuesta(e.getStatus(), e.getMessage());
  }

  /** Falló @Valid: se informa qué campo tiene qué problema. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
    Map<String, String> campos = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
    Map<String, Object> body = cuerpo(400, "Hay datos inválidos en la solicitud.");
    body.put("campos", campos);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) {
    return respuesta(400, "El cuerpo de la solicitud no es un JSON válido.");
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<Map<String, Object>> faltaCabecera(MissingRequestHeaderException e) {
    return respuesta(400, "Falta la cabecera obligatoria " + e.getHeaderName() + ".");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException e) {
    return respuesta(400, "El parámetro " + e.getName() + " no tiene un formato válido.");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException e) {
    return respuesta(409, "El registro entra en conflicto con datos existentes.");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> sinPermiso(AccessDeniedException e) {
    return respuesta(403, "No tienes permisos para esta acción.");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> rutaInexistente(NoResourceFoundException e) {
    return respuesta(404, "La ruta solicitada no existe.");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> metodo(HttpRequestMethodNotSupportedException e) {
    return respuesta(405, "Método HTTP no permitido en esta ruta.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> inesperado(Exception e) {
    log.error("Error no controlado", e);
    return respuesta(500, "Ocurrió un error inesperado en el servidor.");
  }
}
