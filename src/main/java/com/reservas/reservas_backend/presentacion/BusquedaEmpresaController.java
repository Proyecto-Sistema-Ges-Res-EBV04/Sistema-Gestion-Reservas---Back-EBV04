package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.BusquedaEmpresaService;
import com.reservas.reservas_backend.integracion.BusquedaEmpresasResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// HU-18: Buscar empresas por nombre y/o ubicación (municipio).
// Ej: GET /api/empresas/buscar?nombre=clinica&municipio=medellin
@RestController
@RequestMapping("/api/empresas")
public class BusquedaEmpresaController {

    private final BusquedaEmpresaService busquedaEmpresaService;

    @Autowired
    public BusquedaEmpresaController(BusquedaEmpresaService busquedaEmpresaService) {
        this.busquedaEmpresaService = busquedaEmpresaService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<BusquedaEmpresasResponse> buscar(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "municipio", required = false) String municipio) {

        return ResponseEntity.ok(busquedaEmpresaService.buscar(nombre, municipio));
    }
}