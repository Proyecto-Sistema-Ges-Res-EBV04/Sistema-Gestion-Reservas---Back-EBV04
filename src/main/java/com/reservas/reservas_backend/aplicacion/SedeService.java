package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.integracion.RegistrarSedeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public SedeService(SedeRepository sedeRepository, EmpresaContextoService empresaContexto) {
        this.sedeRepository = sedeRepository;
        this.empresaContexto = empresaContexto;
    }

    // RN1/RN2/RN3/RN6
    public Sede registrarSede(RegistrarSedeRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();

        Sede sede = new Sede();
        sede.setEmpresa(empresa);
        sede.setNombre(request.getNombre());
        sede.setDireccion(request.getDireccion());
        sede.setMunicipio(request.getMunicipio());
        sede.setTelefono(request.getTelefono());
        sede.setActiva(true);
        sede.setFechaCreacion(LocalDateTime.now());

        return sedeRepository.save(sede);
    }

    // Soporta el escenario "Sede no disponible": listar solo activas para seleccionar en HU-11.
    public List<Sede> listarSedes(boolean soloActivas) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        return soloActivas
                ? sedeRepository.findByEmpresa_IdAndActivaTrue(empresa.getId())
                : sedeRepository.findByEmpresa_Id(empresa.getId());
    }
}
