package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.SedeService;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.integracion.RegistrarSedeRequest;
import com.reservas.reservas_backend.integracion.SedeResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService sedeService;

    @Autowired
    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @PostMapping
    public ResponseEntity<SedeResponse> registrarSede(@Valid @RequestBody RegistrarSedeRequest request) {
        Sede sede = sedeService.registrarSede(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SedeResponse.desde(sede));
    }

    // ?activas=true -> solo sedes activas (para el paso de "seleccionar sedes" al registrar un servicio)
    @GetMapping
    public ResponseEntity<List<SedeResponse>> listarSedes(
            @RequestParam(name = "activas", defaultValue = "false") boolean soloActivas) {
        List<Sede> sedes = sedeService.listarSedes(soloActivas);
        List<SedeResponse> respuesta = new ArrayList<>();
        for (Sede sede : sedes) {
            respuesta.add(SedeResponse.desde(sede));
        }
        return ResponseEntity.ok(respuesta);
    }
}
