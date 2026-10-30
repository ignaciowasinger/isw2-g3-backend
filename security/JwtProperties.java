package com.example.backendprojt.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración del JWT ({@code app.jwt.*}).
 *
 * <p>El secreto se valida al construir el record para garantizar que tenga al menos 32
 * bytes, mínimo requerido por HMAC-SHA256 (RNF-05). La vigencia se expresa en minutos y
 * por defecto es de 480 (8 horas).</p>
 *
 * @param secret            clave HMAC en texto plano; en producción debe provenir de una
 *                          variable de entorno o gestor de secretos.
 * @param expirationMinutes vigencia del token en minutos.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {

    /**
     * Valida la configuración recibida.
     *
     * @throws IllegalArgumentException si el secreto es nulo o menor a 32 bytes, o si la
     *                                  vigencia no es positiva.
     */
    public JwtProperties {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret debe tener al menos 32 bytes");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("app.jwt.expiration-minutes debe ser mayor que cero");
        }
    }
}
