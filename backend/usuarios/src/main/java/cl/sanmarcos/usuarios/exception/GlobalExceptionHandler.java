package cl.sanmarcos.usuarios.exception;

import cl.sanmarcos.common.exception.ApiErrors;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Aplica a todos los controladores los manejadores de error heredados de ApiErrors. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ApiErrors {}
