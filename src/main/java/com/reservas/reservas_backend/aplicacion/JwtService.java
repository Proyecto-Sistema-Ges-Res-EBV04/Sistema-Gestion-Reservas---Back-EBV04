package com.reservas.reservas_backend.aplicacion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // Clave secreta para firmar los tokens. Se lee de jwt.secret en application.properties,
    // que en Render toma el valor de la variable de entorno JWT_SECRET.
    private final SecretKey clave;

    private static final long DURACION_TOKEN_MS = 1000 * 60 * 60; // 1 hora

    public JwtService(@Value("${jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes());
    }

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
