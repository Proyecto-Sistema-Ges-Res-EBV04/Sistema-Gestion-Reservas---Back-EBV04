package com.reservas.reservas_backend.aplicacion;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Guarda los tokens que fueron invalidados por logout
    private final Set<String> tokensInvalidados = ConcurrentHashMap.newKeySet();

    public void invalidarToken(String token) {
        tokensInvalidados.add(token);
    }

    public boolean estaInvalidado(String token) {
        return tokensInvalidados.contains(token);
    }
}
