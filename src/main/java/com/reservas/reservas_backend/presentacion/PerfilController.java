package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.JwtService;
import com.reservas.reservas_backend.aplicacion.PerfilService;
import com.reservas.reservas_backend.integracion.PerfilResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;
    private final JwtService jwtService;

    public PerfilController(PerfilService perfilService, JwtService jwtService) {
        this.perfilService = perfilService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<PerfilResponse> consultarPerfil(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token de autorización requerido");
        }

        String token = authHeader.substring(7);

        if (!jwtService.tokenValido(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Token inválido o vencido");
        }

        String correo = jwtService.extraerCorreo(token);

        return ResponseEntity.ok(perfilService.consultarPerfil(correo));
    }
}
