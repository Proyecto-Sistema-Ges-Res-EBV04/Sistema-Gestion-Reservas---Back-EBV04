package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.integracion.AsociacionServicioSedeNoEncontradaException;
import com.reservas.reservas_backend.integracion.CapacidadInvalidaException;
import com.reservas.reservas_backend.integracion.CapacidadResponse;
import com.reservas.reservas_backend.integracion.ConfigurarCapacidadRequest;
import com.reservas.reservas_backend.integracion.SedeNoEncontradaException;
import com.reservas.reservas_backend.integracion.ServicioNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// HU-16: Configurar capacidad de atención (RN1-RN11).
@Service
public class CapacidadService {

    private final ServicioRepository servicioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RecursoRepository recursoRepository;
    private final CapacidadAtencionRepository capacidadAtencionRepository;
    private final CapacidadEmpleadoRepository capacidadEmpleadoRepository;
    private final CapacidadRecursoRepository capacidadRecursoRepository;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public CapacidadService(ServicioRepository servicioRepository, SedeRepository sedeRepository,
                             ServicioSedeRepository servicioSedeRepository, EmpleadoRepository empleadoRepository,
                             RecursoRepository recursoRepository, CapacidadAtencionRepository capacidadAtencionRepository,
                             CapacidadEmpleadoRepository capacidadEmpleadoRepository,
                             CapacidadRecursoRepository capacidadRecursoRepository,
                             EmpresaContextoService empresaContexto) {
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.empleadoRepository = empleadoRepository;
        this.recursoRepository = recursoRepository;
        this.capacidadAtencionRepository = capacidadAtencionRepository;
        this.capacidadEmpleadoRepository = capacidadEmpleadoRepository;
        this.capacidadRecursoRepository = capacidadRecursoRepository;
        this.empresaContexto = empresaContexto;
    }

    @Transactional
    public List<CapacidadResponse> configurarCapacidad(Integer idServicio, Integer idSede,
                                                         ConfigurarCapacidadRequest request) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio, empresa);
        Sede sede = obtenerSedeDeLaEmpresa(idSede, empresa);
        validarAsociacion(idServicio, idSede);

        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new CapacidadInvalidaException("La hora de fin debe ser posterior a la hora de inicio");
        }

        List<ConfigurarCapacidadRequest.RecursoAsignado> recursosRequest =
                request.getRecursos() != null ? request.getRecursos() : new ArrayList<>();
        List<ConfigurarCapacidadRequest.EmpleadoLimite> empleadosRequest =
                request.getEmpleados() != null ? request.getEmpleados() : new ArrayList<>();

        if (recursosRequest.isEmpty() && empleadosRequest.isEmpty()) {
            throw new CapacidadInvalidaException("Debes asignar al menos un recurso o un empleado disponible");
        }

        // RN8: valida cada empleado (pertenece al servicio+sede) y guarda su límite simultáneo.
        Map<Integer, Empleado> empleadosPorId = new LinkedHashMap<>();
        Map<Integer, Integer> limitesPorEmpleado = new LinkedHashMap<>();
        for (ConfigurarCapacidadRequest.EmpleadoLimite el : empleadosRequest) {
            if (limitesPorEmpleado.containsKey(el.getIdEmpleado())) {
                throw new CapacidadInvalidaException("El empleado " + el.getIdEmpleado() + " está duplicado en la configuración");
            }
            Empleado empleado = obtenerEmpleadoDelServicioYSede(el.getIdEmpleado(), servicio, sede);
            empleadosPorId.put(empleado.getId(), empleado);
            limitesPorEmpleado.put(empleado.getId(), el.getLimiteSimultaneo());
        }

        // RN7/RN9/RN10: valida cada recurso (pertenece al servicio+sede) y cuenta cuántos
        // recursos quedan a cargo de cada empleado responsable.
        Map<Integer, Recurso> recursosPorId = new LinkedHashMap<>();
        Map<Integer, Integer> idEmpleadoResponsablePorRecurso = new LinkedHashMap<>();
        Map<Integer, Integer> conteoRecursosPorEmpleado = new LinkedHashMap<>();
        for (ConfigurarCapacidadRequest.RecursoAsignado ra : recursosRequest) {
            if (recursosPorId.containsKey(ra.getIdRecurso())) {
                throw new CapacidadInvalidaException("El recurso " + ra.getIdRecurso() + " está duplicado en la configuración");
            }
            Recurso recurso = obtenerRecursoDelServicioYSede(ra.getIdRecurso(), servicio, sede);
            recursosPorId.put(recurso.getId(), recurso);

            if (ra.getIdEmpleado() != null) {
                if (!limitesPorEmpleado.containsKey(ra.getIdEmpleado())) {
                    throw new CapacidadInvalidaException(
                            "El empleado responsable " + ra.getIdEmpleado() + " del recurso " + ra.getIdRecurso()
                                    + " debe estar incluido en la lista de empleados con su límite simultáneo");
                }
                idEmpleadoResponsablePorRecurso.put(recurso.getId(), ra.getIdEmpleado());
                conteoRecursosPorEmpleado.merge(ra.getIdEmpleado(), 1, Integer::sum);
            }
        }

        // RN10: un empleado no puede tener más recursos asignados que su límite simultáneo.
        for (Map.Entry<Integer, Integer> conteo : conteoRecursosPorEmpleado.entrySet()) {
            Integer limite = limitesPorEmpleado.get(conteo.getKey());
            if (conteo.getValue() > limite) {
                throw new CapacidadInvalidaException(
                        "El empleado " + conteo.getKey() + " tiene " + conteo.getValue()
                                + " recursos asignados, pero su límite simultáneo configurado es " + limite);
            }
        }

        // RN9: la capacidad no puede superar los recursos disponibles ni la capacidad del personal asignado.
        if (!recursosPorId.isEmpty() && request.getCapacidad() > recursosPorId.size()) {
            throw new CapacidadInvalidaException(
                    "La capacidad (" + request.getCapacidad() + ") no puede superar la cantidad de recursos disponibles ("
                            + recursosPorId.size() + ")");
        }
        if (!limitesPorEmpleado.isEmpty()) {
            int capacidadTotalPersonal = limitesPorEmpleado.values().stream().mapToInt(Integer::intValue).sum();
            if (request.getCapacidad() > capacidadTotalPersonal) {
                throw new CapacidadInvalidaException(
                        "La capacidad (" + request.getCapacidad() + ") no puede superar la capacidad simultánea total del personal asignado ("
                                + capacidadTotalPersonal + ")");
            }
        }

        List<CapacidadResponse> respuesta = new ArrayList<>();
        for (DiaSemana dia : request.getDiasSemana()) {
            CapacidadAtencion capacidadAtencion = guardarCapacidadAtencion(
                    servicio, sede, dia, request.getHoraInicio(), request.getHoraFin(), request.getCapacidad());

            List<CapacidadEmpleado> capacidadEmpleados = new ArrayList<>();
            for (Map.Entry<Integer, Integer> limite : limitesPorEmpleado.entrySet()) {
                CapacidadEmpleado capacidadEmpleado = new CapacidadEmpleado();
                capacidadEmpleado.setCapacidadAtencion(capacidadAtencion);
                capacidadEmpleado.setEmpleado(empleadosPorId.get(limite.getKey()));
                capacidadEmpleado.setLimiteSimultaneo(limite.getValue());
                capacidadEmpleados.add(capacidadEmpleadoRepository.save(capacidadEmpleado));
            }

            List<CapacidadRecurso> capacidadRecursos = new ArrayList<>();
            for (Recurso recurso : recursosPorId.values()) {
                CapacidadRecurso capacidadRecurso = new CapacidadRecurso();
                capacidadRecurso.setCapacidadAtencion(capacidadAtencion);
                capacidadRecurso.setRecurso(recurso);
                Integer idEmpleadoResponsable = idEmpleadoResponsablePorRecurso.get(recurso.getId());
                capacidadRecurso.setEmpleado(idEmpleadoResponsable != null ? empleadosPorId.get(idEmpleadoResponsable) : null);
                capacidadRecursos.add(capacidadRecursoRepository.save(capacidadRecurso));
            }

            respuesta.add(CapacidadResponse.desde(capacidadAtencion, capacidadRecursos, capacidadEmpleados));
        }

        return respuesta;
    }

    public List<CapacidadResponse> consultarCapacidad(Integer idServicio, Integer idSede) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        obtenerServicioDeLaEmpresa(idServicio, empresa);
        obtenerSedeDeLaEmpresa(idSede, empresa);
        validarAsociacion(idServicio, idSede);

        List<CapacidadResponse> respuesta = new ArrayList<>();
        for (CapacidadAtencion capacidadAtencion : capacidadAtencionRepository.findByServicio_IdAndSede_Id(idServicio, idSede)) {
            List<CapacidadRecurso> capacidadRecursos = capacidadRecursoRepository.findByCapacidadAtencion_Id(capacidadAtencion.getId());
            List<CapacidadEmpleado> capacidadEmpleados = capacidadEmpleadoRepository.findByCapacidadAtencion_Id(capacidadAtencion.getId());
            respuesta.add(CapacidadResponse.desde(capacidadAtencion, capacidadRecursos, capacidadEmpleados));
        }
        return respuesta;
    }

    // RN11: si ya existe una configuración para ese servicio+sede+día+horario, se reemplazan
    // sus recursos/empleados (se conserva el id y la fecha de creación).
    private CapacidadAtencion guardarCapacidadAtencion(Servicio servicio, Sede sede, DiaSemana dia,
                                                         java.time.LocalTime horaInicio, java.time.LocalTime horaFin,
                                                         Integer capacidad) {
        CapacidadAtencion capacidadAtencion = capacidadAtencionRepository
                .findByServicio_IdAndSede_IdAndDiaSemanaAndHoraInicioAndHoraFin(servicio.getId(), sede.getId(), dia, horaInicio, horaFin)
                .orElseGet(() -> {
                    CapacidadAtencion nueva = new CapacidadAtencion();
                    nueva.setServicio(servicio);
                    nueva.setSede(sede);
                    nueva.setDiaSemana(dia);
                    nueva.setHoraInicio(horaInicio);
                    nueva.setHoraFin(horaFin);
                    nueva.setFechaCreacion(LocalDateTime.now());
                    return nueva;
                });

        if (capacidadAtencion.getId() != null) {
            capacidadRecursoRepository.deleteByCapacidadAtencion_Id(capacidadAtencion.getId());
            capacidadEmpleadoRepository.deleteByCapacidadAtencion_Id(capacidadAtencion.getId());
        }

        capacidadAtencion.setCapacidad(capacidad);
        capacidadAtencion.setFechaModificacion(LocalDateTime.now());
        return capacidadAtencionRepository.save(capacidadAtencion);
    }

    private void validarAsociacion(Integer idServicio, Integer idSede) {
        if (!servicioSedeRepository.existsByServicio_IdAndSede_Id(idServicio, idSede)) {
            throw new AsociacionServicioSedeNoEncontradaException(
                    "El servicio " + idServicio + " no está asociado a la sede " + idSede);
        }
    }

    private Servicio obtenerServicioDeLaEmpresa(Integer idServicio, Empresa empresa) {
        return servicioRepository.findById(idServicio)
                .filter(s -> s.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new ServicioNoEncontradoException(
                        "El servicio " + idServicio + " no existe o no pertenece a tu Empresa"));
    }

    private Sede obtenerSedeDeLaEmpresa(Integer idSede, Empresa empresa) {
        return sedeRepository.findById(idSede)
                .filter(s -> s.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new SedeNoEncontradaException(
                        "La sede " + idSede + " no existe o no pertenece a tu Empresa"));
    }

    private Empleado obtenerEmpleadoDelServicioYSede(Integer idEmpleado, Servicio servicio, Sede sede) {
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new CapacidadInvalidaException("El empleado " + idEmpleado + " no existe"));
        if (!empleado.getServicio().getId().equals(servicio.getId()) || !empleado.getSede().getId().equals(sede.getId())) {
            throw new CapacidadInvalidaException(
                    "El empleado " + idEmpleado + " no pertenece al servicio " + servicio.getId() + " en la sede " + sede.getId());
        }
        return empleado;
    }

    private Recurso obtenerRecursoDelServicioYSede(Integer idRecurso, Servicio servicio, Sede sede) {
        Recurso recurso = recursoRepository.findById(idRecurso)
                .orElseThrow(() -> new CapacidadInvalidaException("El recurso " + idRecurso + " no existe"));
        if (!recurso.getServicio().getId().equals(servicio.getId()) || !recurso.getSede().getId().equals(sede.getId())) {
            throw new CapacidadInvalidaException(
                    "El recurso " + idRecurso + " no pertenece al servicio " + servicio.getId() + " en la sede " + sede.getId());
        }
        return recurso;
    }
}
