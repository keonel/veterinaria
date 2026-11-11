package cl.sanmarcos.usuarios.service;

import cl.sanmarcos.common.exception.ApiException;
import java.util.Locale;

/** Reglas de negocio de las cuentas, compartidas por el registro y el CRUD de usuarios. */
public final class ReglasUsuario {
  private ReglasUsuario() {}

  /**
   * Valida el dígito verificador (módulo 11) y devuelve el RUN en formato único 12345678-9,
   * así "12.345.678-9" y "123456789" se reconocen como la misma persona.
   */
  public static String normalizarRun(String run) {
    String limpio = run == null ? "" : run.replaceAll("[^0-9kK]", "").toUpperCase(Locale.ROOT);
    if (limpio.length() < 8 || limpio.length() > 9) {
      throw new ApiException(400, "RUN inválido (ej: 12345678-9).");
    }
    String cuerpo = limpio.substring(0, limpio.length() - 1);
    char dv = limpio.charAt(limpio.length() - 1);
    if (!cuerpo.chars().allMatch(Character::isDigit)) {
      throw new ApiException(400, "RUN inválido (ej: 12345678-9).");
    }
    int suma = 0;
    int multiplo = 2;
    for (int i = cuerpo.length() - 1; i >= 0; i--) {
      suma += multiplo * (cuerpo.charAt(i) - '0');
      multiplo = multiplo < 7 ? multiplo + 1 : 2;
    }
    int esperado = 11 - (suma % 11);
    char dvEsperado = esperado == 11 ? '0' : esperado == 10 ? 'K' : (char) ('0' + esperado);
    if (dv != dvEsperado) {
      throw new ApiException(400, "RUN inválido: el dígito verificador no corresponde.");
    }
    return cuerpo + "-" + dv;
  }

  /** Misma política que muestra el formulario: 5 a 10 caracteres, una mayúscula y un número. */
  public static void validarContrasena(String clave) {
    boolean valida =
        clave != null
            && clave.length() >= 5
            && clave.length() <= 10
            && clave.chars().anyMatch(Character::isUpperCase)
            && clave.chars().anyMatch(Character::isDigit);
    if (!valida) {
      throw new ApiException(
          400,
          "La contraseña debe tener entre 5 y 10 caracteres, una mayúscula y al menos un número.");
    }
  }

  public static String normalizarCorreo(String correo) {
    return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
  }

  public static String limpiar(String texto) {
    return texto == null ? null : texto.trim();
  }
}
