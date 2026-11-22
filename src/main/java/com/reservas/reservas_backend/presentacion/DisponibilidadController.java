package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.DisponibilidadService;
import com.reservas.reservas_backend.integracion.DisponibilidadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// HU-21: consultar disponibilidad. GET de solo lectura: no crea citas (RN7).
@RestController
@RequestMapping("/api/servicios")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @Autowired
    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    // Ej.: GET /api/servicios/3/sedes/2/disponibilidad?fecha=2026-11-30
    @GetMapping("/{idServicio}/sedes/{idSede}/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> consultar(
            @PathVariable Integer idServicio,
            @PathVariable Integer idSede,
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(disponibilidadService.consultar(idServicio, idSede, fecha));
    }
}
