package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import com.reservas.reservas_backend.dominio.Cita;
import com.reservas.reservas_backend.dominio.DiaSemana;
import com.reservas.reservas_backend.dominio.Empleado;
import com.reservas.reservas_backend.dominio.EspacioEmpleado;
import com.reservas.reservas_backend.dominio.EstadoCita;
import com.reservas.reservas_backend.dominio.HorarioEmpleado;
import com.reservas.reservas_backend.dominio.Recurso;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.UnidadDuracion;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.CapacidadAtencionRepository;
import com.reservas.reservas_backend.infraestructura.CitaAgendamientoRepository;
import com.reservas.reservas_backend.infraestructura.EspacioEmpleadoRepository;
import com.reservas.reservas_backend.infraestructura.FranjaBloqueoRepository;
import com.reservas.reservas_backend.infraestructura.HorarioEmpleadoRepository;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.infraestructura.ServicioRepository;
import com.reservas.reservas_backend.infraestructura.ServicioSedeRepository;
import com.reservas.reservas_backend.integracion.AgendarCitaRequest;
import com.reservas.reservas_backend.integracion.CitaAgendadaResponse;
import com.reservas.reservas_backend.integracion.DatosCitaInvalidosException;
import com.reservas.reservas_backend.integracion.SinDisponibilidadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// HU-22: agendar cita. La capacidad de una franja es la cantidad de pares
// empleado + espacio (HU-16); cada cita ocupa un par en una fecha y hora.
@Service
public class AgendarCitaService {

    private final ServicioRepository servicioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final CapacidadAtencionRepository capacidadAtencionRepository;
    private final HorarioEmpleadoRepository horarioEmpleadoRepository;
    private final EspacioEmpleadoRepository espacioEmpleadoRepository;
    private final FranjaBloqueoRepository franjaBloqueoRepository;
    private final CitaAgendamientoRepository citaAgendamientoRepository;

    @Autowired
    public AgendarCitaService(ServicioRepository servicioRepository,
                              SedeRepository sedeRepository,
                              ServicioSedeRepository servicioSedeRepository,
                              CapacidadAtencionRepository capacidadAtencionRepository,
                              HorarioEmpleadoRepository horarioEmpleadoRepository,
                              EspacioEmpleadoRepository espacioEmpleadoRepository,
                              FranjaBloqueoRepository franjaBloqueoRepository,
                              CitaAgendamientoRepository citaAgendamientoRepository) {
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.capacidadAtencionRepository = capacidadAtencionRepository;
        this.horarioEmpleadoRepository = horarioEmpleadoRepository;
        this.espacioEmpleadoRepository = espacioEmpleadoRepository;
        this.franjaBloqueoRepository = franjaBloqueoRepository;
        this.citaAgendamientoRepository = citaAgendamientoRepository;
    }

    // RN8: el Usuario revisa los datos antes de confirmar. No guarda nada.
    @Transactional(readOnly = true)
    public CitaAgendadaResponse previsualizar(AgendarCitaRequest request) {
        return aRespuesta(null, evaluar(request, false));
    }

    // RN10: la cita se guarda solo al confirmar, tras validar de nuevo la capacidad.
    @Transactional
    public CitaAgendadaResponse agendar(Usuario usuario, AgendarCitaRequest request) {
        Evaluacion e = evaluar(request, true);

        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setServicio(e.servicio());
        cita.setSede(e.sede());
        cita.setFecha(e.fecha());
        cita.setHoraInicio(e.horaInicio());
        cita.setHoraFin(e.horaFin());
        cita.setEmpleado(e.empleado());
        cita.setRecurso(e.recurso());
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setFechaCreacion(LocalDateTime.now());

        Cita guardada = citaAgendamientoRepository.save(cita);
        return aRespuesta(guardada.getId(), e);
    }

    private Evaluacion evaluar(AgendarCitaRequest request, boolean bloquearFranja) {
        Servicio servicio = servicioRepository.findById(request.idServicio())
                .orElseThrow(() -> new DatosCitaInvalidosException("El servicio seleccionado no existe"));
        Sede sede = sedeRepository.findById(request.idSede())
                .orElseThrow(() -> new DatosCitaInvalidosException("La sede seleccionada no existe"));

        // Mismas condiciones de "servicio disponible" que HU-19
        if (!Boolean.TRUE.equals(sede.getActiva())
                || !Boolean.TRUE.equals(servicio.getEmpresa().getRegistroCompleto())) {
            throw new DatosCitaInvalidosException("El servicio no está disponible en la sede seleccionada");
        }
        if (!servicioSedeRepository.existsByServicio_IdAndSede_Id(servicio.getId(), sede.getId())) {
            throw new DatosCitaInvalidosException("El servicio no se ofrece en la sede seleccionada");
        }

        LocalDate fecha = request.fecha();
        LocalTime hora = request.hora();
        if (!LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now())) {
            throw new DatosCitaInvalidosException("La fecha y la hora de la cita deben ser futuras");
        }

        // RN3: la duración de la cita es la configurada para el servicio
        int minutos = duracionEnMinutos(servicio);
        if (minutos <= 0) {
            throw new DatosCitaInvalidosException("El servicio no tiene una duración válida");
        }
        LocalTime horaFin = hora.plusMinutes(minutos);
        if (!horaFin.isAfter(hora)) {
            throw new DatosCitaInvalidosException("La cita no puede terminar después de la medianoche");
        }

        // RN2: debe existir una franja del servicio y la sede que contenga la hora elegida
        DiaSemana dia = aDiaSemana(fecha.getDayOfWeek());
        CapacidadAtencion franja = capacidadAtencionRepository
                .findByServicio_IdAndSede_Id(servicio.getId(), sede.getId()).stream()
                .filter(c -> c.getDiaSemana() == dia && encaja(c, hora, horaFin, minutos))
                .findFirst()
                .orElseThrow(() -> new SinDisponibilidadException(
                        "No hay disponibilidad para la fecha y hora seleccionadas"));

        // Último cupo: se bloquea la franja y se cuenta de nuevo después del bloqueo
        if (bloquearFranja) {
            franjaBloqueoRepository.bloquearPorId(franja.getId());
        }

        // RN5/RN6/RN7: pares empleado + espacio de la franja
        List<Par> pares = construirPares(servicio.getId(), sede.getId(), franja.getId());
        if (pares.isEmpty()) {
            throw new SinDisponibilidadException(
                    "No hay personal con espacios asignados para el horario seleccionado");
        }

        // RN4: cupo total de la franja
        List<Cita> ocupadas = citaAgendamientoRepository
                .findByServicio_IdAndSede_IdAndFechaAndHoraInicioAndEstado(
                        servicio.getId(), sede.getId(), fecha, hora, EstadoCita.PROGRAMADA);
        if (ocupadas.size() >= pares.size()) {
            throw new SinDisponibilidadException("No hay cupos disponibles en el horario seleccionado");
        }

        Set<String> usados = new HashSet<>();
        for (Cita c : ocupadas) {
            if (c.getEmpleado() != null && c.getRecurso() != null) {
                usados.add(clave(c.getEmpleado().getId(), c.getRecurso().getId()));
            }
        }
        Par elegido = pares.stream()
                .filter(p -> !usados.contains(clave(p.empleado().getId(), p.recurso().getId())))
                .findFirst()
                .orElseThrow(() -> new SinDisponibilidadException(
                        "No hay un empleado y un espacio disponibles para el horario seleccionado"));

        return new Evaluacion(servicio, sede, fecha, hora, horaFin, elegido.empleado(), elegido.recurso());
    }

    // Empleados que tienen la franja en su horario, cada uno con los espacios que atiende.
    private List<Par> construirPares(Integer idServicio, Integer idSede, Integer idFranja) {
        Map<Integer, Empleado> empleados = new LinkedHashMap<>();
        horarioEmpleadoRepository.findByFranja_Servicio_IdAndFranja_Sede_Id(idServicio, idSede).stream()
                .filter(h -> h.getFranja().getId().equals(idFranja))
                .sorted(Comparator.comparing((HorarioEmpleado h) -> h.getEmpleado().getId()))
                .forEach(h -> empleados.put(h.getEmpleado().getId(), h.getEmpleado()));

        Map<Integer, List<Recurso>> espaciosPorEmpleado = new HashMap<>();
        for (EspacioEmpleado ee : espacioEmpleadoRepository
                .findByEmpleado_Servicio_IdAndEmpleado_Sede_Id(idServicio, idSede)) {
            espaciosPorEmpleado.computeIfAbsent(ee.getEmpleado().getId(), k -> new ArrayList<>())
                    .add(ee.getRecurso());
        }

        List<Par> pares = new ArrayList<>();
        for (Empleado empleado : empleados.values()) {
            List<Recurso> espacios = new ArrayList<>(
                    espaciosPorEmpleado.getOrDefault(empleado.getId(), new ArrayList<>()));
            espacios.sort(Comparator.comparing(Recurso::getId));
            for (Recurso espacio : espacios) {
                pares.add(new Par(empleado, espacio));
            }
        }
        return pares;
    }

    private String clave(Integer idEmpleado, Integer idRecurso) {
        return idEmpleado + "-" + idRecurso;
    }

    // La hora debe estar dentro de la franja y arrancar en un múltiplo de la duración
    private boolean encaja(CapacidadAtencion franja, LocalTime hora, LocalTime horaFin, int minutos) {
        if (hora.isBefore(franja.getHoraInicio()) || horaFin.isAfter(franja.getHoraFin())) {
            return false;
        }
        return Duration.between(franja.getHoraInicio(), hora).toMinutes() % minutos == 0;
    }

    private int duracionEnMinutos(Servicio servicio) {
        int duracion = servicio.getDuracion();
        return servicio.getUnidadDuracion() == UnidadDuracion.HORAS ? duracion * 60 : duracion;
    }

    private DiaSemana aDiaSemana(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private CitaAgendadaResponse aRespuesta(Integer id, Evaluacion e) {
        return new CitaAgendadaResponse(
                id,
                id == null ? null : EstadoCita.PROGRAMADA.name(),
                e.servicio().getEmpresa().getNombre(),
                e.servicio().getNombre(),
                e.sede().getNombre(),
                e.fecha(),
                e.horaInicio(),
                e.horaFin(),
                e.servicio().getDuracion(),
                e.servicio().getUnidadDuracion().name(),
                e.empleado().getNombre(),
                e.recurso().getIdentificador());
    }

    private record Par(Empleado empleado, Recurso recurso) {
    }

    private record Evaluacion(Servicio servicio, Sede sede, LocalDate fecha, LocalTime horaInicio,
                              LocalTime horaFin, Empleado empleado, Recurso recurso) {
    }
}
