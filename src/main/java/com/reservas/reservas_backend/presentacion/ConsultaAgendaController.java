package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.ConsultaAgendaService;
import com.reservas.reservas_backend.integracion.AgendaConfiguradaResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// Consultar agenda configurada.
// GET /api/servicios/{idServicio}/sedes/{idSede}/agenda?desde=2026-12-01&hasta=2026-12-31
// Para consultar una sola fecha: desde = hasta = esa fecha.
@RestController
@RequestMapping("/api/servicios/{idServicio}/sedes/{idSede}/agenda")
public class ConsultaAgendaController {

    private final ConsultaAgendaService consultaAgendaService;

    public ConsultaAgendaController(ConsultaAgendaService consultaAgendaService) {
        this.consultaAgendaService = consultaAgendaService;
    }

    @GetMapping
    public ResponseEntity<AgendaConfiguradaResponse> consultar(
            @PathVariable Integer idServicio,
            @PathVariable Integer idSede,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(consultaAgendaService.consultar(idServicio, idSede, desde, hasta));
    }
}