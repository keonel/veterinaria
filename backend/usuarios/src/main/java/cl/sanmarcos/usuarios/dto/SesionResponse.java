package cl.sanmarcos.usuarios.dto;

/**
 * Respuesta de login y refresh. El access token viaja en el JSON (React lo guarda en memoria);
 * el refresh token NO aparece aquí: viaja en una cookie HttpOnly que JavaScript no puede leer.
 */
public record SesionResponse(
    String accessToken, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {}
