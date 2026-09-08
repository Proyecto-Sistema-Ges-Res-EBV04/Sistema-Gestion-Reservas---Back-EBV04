package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.AutenticacionService;
import com.reservas.reservas_backend.aplicacion.JwtService;
import com.reservas.reservas_backend.aplicacion.TokenBlacklistService;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.integracion.LoginRequest;
import com.reservas.reservas_backend.integracion.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public AutenticacionController(AutenticacionService autenticacionService,
                                    JwtService jwtService,
                                    TokenBlacklistService tokenBlacklistService) {
        this.autenticacionService = autenticacionService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        InicioSesion inicioSesion = autenticacionService.iniciarSesion(request.getCorreo(), request.getContrasena());

        String nombreTipoUsuario = inicioSesion.getTipoUsuario() != null
                ? inicioSesion.getTipoUsuario().getNombre()
                : "SIN_ROL";

        String token = jwtService.generarToken(inicioSesion.getCorreoElectronico(), nombreTipoUsuario);

        LoginResponse response = new LoginResponse(
                inicioSesion.getId(),
                inicioSesion.getCorreoElectronico(),
                nombreTipoUsuario,
                token
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        tokenBlacklistService.invalidarToken(token);
        return ResponseEntity.noContent().build();
    }
}