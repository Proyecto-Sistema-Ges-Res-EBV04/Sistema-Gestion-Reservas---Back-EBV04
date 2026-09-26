package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.aplicacion.EmpresaService;
import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.integracion.LoginResponse;
import com.reservas.reservas_backend.integracion.RegistrarEmpresaRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// HU-02: Registrar cuenta de Empresa (registro por pasos).
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    @Autowired
    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> registrarEmpresa(@Valid @RequestBody RegistrarEmpresaRequest request) {
        LoginResponse respuesta = empresaService.registrarEmpresa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // RN3/RN4/RN5: solo finaliza si ya hay sede + servicio asociado a una sede + capacidad configurada.
    @PostMapping("/finalizar-registro")
    public ResponseEntity<Map<String, Object>> finalizarRegistro() {
        Empresa empresa = empresaService.finalizarRegistro();
        return ResponseEntity.ok(Map.of(
                "id", empresa.getId(),
                "registroCompleto", empresa.getRegistroCompleto(),
                "mensaje", "El registro de la Empresa quedó completo. Ya puedes continuar operando."
        ));
    }
}
