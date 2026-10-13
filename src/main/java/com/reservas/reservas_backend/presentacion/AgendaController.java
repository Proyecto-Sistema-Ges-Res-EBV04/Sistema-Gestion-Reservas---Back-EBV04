package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.AgendaService;
import com.reservas.reservas_backend.dominio.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Agenda")
public class AgendaController {
    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    /** 1. Ver agenda negocio empleado */
    @PostMapping("/horarios")
    public ResponseEntity<List<HorarioDisponibleDto>> verAgendaNegocioEmpleado(
            @Valid @RequestBody VerAgendaRequest req) {
        return ResponseEntity.ok(agendaService.verAgendaNegocioEmpleado(
                req.idSedeServicio(), req.fechaDesde(), req.fechaHasta()));
    }

    /** 2. Configurar agenda servicio empleado */
    @PostMapping("/configurar")
    public ResponseEntity<Boolean> configurarAgendaServicioEmpleado(@Valid @RequestBody ConfigurarAgendaRequest req) {
        return ResponseEntity.ok(agendaService.configurarAgendaServicioEmpleado(req));
    }

    /** 3. Configurar día festivo */
    @PostMapping("/dia-festivo")
    public ResponseEntity<Boolean> configurarDiaFestivo(@Valid @RequestBody ConfigurarDiaFestivoRequest req) {
        return ResponseEntity.ok(agendaService.configurarDiaFestivo(req));
    }

    /** 4. Modificar cita usuario (crear o pasar de estado 3 a 2) */
    @PostMapping("/cita")
    public ResponseEntity<ResultadoCitaDto> modificarCitaUsuario(@RequestBody ModificarCitaRequest req) {
        ResultadoCitaDto r = agendaService.modificarCitaUsuario(req);
        return r.exito() ? ResponseEntity.ok(r) : ResponseEntity.badRequest().body(r);
    }
}
