package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.AgendarCitaService;
import com.reservas.reservas_backend.aplicacion.UsuarioContextoService;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.integracion.AgendarCitaRequest;
import com.reservas.reservas_backend.integracion.CitaAgendadaResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas")
public class AgendarCitaController {

    private final AgendarCitaService agendarCitaService;
    private final UsuarioContextoService usuarioContextoService;

    @Autowired
    public AgendarCitaController(AgendarCitaService agendarCitaService,
                                 UsuarioContextoService usuarioContextoService) {
        this.agendarCitaService = agendarCitaService;
        this.usuarioContextoService = usuarioContextoService;
    }

    // RN8: revisar los datos antes de confirmar. No guarda nada.
    @PostMapping("/previsualizar")
    public ResponseEntity<CitaAgendadaResponse> previsualizar(@Valid @RequestBody AgendarCitaRequest request) {
        usuarioContextoService.obtenerUsuarioAutenticado();
        return ResponseEntity.ok(agendarCitaService.previsualizar(request));
    }

    // RN1, RN10: solo un Usuario autenticado; la cita se guarda al confirmar.
    @PostMapping
    public ResponseEntity<CitaAgendadaResponse> agendar(@Valid @RequestBody AgendarCitaRequest request) {
        Usuario usuario = usuarioContextoService.obtenerUsuarioAutenticado();
        return ResponseEntity.status(HttpStatus.CREATED).body(agendarCitaService.agendar(usuario, request));
    }
}