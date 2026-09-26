package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.CapacidadService;
import com.reservas.reservas_backend.aplicacion.ServicioService;
import com.reservas.reservas_backend.dominio.Empleado;
import com.reservas.reservas_backend.dominio.Recurso;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.integracion.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;
    private final CapacidadService capacidadService;

    @Autowired
    public ServicioController(ServicioService servicioService, CapacidadService capacidadService) {
        this.servicioService = servicioService;
        this.capacidadService = capacidadService;
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> registrarServicio(@Valid @RequestBody RegistrarServicioRequest request) {
        Servicio servicio = servicioService.registrarServicio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(servicio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> consultarServicio(@PathVariable Integer id) {
        Servicio servicio = servicioService.consultarServicio(id);
        return ResponseEntity.ok(aRespuesta(servicio));
    }

    @PostMapping("/{idServicio}/sedes/{idSede}/empleados")
    public ResponseEntity<EmpleadoResponse> registrarEmpleado(@PathVariable Integer idServicio,
                                                               @PathVariable Integer idSede,
                                                               @Valid @RequestBody RegistrarEmpleadoRequest request) {
        Empleado empleado = servicioService.registrarEmpleado(idServicio, idSede, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmpleadoResponse.desde(empleado));
    }

    @PostMapping("/{idServicio}/sedes/{idSede}/recursos")
    public ResponseEntity<RecursoResponse> registrarRecurso(@PathVariable Integer idServicio,
                                                              @PathVariable Integer idSede,
                                                              @Valid @RequestBody RegistrarRecursoRequest request) {
        Recurso recurso = servicioService.registrarRecurso(idServicio, idSede, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecursoResponse.desde(recurso));
    }

    // HU-16: Configurar capacidad de atención (crea o reconfigura, RN11).
    @PostMapping("/{idServicio}/sedes/{idSede}/capacidad")
    public ResponseEntity<List<CapacidadResponse>> configurarCapacidad(@PathVariable Integer idServicio,
                                                                        @PathVariable Integer idSede,
                                                                        @Valid @RequestBody ConfigurarCapacidadRequest request) {
        List<CapacidadResponse> respuesta = capacidadService.configurarCapacidad(idServicio, idSede, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/{idServicio}/sedes/{idSede}/capacidad")
    public ResponseEntity<List<CapacidadResponse>> consultarCapacidad(@PathVariable Integer idServicio,
                                                                       @PathVariable Integer idSede) {
        return ResponseEntity.ok(capacidadService.consultarCapacidad(idServicio, idSede));
    }

    private ServicioResponse aRespuesta(Servicio servicio) {
        List<SedeResponse> sedes = new ArrayList<>();
        for (Sede sede : servicioService.obtenerSedesAsociadas(servicio)) {
            sedes.add(SedeResponse.desde(sede));
        }

        List<EmpleadoResponse> empleados = new ArrayList<>();
        for (Empleado empleado : servicioService.obtenerEmpleados(servicio)) {
            empleados.add(EmpleadoResponse.desde(empleado));
        }

        List<RecursoResponse> recursos = new ArrayList<>();
        for (Recurso recurso : servicioService.obtenerRecursos(servicio)) {
            recursos.add(RecursoResponse.desde(recurso));
        }

        return ServicioResponse.desde(servicio, sedes, empleados, recursos);
    }
}
