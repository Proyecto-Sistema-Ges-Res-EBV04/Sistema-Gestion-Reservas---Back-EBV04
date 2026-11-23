package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.integracion.ServicioCatalogoResponse;
import com.reservas.reservas_backend.integracion.SedeCatalogoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CatalogoServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final CapacidadAtencionRepository capacidadAtencionRepository;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public CatalogoServicioService(ServicioRepository servicioRepository,
                                   ServicioSedeRepository servicioSedeRepository,
                                   CapacidadAtencionRepository capacidadAtencionRepository,
                                   EmpresaContextoService empresaContexto) {
        this.servicioRepository = servicioRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.capacidadAtencionRepository = capacidadAtencionRepository;
        this.empresaContexto = empresaContexto;
    }

    @Transactional(readOnly = true)
    public List<ServicioCatalogoResponse> listarCatalogo() {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        List<Servicio> servicios = servicioRepository.findByEmpresa_Id(empresa.getId());
        if (servicios.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>();
        for (Servicio s : servicios) {
            ids.add(s.getId());
        }

        // Sedes de todos los servicios (1 consulta)
        Map<Integer, List<Sede>> sedesPorServicio = new HashMap<>();
        for (ServicioSede vinculo : servicioSedeRepository.findByServicio_IdIn(ids)) {
            sedesPorServicio.computeIfAbsent(vinculo.getServicio().getId(), k -> new ArrayList<>())
                    .add(vinculo.getSede());
        }

        // Franjas de todos los servicios (1 consulta)
        Map<Integer, Map<Integer, List<CapacidadAtencion>>> franjas = new HashMap<>();
        for (CapacidadAtencion franja : capacidadAtencionRepository.findByServicio_IdIn(ids)) {
            franjas.computeIfAbsent(franja.getServicio().getId(), k -> new HashMap<>())
                    .computeIfAbsent(franja.getSede().getId(), k -> new ArrayList<>())
                    .add(franja);
        }

        List<ServicioCatalogoResponse> respuesta = new ArrayList<>();
        for (Servicio servicio : servicios) {
            Map<Integer, List<CapacidadAtencion>> franjasDelServicio =
                    franjas.getOrDefault(servicio.getId(), Map.of());
            List<SedeCatalogoResponse> sedes = new ArrayList<>();
            for (Sede sede : sedesPorServicio.getOrDefault(servicio.getId(), List.of())) {
                sedes.add(armarSede(sede, franjasDelServicio.getOrDefault(sede.getId(), List.of())));
            }
            respuesta.add(armarServicio(servicio, sedes));
        }
        return respuesta;
    }

    // Misma regla de CapacidadService (RN9): al menos una franja y todas con capacidad >= 1
    private SedeCatalogoResponse armarSede(Sede sede, List<CapacidadAtencion> franjas) {
        List<String> pendientes = new ArrayList<>();
        if (franjas.isEmpty()) {
            pendientes.add("Sin franjas de atención definidas");
        } else {
            List<String> sinCapacidad = new ArrayList<>();
            for (CapacidadAtencion franja : franjas) {
                Integer capacidad = franja.getCapacidad();
                if (capacidad == null || capacidad < 1) {
                    sinCapacidad.add(franja.getDiaSemana() + " " + franja.getHoraInicio() + "-" + franja.getHoraFin());
                }
            }
            if (!sinCapacidad.isEmpty()) {
                pendientes.add("Franjas sin capacidad (falta un empleado con espacio): "
                        + String.join(", ", sinCapacidad));
            }
        }
        boolean activa = Boolean.TRUE.equals(sede.getActiva());
        return new SedeCatalogoResponse(sede.getId(), sede.getNombre(), sede.getDireccion(),
                activa, pendientes.isEmpty(), pendientes);
    }

    private ServicioCatalogoResponse armarServicio(Servicio servicio, List<SedeCatalogoResponse> sedes) {
        int activas = 0;
        int disponibles = 0;
        for (SedeCatalogoResponse sede : sedes) {
            if (sede.isActiva()) {
                activas++;
                if (sede.isConfigurada()) {
                    disponibles++;
                }
            }
        }
        String estado;
        if (disponibles == 0) {
            estado = ServicioCatalogoResponse.PENDIENTE;
        } else if (disponibles < activas) {
            estado = ServicioCatalogoResponse.PARCIAL;
        } else {
            estado = ServicioCatalogoResponse.DISPONIBLE;
        }
        return new ServicioCatalogoResponse(servicio.getId(), servicio.getNombre(), servicio.getDescripcion(),
                servicio.getDuracion(), servicio.getUnidadDuracion(), estado, disponibles > 0, sedes);
    }
}