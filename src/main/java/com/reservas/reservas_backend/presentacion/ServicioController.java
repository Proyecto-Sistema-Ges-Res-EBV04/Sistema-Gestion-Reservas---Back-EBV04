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

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> listarServicios() {
        List<ServicioResponse> respuesta = new ArrayList<>();
        for (Servicio servicio : servicioService.listarServicios()) {
            respuesta.add(aRespuesta(servicio));
        }
        return ResponseEntity.ok(respuesta);
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

    // HU-16 (RN1): define las franjas de atención del servicio en la sede.
    @PostMapping("/{idServicio}/sedes/{idSede}/franjas")
    public ResponseEntity<List<FranjaResponse>> definirFranjas(@PathVariable Integer idServicio,
                                                                @PathVariable Integer idSede,
                                                                @Valid @RequestBody DefinirFranjasRequest request) {
        List<FranjaResponse> respuesta = capacidadService.definirFranjas(idServicio, idSede, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/{idServicio}/sedes/{idSede}/franjas")
    public ResponseEntity<List<FranjaResponse>> consultarFranjas(@PathVariable Integer idServicio,
                                                                  @PathVariable Integer idSede) {
        return ResponseEntity.ok(capacidadService.consultarFranjas(idServicio, idSede));
    }

    // HU-16 (RN13): calcula el resumen de capacidad sin guardar, para revisarlo antes de confirmar.
    @PostMapping("/{idServicio}/sedes/{idSede}/capacidad/resumen")
    public ResponseEntity<CapacidadResumenResponse> previsualizarCapacidad(@PathVariable Integer idServicio,
                                                                           @PathVariable Integer idSede,
                                                                           @Valid @RequestBody ConfigurarCapacidadRequest request) {
        return ResponseEntity.ok(capacidadService.previsualizarCapacidad(idServicio, idSede, request));
    }

    // HU-16: confirma la configuración de capacidad (reemplaza la anterior del servicio en la sede).
    @PostMapping("/{idServicio}/sedes/{idSede}/capacidad")
    public ResponseEntity<CapacidadResumenResponse> configurarCapacidad(@PathVariable Integer idServicio,
                                                                        @PathVariable Integer idSede,
                                                                        @Valid @RequestBody ConfigurarCapacidadRequest request) {
        CapacidadResumenResponse respuesta = capacidadService.configurarCapacidad(idServicio, idSede, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/{idServicio}/sedes/{idSede}/capacidad")
    public ResponseEntity<CapacidadResumenResponse> consultarCapacidad(@PathVariable Integer idServicio,
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
            empleados.add(EmpleadoResponse.desde(empleado, servicioService.obtenerEspaciosDeEmpleado(empleado)));
        }

        List<RecursoResponse> recursos = new ArrayList<>();
        for (Recurso recurso : servicioService.obtenerRecursos(servicio)) {
            recursos.add(RecursoResponse.desde(recurso));
        }

        return ServicioResponse.desde(servicio, sedes, empleados, recursos);
    }
}
