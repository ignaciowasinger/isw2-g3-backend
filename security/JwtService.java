package com.example.backendprojt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Servicio de emisión y validación de tokens JWT (RF-01, RNF-05).
 *
 * <p>Firma con HMAC-SHA usando la clave configurada en {@link JwtProperties}; jjwt elige
 * HS256 cuando la clave tiene al menos 32 bytes. Incluye el identificador, nombre y rol
 * del usuario como claims, además del sujeto ({@code username}) y la expiración.</p>
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long vigenciaMs;

    /**
     * Construye el servicio a partir de las propiedades validadas.
     *
     * @param propiedades configuración de secreto y vigencia.
     */
    public JwtService(JwtProperties propiedades) {
        this.clave = Keys.hmacShaKeyFor(propiedades.secret().getBytes(StandardCharsets.UTF_8));
        this.vigenciaMs = propiedades.expirationMinutes() * 60_000L;
    }

    /**
     * Genera un token firmado para un usuario autenticado.
     *
     * @param usuario principal autenticado.
     * @return token JWT compacto.
     */
    public String generarToken(UsuarioPrincipal usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombreCompleto())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + vigenciaMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Verifica la firma y vigencia del token y devuelve sus claims.
     *
     * @param token token JWT sin el prefijo {@code Bearer }.
     * @return claims del token.
     * @throws io.jsonwebtoken.JwtException si el token es inválido, está expirado o la
     *                                      firma no corresponde.
     */
    public Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
