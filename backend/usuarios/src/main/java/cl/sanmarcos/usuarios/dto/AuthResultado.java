package cl.sanmarcos.usuarios.dto;

/** Resultado interno de AuthService: la sesión para el JSON y el refresh token para la cookie. */
public record AuthResultado(SesionResponse sesion, String refreshToken) {}
