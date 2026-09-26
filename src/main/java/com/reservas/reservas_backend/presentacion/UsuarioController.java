package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.UsuarioService;
import com.reservas.reservas_backend.integracion.LoginResponse;
import com.reservas.reservas_backend.integracion.RegistrarUsuarioRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// HU-01: Registrar cuenta de Usuario.
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> registrarUsuario(@Valid @RequestBody RegistrarUsuarioRequest request) {
        LoginResponse respuesta = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
