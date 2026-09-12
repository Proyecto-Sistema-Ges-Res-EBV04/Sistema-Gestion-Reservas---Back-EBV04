package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.integracion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RecursoRepository recursoRepository;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public ServicioService(ServicioRepository servicioRepository, SedeRepository sedeRepository,
                            ServicioSedeRepository servicioSedeRepository, EmpleadoRepository empleadoRepository,
                            RecursoRepository recursoRepository, EmpresaContextoService empresaContexto) {
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.empleadoRepository = empleadoRepository;
        this.recursoRepository = recursoRepository;
        this.empresaContexto = empresaContexto;
    }

    // RN1/RN2/RN3/RN4/RN5/RN6
    // @Transactional: el servicio y sus vínculos con las sedes (ServicioSede) se
    // guardan en varios INSERT separados; si uno falla a mitad de camino, Spring
    // deshace TODOS los cambios de este método (no queda un servicio a medias).
    @Transactional
    public Servicio registrarServicio(RegistrarServicioRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();

        List<Sede> sedes = new ArrayList<>();
        for (Integer idSede : request.getIdsSede()) {
            sedes.add(obtenerSedeActivaDeLaEmpresa(idSede, empresa));
        }

        Servicio servicio = new Servicio();
        servicio.setEmpresa(empresa);
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracion(request.getDuracion());
        servicio.setUnidadDuracion(request.getUnidadDuracion());
        servicio.setFechaCreacion(LocalDateTime.now());
        servicio = servicioRepository.save(servicio);

        for (Sede sede : sedes) {
            ServicioSede vinculo = new ServicioSede();
            vinculo.setServicio(servicio);
            vinculo.setSede(sede);
            servicioSedeRepository.save(vinculo);
        }

        return servicio;
    }

    // RN7/RN8/RN9/RN10
    public Empleado registrarEmpleado(Integer idServicio, Integer idSede, RegistrarEmpleadoRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio, empresa);
        Sede sede = validarAsociacion(servicio, idSede);

        Empleado empleado = new Empleado();
        empleado.setServicio(servicio);
        empleado.setSede(sede);
        empleado.setNombre(request.getNombre());
        empleado.setCargo(request.getCargo());
        empleado.setFechaCreacion(LocalDateTime.now());

        return empleadoRepository.save(empleado);
    }

    // RN7/RN8/RN9/RN10
    public Recurso registrarRecurso(Integer idServicio, Integer idSede, RegistrarRecursoRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio, empresa);
        Sede sede = validarAsociacion(servicio, idSede);

        Recurso recurso = new Recurso();
        recurso.setServicio(servicio);
        recurso.setSede(sede);
        recurso.setIdentificador(request.getIdentificador());
        recurso.setFechaCreacion(LocalDateTime.now());

        return recursoRepository.save(recurso);
    }

    public Servicio consultarServicio(Integer idServicio) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        return obtenerServicioDeLaEmpresa(idServicio, empresa);
    }

    public List<Sede> obtenerSedesAsociadas(Servicio servicio) {
        List<ServicioSede> vinculos = servicioSedeRepository.findByServicio_Id(servicio.getId());
        List<Sede> sedes = new ArrayList<>();
        for (ServicioSede vinculo : vinculos) {
            sedes.add(vinculo.getSede());
        }
        return sedes;
    }

    public List<Empleado> obtenerEmpleados(Servicio servicio) {
        return empleadoRepository.findByServicio_Id(servicio.getId());
    }

    public List<Recurso> obtenerRecursos(Servicio servicio) {
        return recursoRepository.findByServicio_Id(servicio.getId());
    }

    private Sede obtenerSedeActivaDeLaEmpresa(Integer idSede, Empresa empresa) {
        Sede sede = sedeRepository.findById(idSede)
                .filter(s -> s.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new SedeNoEncontradaException(
                        "La sede " + idSede + " no existe o no pertenece a tu Empresa"));

        if (!Boolean.TRUE.equals(sede.getActiva())) {
            throw new SedeInactivaException(
                    "La sede '" + sede.getNombre() + "' no está activa y no puede asociarse a un servicio");
        }
        return sede;
    }

    private Servicio obtenerServicioDeLaEmpresa(Integer idServicio, Empresa empresa) {
        return servicioRepository.findById(idServicio)
                .filter(s -> s.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new ServicioNoEncontradoException(
                        "El servicio " + idServicio + " no existe o no pertenece a tu Empresa"));
    }

    private Sede validarAsociacion(Servicio servicio, Integer idSede) {
        boolean asociado = servicioSedeRepository.existsByServicio_IdAndSede_Id(servicio.getId(), idSede);
        if (!asociado) {
            throw new AsociacionServicioSedeNoEncontradaException(
                    "La sede " + idSede + " no está asociada al servicio " + servicio.getId());
        }
        return sedeRepository.findById(idSede)
                .orElseThrow(() -> new SedeNoEncontradaException("La sede " + idSede + " no existe"));
    }
}
