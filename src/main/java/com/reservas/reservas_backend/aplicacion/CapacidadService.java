package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.integracion.AsociacionServicioSedeNoEncontradaException;
import com.reservas.reservas_backend.integracion.CapacidadInvalidaException;
import com.reservas.reservas_backend.integracion.CapacidadResumenResponse;
import com.reservas.reservas_backend.integracion.ConfigurarCapacidadRequest;
import com.reservas.reservas_backend.integracion.DefinirFranjasRequest;
import com.reservas.reservas_backend.integracion.FranjaResponse;
import com.reservas.reservas_backend.integracion.RecursoResponse;
import com.reservas.reservas_backend.integracion.SedeNoEncontradaException;
import com.reservas.reservas_backend.integracion.ServicioNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

// HU-16: Configurar capacidad de atención (RN1-RN13).
// La capacidad de cada franja se calcula siempre con el mismo método (calcularResumen),
// tanto en la previsualización como al confirmar y al consultar, para que los totales
// mostrados coincidan con los horarios y espacios asignados.
@Service
public class CapacidadService {

    private static final Comparator<CapacidadAtencion> ORDEN_FRANJAS =
            Comparator.comparing(CapacidadAtencion::getDiaSemana).thenComparing(CapacidadAtencion::getHoraInicio);

    private final ServicioRepository servicioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RecursoRepository recursoRepository;
    private final CapacidadAtencionRepository capacidadAtencionRepository;
    private final HorarioEmpleadoRepository horarioEmpleadoRepository;
    private final EspacioEmpleadoRepository espacioEmpleadoRepository;
    private final EmpresaContextoService empresaContexto;

    @Autowired
    public CapacidadService(ServicioRepository servicioRepository, SedeRepository sedeRepository,
                             ServicioSedeRepository servicioSedeRepository, EmpleadoRepository empleadoRepository,
                             RecursoRepository recursoRepository, CapacidadAtencionRepository capacidadAtencionRepository,
                             HorarioEmpleadoRepository horarioEmpleadoRepository,
                             EspacioEmpleadoRepository espacioEmpleadoRepository,
                             EmpresaContextoService empresaContexto) {
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.empleadoRepository = empleadoRepository;
        this.recursoRepository = recursoRepository;
        this.capacidadAtencionRepository = capacidadAtencionRepository;
        this.horarioEmpleadoRepository = horarioEmpleadoRepository;
        this.espacioEmpleadoRepository = espacioEmpleadoRepository;
        this.empresaContexto = empresaContexto;
    }

    // RN1: define las franjas sobre las que luego se configura la capacidad (quedan con capacidad 0).
    @Transactional
    public List<FranjaResponse> definirFranjas(Integer idServicio, Integer idSede, DefinirFranjasRequest request) {
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio);
        Sede sede = obtenerSedeDeLaEmpresa(idSede, servicio.getEmpresa());
        validarAsociacion(idServicio, idSede);

        // Se validan contra las ya guardadas y contra las anteriores de la misma solicitud.
        List<CapacidadAtencion> franjas = new ArrayList<>(capacidadAtencionRepository.findByServicio_IdAndSede_Id(idServicio, idSede));
        List<CapacidadAtencion> nuevas = new ArrayList<>();
        for (DefinirFranjasRequest.FranjaNueva franjaNueva : request.getFranjas()) {
            CapacidadAtencion franja = new CapacidadAtencion();
            franja.setServicio(servicio);
            franja.setSede(sede);
            franja.setDiaSemana(franjaNueva.getDiaSemana());
            franja.setHoraInicio(franjaNueva.getHoraInicio());
            franja.setHoraFin(franjaNueva.getHoraFin());
            franja.setCapacidad(0);
            franja.setFechaCreacion(LocalDateTime.now());

            if (!franja.getHoraFin().isAfter(franja.getHoraInicio())) {
                throw new CapacidadInvalidaException(
                        "En la franja " + describir(franja) + " la hora de fin debe ser posterior a la hora de inicio");
            }
            for (CapacidadAtencion otra : franjas) {
                boolean seSuperpone = otra.getDiaSemana() == franja.getDiaSemana()
                        && franja.getHoraInicio().isBefore(otra.getHoraFin())
                        && otra.getHoraInicio().isBefore(franja.getHoraFin());
                if (seSuperpone) {
                    throw new CapacidadInvalidaException(
                            "La franja " + describir(franja) + " se superpone con la franja " + describir(otra));
                }
            }
            franjas.add(franja);
            nuevas.add(franja);
        }
        capacidadAtencionRepository.saveAll(nuevas);

        return consultarFranjas(idServicio, idSede);
    }

    public List<FranjaResponse> consultarFranjas(Integer idServicio, Integer idSede) {
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio);
        obtenerSedeDeLaEmpresa(idSede, servicio.getEmpresa());
        validarAsociacion(idServicio, idSede);

        List<FranjaResponse> respuesta = new ArrayList<>();
        for (CapacidadAtencion franja : obtenerFranjasOrdenadas(idServicio, idSede)) {
            respuesta.add(FranjaResponse.desde(franja));
        }
        return respuesta;
    }

    // RN13: calcula y muestra el resumen sin guardar nada, para revisarlo antes de confirmar.
    public CapacidadResumenResponse previsualizarCapacidad(Integer idServicio, Integer idSede,
                                                           ConfigurarCapacidadRequest request) {
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio);
        Sede sede = obtenerSedeDeLaEmpresa(idSede, servicio.getEmpresa());
        validarAsociacion(idServicio, idSede);

        List<CapacidadAtencion> franjas = obtenerFranjasOrdenadas(idServicio, idSede);
        List<Asignacion> asignaciones = construirAsignaciones(servicio, sede, franjas, request);
        return calcularResumen(servicio, sede, franjas, asignaciones);
    }

    // Confirma la configuración: reemplaza la anterior del servicio+sede y guarda la capacidad calculada.
    @Transactional
    public CapacidadResumenResponse configurarCapacidad(Integer idServicio, Integer idSede,
                                                        ConfigurarCapacidadRequest request) {
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio);
        Sede sede = obtenerSedeDeLaEmpresa(idSede, servicio.getEmpresa());
        validarAsociacion(idServicio, idSede);

        List<CapacidadAtencion> franjas = obtenerFranjasOrdenadas(idServicio, idSede);
        List<Asignacion> asignaciones = construirAsignaciones(servicio, sede, franjas, request);
        CapacidadResumenResponse resumen = calcularResumen(servicio, sede, franjas, asignaciones);

        // RN9: no se puede continuar mientras alguna franja no alcance para una atención.
        if (!resumen.isCompleta()) {
            List<String> pendientes = new ArrayList<>();
            for (FranjaResponse pendiente : resumen.getFranjasPendientes()) {
                pendientes.add(describir(pendiente));
            }
            throw new CapacidadInvalidaException(
                    "Las siguientes franjas están pendientes de capacidad (necesitan al menos un empleado con un espacio): "
                            + String.join(", ", pendientes));
        }

        horarioEmpleadoRepository.eliminarPorServicioYSede(idServicio, idSede);
        espacioEmpleadoRepository.eliminarPorServicioYSede(idServicio, idSede);

        for (Asignacion asignacion : asignaciones) {
            Empleado empleado = asignacion.empleado();
            // RN2: el personal agregado durante el flujo se registra al confirmar.
            if (empleado.getId() == null) {
                empleado.setFechaCreacion(LocalDateTime.now());
                empleado = empleadoRepository.save(empleado);
            }
            for (CapacidadAtencion franja : asignacion.franjas()) {
                horarioEmpleadoRepository.save(new HorarioEmpleado(null, franja, empleado));
            }
            for (Recurso espacio : asignacion.espacios()) {
                espacioEmpleadoRepository.save(new EspacioEmpleado(null, empleado, espacio));
            }
        }

        Map<Integer, Integer> capacidadPorFranja = new HashMap<>();
        for (CapacidadResumenResponse.FranjaCapacidad franjaCapacidad : resumen.getFranjas()) {
            capacidadPorFranja.put(franjaCapacidad.getIdFranja(), franjaCapacidad.getCapacidad());
        }
        for (CapacidadAtencion franja : franjas) {
            franja.setCapacidad(capacidadPorFranja.get(franja.getId()));
            franja.setFechaModificacion(LocalDateTime.now());
            capacidadAtencionRepository.save(franja);
        }

        return consultarCapacidad(idServicio, idSede);
    }

    // Resumen de la configuración guardada; incluye todo el personal disponible del servicio+sede (RN2).
    public CapacidadResumenResponse consultarCapacidad(Integer idServicio, Integer idSede) {
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio);
        Sede sede = obtenerSedeDeLaEmpresa(idSede, servicio.getEmpresa());
        validarAsociacion(idServicio, idSede);

        List<CapacidadAtencion> franjas = obtenerFranjasOrdenadas(idServicio, idSede);

        Map<Integer, List<CapacidadAtencion>> franjasPorEmpleado = new HashMap<>();
        for (HorarioEmpleado horario : horarioEmpleadoRepository.findByFranja_Servicio_IdAndFranja_Sede_Id(idServicio, idSede)) {
            franjasPorEmpleado.computeIfAbsent(horario.getEmpleado().getId(), k -> new ArrayList<>()).add(horario.getFranja());
        }
        Map<Integer, List<Recurso>> espaciosPorEmpleado = new HashMap<>();
        for (EspacioEmpleado espacio : espacioEmpleadoRepository.findByEmpleado_Servicio_IdAndEmpleado_Sede_Id(idServicio, idSede)) {
            espaciosPorEmpleado.computeIfAbsent(espacio.getEmpleado().getId(), k -> new ArrayList<>()).add(espacio.getRecurso());
        }

        List<Asignacion> asignaciones = new ArrayList<>();
        for (Empleado empleado : empleadoRepository.findByServicio_IdAndSede_Id(idServicio, idSede)) {
            List<CapacidadAtencion> franjasEmpleado = franjasPorEmpleado.getOrDefault(empleado.getId(), new ArrayList<>());
            franjasEmpleado.sort(ORDEN_FRANJAS);
            asignaciones.add(new Asignacion(empleado, franjasEmpleado,
                    espaciosPorEmpleado.getOrDefault(empleado.getId(), new ArrayList<>())));
        }

        return calcularResumen(servicio, sede, franjas, asignaciones);
    }

    // Valida la solicitud (RN2, RN4, pertenencia al servicio+sede) y la convierte en asignaciones.
    private List<Asignacion> construirAsignaciones(Servicio servicio, Sede sede, List<CapacidadAtencion> franjas,
                                                   ConfigurarCapacidadRequest request) {
        if (franjas.isEmpty()) {
            throw new CapacidadInvalidaException(
                    "Primero debes definir las franjas de atención del servicio en esta sede");
        }
        Map<Integer, CapacidadAtencion> franjasPorId = new HashMap<>();
        for (CapacidadAtencion franja : franjas) {
            franjasPorId.put(franja.getId(), franja);
        }

        List<Asignacion> asignaciones = new ArrayList<>();
        Set<Integer> idsEmpleadosVistos = new HashSet<>();
        for (ConfigurarCapacidadRequest.AsignacionEmpleado item : request.getEmpleados()) {
            Empleado empleado;
            if (item.getIdEmpleado() != null) {
                if (!idsEmpleadosVistos.add(item.getIdEmpleado())) {
                    throw new CapacidadInvalidaException("El empleado " + item.getIdEmpleado() + " está duplicado en la configuración");
                }
                empleado = obtenerEmpleadoDelServicioYSede(item.getIdEmpleado(), servicio, sede);
            } else {
                // RN2: personal adicional, mínimo nombre y cargo o función.
                if (estaVacio(item.getNombre()) || estaVacio(item.getCargo())) {
                    throw new CapacidadInvalidaException(
                            "Para agregar personal adicional debes indicar como mínimo su nombre y su cargo o función");
                }
                empleado = new Empleado();
                empleado.setServicio(servicio);
                empleado.setSede(sede);
                empleado.setNombre(item.getNombre().trim());
                empleado.setCargo(item.getCargo().trim());
            }

            List<CapacidadAtencion> franjasEmpleado = new ArrayList<>();
            for (Integer idFranja : sinRepetir(item.getIdsFranjas())) {
                CapacidadAtencion franja = franjasPorId.get(idFranja);
                if (franja == null) {
                    throw new CapacidadInvalidaException(
                            "La franja " + idFranja + " no está definida para el servicio " + servicio.getId()
                                    + " en la sede " + sede.getId());
                }
                franjasEmpleado.add(franja);
            }
            franjasEmpleado.sort(ORDEN_FRANJAS);

            List<Recurso> espacios = new ArrayList<>();
            for (Integer idEspacio : sinRepetir(item.getIdsEspacios())) {
                espacios.add(obtenerRecursoDelServicioYSede(idEspacio, servicio, sede));
            }

            // RN4: no se asignan espacios a un empleado sin al menos un horario.
            if (!espacios.isEmpty() && franjasEmpleado.isEmpty()) {
                throw new CapacidadInvalidaException(
                        "Para asignar espacios a " + empleado.getNombre()
                                + " primero debes seleccionar al menos un horario");
            }

            asignaciones.add(new Asignacion(empleado, franjasEmpleado, espacios));
        }
        return asignaciones;
    }

    // RN6/RN7/RN9/RN10/RN12: calcula la capacidad individual y la de cada franja.
    private CapacidadResumenResponse calcularResumen(Servicio servicio, Sede sede, List<CapacidadAtencion> franjas,
                                                     List<Asignacion> asignaciones) {
        List<CapacidadResumenResponse.FranjaCapacidad> franjasCapacidad = new ArrayList<>();
        List<FranjaResponse> franjasPendientes = new ArrayList<>();

        for (CapacidadAtencion franja : franjas) {
            int capacidad = 0;
            List<CapacidadResumenResponse.ParticipanteFranja> personal = new ArrayList<>();
            Map<Integer, Empleado> usoEspacioExclusivo = new HashMap<>();

            for (Asignacion asignacion : asignaciones) {
                if (!asignacion.tieneFranja(franja)) {
                    continue;
                }
                for (Recurso espacio : asignacion.espacios()) {
                    // RN10: un espacio exclusivo no puede usarlo otro empleado en la misma franja.
                    if (!Boolean.FALSE.equals(espacio.getExclusivo())) {
                        Empleado otro = usoEspacioExclusivo.putIfAbsent(espacio.getId(), asignacion.empleado());
                        if (otro != null) {
                            throw new CapacidadInvalidaException(
                                    "El espacio '" + espacio.getIdentificador() + "' ya está siendo utilizado por "
                                            + otro.getNombre() + " en la franja " + describir(franja)
                                            + "; no puede asignarse también a " + asignacion.empleado().getNombre());
                        }
                    }
                }
                // RN7/RN11: la franja suma los espacios de cada empleado que la tiene asignada.
                capacidad += asignacion.espacios().size();
                personal.add(new CapacidadResumenResponse.ParticipanteFranja(
                        asignacion.empleado().getId(), asignacion.empleado().getNombre(),
                        aRecursos(asignacion.espacios())));
            }

            // RN9: cada franja necesita capacidad mínima de una atención.
            boolean pendiente = capacidad < 1;
            if (pendiente) {
                franjasPendientes.add(new FranjaResponse(franja.getId(), franja.getDiaSemana(),
                        franja.getHoraInicio(), franja.getHoraFin(), capacidad));
            }
            franjasCapacidad.add(new CapacidadResumenResponse.FranjaCapacidad(
                    franja.getId(), franja.getDiaSemana(), franja.getHoraInicio(), franja.getHoraFin(),
                    capacidad, pendiente, personal));
        }

        List<CapacidadResumenResponse.EmpleadoCapacidad> empleados = new ArrayList<>();
        for (Asignacion asignacion : asignaciones) {
            Empleado empleado = asignacion.empleado();
            List<FranjaResponse> horarios = new ArrayList<>();
            for (CapacidadAtencion franja : asignacion.franjas()) {
                horarios.add(new FranjaResponse(franja.getId(), franja.getDiaSemana(),
                        franja.getHoraInicio(), franja.getHoraFin(), null));
            }
            // RN6: capacidad individual = espacios asignados. RN12: sin horarios, sus espacios no aplican.
            int capacidadIndividual = asignacion.franjas().isEmpty() ? 0 : asignacion.espacios().size();
            empleados.add(new CapacidadResumenResponse.EmpleadoCapacidad(
                    empleado.getId(), empleado.getNombre(), empleado.getCargo(), capacidadIndividual,
                    horarios, aRecursos(asignacion.espacios())));
        }

        return new CapacidadResumenResponse(servicio.getId(), sede.getId(), franjasPendientes.isEmpty(),
                franjasCapacidad, empleados, franjasPendientes);
    }

    private List<CapacidadAtencion> obtenerFranjasOrdenadas(Integer idServicio, Integer idSede) {
        List<CapacidadAtencion> franjas = new ArrayList<>(capacidadAtencionRepository.findByServicio_IdAndSede_Id(idServicio, idSede));
        franjas.sort(ORDEN_FRANJAS);
        return franjas;
    }

    private List<RecursoResponse> aRecursos(List<Recurso> recursos) {
        List<RecursoResponse> respuesta = new ArrayList<>();
        for (Recurso recurso : recursos) {
            respuesta.add(RecursoResponse.desde(recurso));
        }
        return respuesta;
    }

    private List<Integer> sinRepetir(List<Integer> ids) {
        if (ids == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private String describir(CapacidadAtencion franja) {
        return franja.getDiaSemana() + " " + franja.getHoraInicio() + "-" + franja.getHoraFin();
    }

    private String describir(FranjaResponse franja) {
        return franja.getDiaSemana() + " " + franja.getHoraInicio() + "-" + franja.getHoraFin();
    }

    private void validarAsociacion(Integer idServicio, Integer idSede) {
        if (!servicioSedeRepository.existsByServicio_IdAndSede_Id(idServicio, idSede)) {
            throw new AsociacionServicioSedeNoEncontradaException(
                    "El servicio " + idServicio + " no está asociado a la sede " + idSede);
        }
    }

    private Servicio obtenerServicioDeLaEmpresa(Integer idServicio) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
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
                .orElseThrow(() -> new CapacidadInvalidaException("El espacio " + idRecurso + " no existe"));
        if (!recurso.getServicio().getId().equals(servicio.getId()) || !recurso.getSede().getId().equals(sede.getId())) {
            throw new CapacidadInvalidaException(
                    "El espacio " + idRecurso + " no pertenece al servicio " + servicio.getId() + " en la sede " + sede.getId());
        }
        return recurso;
    }

    // Horarios y espacios de un empleado (guardado o nuevo) dentro de la configuración.
    private record Asignacion(Empleado empleado, List<CapacidadAtencion> franjas, List<Recurso> espacios) {

        boolean tieneFranja(CapacidadAtencion franja) {
            for (CapacidadAtencion propia : franjas) {
                if (propia.getId().equals(franja.getId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
