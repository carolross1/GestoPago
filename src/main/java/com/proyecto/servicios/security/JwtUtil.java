package com.proyecto.servicios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los JWT de sesion del cliente. El secreto y la
 * expiracion vienen de configuracion (nunca hardcodeados en el codigo).
 */
@Component
public class JwtUtil {

    @Value("${seguridad.jwt.secreto}")
    private String secreto;

    @Value("${seguridad.jwt.expiracion-ms}")
    private long expiracionMs;

    public String generarToken(Long clienteId) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(String.valueOf(clienteId))
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(claveSecreta())
                .compact();
    }

    public Long obtenerClienteId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(claveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    public boolean esValido(String token) {
        try {
            Jwts.parser().verifyWith(claveSecreta()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }

    private SecretKey claveSecreta() {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }
}
