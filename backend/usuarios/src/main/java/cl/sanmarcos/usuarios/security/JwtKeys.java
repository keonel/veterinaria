package cl.sanmarcos.usuarios.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/**
 * Par de claves RSA con que se firman los JWT (algoritmo RS256).
 *
 * <p>La clave PRIVADA firma y solo existe en este servicio. La clave PÚBLICA se publica en
 * /api/v1/auth/jwks para que catálogo, pedidos y citas verifiquen la firma sin poder emitir
 * tokens. Las claves se generan la primera vez y se guardan en disco (carpeta ignorada por Git)
 * para que los tokens sigan siendo válidos después de reiniciar el servicio.
 */
@Component
public class JwtKeys {
  private final RSAKey rsa;

  public JwtKeys(@Value("${app.keys-directory}") String directorio) throws Exception {
    Path carpeta = Path.of(directorio);
    Files.createDirectories(carpeta);
    Path archivoPrivado = carpeta.resolve("private.pk8");
    Path archivoPublico = carpeta.resolve("public.der");

    if (!Files.exists(archivoPrivado) || !Files.exists(archivoPublico)) {
      KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
      generador.initialize(2048);
      KeyPair par = generador.generateKeyPair();
      Files.write(archivoPrivado, par.getPrivate().getEncoded());
      Files.write(archivoPublico, par.getPublic().getEncoded());
    }

    KeyFactory fabrica = KeyFactory.getInstance("RSA");
    RSAPrivateKey privada =
        (RSAPrivateKey)
            fabrica.generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(archivoPrivado)));
    RSAPublicKey publica =
        (RSAPublicKey)
            fabrica.generatePublic(new X509EncodedKeySpec(Files.readAllBytes(archivoPublico)));
    // kid: identificador de la clave, derivado de la clave pública
    String kid =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(MessageDigest.getInstance("SHA-256").digest(publica.getEncoded()));
    this.rsa = new RSAKey.Builder(publica).privateKey(privada).keyID(kid).build();
  }

  public String kid() {
    return rsa.getKeyID();
  }

  /** Codificador que firma los tokens con la clave privada. */
  public JwtEncoder encoder() {
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsa)));
  }

  /** Decodificador local: usuarios valida sus propios tokens con la clave pública. */
  public JwtDecoder decoder(String issuer) throws Exception {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsa.toRSAPublicKey()).build();
    decoder.setJwtValidator(cl.sanmarcos.common.security.SecuritySupport.validador(issuer));
    return decoder;
  }

  /** Solo la parte pública, en formato JWKS. */
  public Map<String, Object> publicJwks() {
    return new JWKSet(rsa.toPublicJWK()).toJSONObject();
  }
}
