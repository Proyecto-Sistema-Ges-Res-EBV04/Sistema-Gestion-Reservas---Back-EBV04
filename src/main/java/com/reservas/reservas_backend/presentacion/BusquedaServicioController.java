package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.BusquedaServicioService;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/servicios")
public class BusquedaServicioController {

    private final BusquedaServicioService busquedaServicioService;

    @Autowired
    public BusquedaServicioController(BusquedaServicioService busquedaServicioService) {
        this.busquedaServicioService = busquedaServicioService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<BusquedaServiciosResponse> buscar(
            @RequestParam(name = "palabra", required = false) String palabra,
            @RequestParam(name = "empresaId", required = false) Integer empresaId,
            @RequestParam(name = "sedeId", required = false) Integer sedeId,
            @RequestParam(name = "municipio", required = false) String municipio) {

        return ResponseEntity.ok(
                busquedaServicioService.buscar(palabra, empresaId, sedeId, municipio));
    }
}