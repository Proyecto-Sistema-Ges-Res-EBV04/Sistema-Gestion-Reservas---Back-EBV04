package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.CitaService;
import com.reservas.reservas_backend.integracion.CitaRecibidaDetalleResponse;
import com.reservas.reservas_backend.integracion.CitaResponse;
import com.reservas.reservas_backend.integracion.EstadoConsultaCita;
import com.reservas.reservas_backend.integracion.MisCitasResponse;
import com.reservas.reservas_backend.integracion.CitaDetalleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    @Autowired
    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // HU-23: Consultar mis citas, con filtros opcionales combinables (RN5/RN6).
    // Ej: GET /api/citas/mis-citas?estado=PROXIMA&idEmpresa=1&fechaDesde=2026-10-01&fechaHasta=2026-10-31
    @GetMapping("/mis-citas")
    public ResponseEntity<MisCitasResponse> consultarMisCitas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Integer idEmpresa,
            @RequestParam(required = false) Integer idServicio,
            @RequestParam(required = false) EstadoConsultaCita estado) {
        return ResponseEntity.ok(citaService.consultarMisCitas(fechaDesde, fechaHasta, idEmpresa, idServicio, estado));
    }

    @GetMapping("/recibidas")
    public ResponseEntity<MisCitasResponse> consultarCitasRecibidas(
            @RequestParam(required = false) Integer idSede,
            @RequestParam(required = false) Integer idServicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) EstadoConsultaCita estado) {
        return ResponseEntity.ok(citaService.consultarCitasRecibidas(idSede, idServicio, fechaDesde, fechaHasta, estado));
    }

    @GetMapping("/recibidas/{id}")
    public ResponseEntity<CitaRecibidaDetalleResponse> consultarDetalleCitaRecibida(@PathVariable Integer id) {
        return ResponseEntity.ok(citaService.consultarDetalleCitaRecibida(id));
    }

    // HU-24: Detalle de una cita propia. Ej: GET /api/citas/15
    @GetMapping("/{id}")
    public ResponseEntity<CitaDetalleResponse> consultarDetalleCita(@PathVariable Integer id) {
        return ResponseEntity.ok(citaService.consultarDetalleCita(id));
    }

    // HU-25: Cancelar cita. Ej: PATCH /api/citas/15/cancelar
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelarCita(@PathVariable Integer id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }
}
