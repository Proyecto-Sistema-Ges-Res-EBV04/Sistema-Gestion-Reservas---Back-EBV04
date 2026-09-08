package com.reservas.reservas_backend.aplicacion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Clave secreta para firmar los tokens (en un proyecto real esto va en variables de entorno, no en el código)
    private final SecretKey clave = Keys.hmacShaKeyFor(
            "clave-secreta-super-larga-para-firmar-los-tokens-jwt-1234567890".getBytes());

    private static final long DURACION_TOKEN_MS = 1000 * 60 * 60; // 1 hora

    public String generarToken(String correo, String tipoCuenta) {
        return Jwts.builder()
                .subject(correo)
                .claim("tipoCuenta", tipoCuenta)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + DURACION_TOKEN_MS))
                .signWith(clave)
                .compact();
    }

    public String extraerCorreo(String token) {
        return parsearClaims(token).getSubject();
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = parsearClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
