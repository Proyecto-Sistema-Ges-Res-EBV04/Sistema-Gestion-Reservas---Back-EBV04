package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Agenda;
import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import com.reservas.reservas_backend.dominio.Cita;
import com.reservas.reservas_backend.dominio.Dia;
import com.reservas.reservas_backend.dominio.DiaSemana;
import com.reservas.reservas_backend.dominio.Diaexcepcion;
import com.reservas.reservas_backend.dominio.EspacioEmpleado;
import com.reservas.reservas_backend.dominio.EstadoCita;
import com.reservas.reservas_backend.dominio.HorarioEmpleado;
import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.ServicioSede;
import com.reservas.reservas_backend.dominio.UnidadDuracion;
import com.reservas.reservas_backend.infraestructura.AgendaRepository;
import com.reservas.reservas_backend.infraestructura.BusquedaServicioRepository;
import com.reservas.reservas_backend.infraestructura.CapacidadAtencionRepository;
import com.reservas.reservas_backend.infraestructura.CitaAgendamientoRepository;
import com.reservas.reservas_backend.infraestructura.DiaExcepcionRepository;
import com.reservas.reservas_backend.infraestructura.DiaRepository;
import com.reservas.reservas_backend.infraestructura.EspacioEmpleadoRepository;
import com.reservas.reservas_backend.infraestructura.HorarioEmpleadoRepository;
import com.reservas.reservas_backend.integracion.ConsultaDisponibilidadInvalidaException;
import com.reservas.reservas_backend.integracion.DisponibilidadResponse;
import com.reservas.reservas_backend.integracion.DisponibilidadResponse.HorarioDisponible;
import com.reservas.reservas_backend.integracion.ServicioNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// HU-21: consultar disponibilidad de un servicio en una sede para una fecha.
//
// La base son las franjas de HU-16 y las citas de HU-22, con las mismas reglas que usa
// AgendarCitaService al confirmar (horarios que arrancan en múltiplos de la duración y
// cupo = pares empleado + espacio de la franja), para que todo horario mostrado aquí
// sea aceptado por HU-22. Sobre esa base se aplican las restricciones de la agenda de
// HU-14 (días de excepción y agenda semanal) cuando la Empresa las haya configurado.
//
// RN7: solo lectura. No crea citas ni aparta cupos.
@Service
public class DisponibilidadService {

    private final BusquedaServicioRepository busquedaRepository;
    private final CapacidadAtencionRepository capacidadAtencionRepository;
    private final HorarioEmpleadoRepository horarioEmpleadoRepository;
    private final EspacioEmpleadoRepository espacioEmpleadoRepository;
    private final CitaAgendamientoRepository citaAgendamientoRepository;
    private final AgendaRepository agendaRepository;
    private final DiaExcepcionRepository diaExcepcionRepository;
    private final DiaRepository diaRepository;

    @Autowired
    public DisponibilidadService(BusquedaServicioRepository busquedaRepository,
                                 CapacidadAtencionRepository capacidadAtencionRepository,
                                 HorarioEmpleadoRepository horarioEmpleadoRepository,
                                 EspacioEmpleadoRepository espacioEmpleadoRepository,
                                 CitaAgendamientoRepository citaAgendamientoRepository,
                                 AgendaRepository agendaRepository,
                                 DiaExcepcionRepository diaExcepcionRepository,
                                 DiaRepository diaRepository) {
        this.busquedaRepository = busquedaRepository;
        this.capacidadAtencionRepository = capacidadAtencionRepository;
        this.horarioEmpleadoRepository = horarioEmpleadoRepository;
        this.espacioEmpleadoRepository = espacioEmpleadoRepository;
        this.citaAgendamientoRepository = citaAgendamientoRepository;
        this.agendaRepository = agendaRepository;
        this.diaExcepcionRepository = diaExcepcionRepository;
        this.diaRepository = diaRepository;
    }

    @Transactional(readOnly = true)
    public DisponibilidadResponse consultar(Integer idServicio, Integer idSede, LocalDate fecha) {
        // RN1: servicio, sede y fecha concretos
        if (fecha == null) {
            throw new ConsultaDisponibilidadInvalidaException("Debes elegir una fecha de atención");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new ConsultaDisponibilidadInvalidaException("La fecha de atención no puede ser anterior a hoy");
        }

        // Mismo criterio de visibilidad que HU-19/HU-20 (sede activa y Empresa con registro completo)
        ServicioSede oferta = busquedaRepository.ofertaPublica(idServicio).stream()
                .filter(ss -> ss.getSede().getId().equals(idSede))
                .findFirst()
                .orElseThrow(() -> new ServicioNoEncontradoException(
                        "El servicio " + idServicio + " no se ofrece en la sede " + idSede));

        Servicio servicio = oferta.getServicio();
        int minutos = duracionEnMinutos(servicio);

        // RN6: sin franjas con personal y espacios no hay nada reservable en esta combinación
        List<CapacidadAtencion> franjas = capacidadAtencionRepository.findByServicio_IdAndSede_Id(idServicio, idSede);
        Map<Integer, Integer> cupoPorFranja = cupoPorFranja(idServicio, idSede);
        if (minutos <= 0 || franjas.isEmpty() || cupoPorFranja.values().stream().noneMatch(c -> c > 0)) {
            return sinHorarios(idServicio, idSede, fecha, minutos,
                    "El servicio aún no tiene agenda o capacidad configurada en esta sede");
        }

        // RN2/RN5: restricciones de la agenda de HU-14 para esa fecha
        Ventanas ventanas = ventanasDeAgenda(Long.valueOf(oferta.getId()), fecha);
        if (ventanas.bloqueada()) {
            return sinHorarios(idServicio, idSede, fecha, minutos,
                    "La fecha seleccionada no está habilitada para reservar");
        }

        DiaSemana dia = aDiaSemana(fecha.getDayOfWeek());
        List<CapacidadAtencion> franjasDelDia = franjas.stream()
                .filter(f -> f.getDiaSemana() == dia)
                .sorted(Comparator.comparing(CapacidadAtencion::getHoraInicio))
                .toList();
        if (franjasDelDia.isEmpty()) {
            return sinHorarios(idServicio, idSede, fecha, minutos,
                    "No hay franjas habilitadas para reservar en la fecha seleccionada");
        }

        // RN4: cupos ya ocupados por citas vigentes de HU-22, agrupados por hora de inicio
        Map<LocalTime, Integer> ocupadasPorHora = new HashMap<>();
        for (Cita cita : citaAgendamientoRepository.findByServicio_IdAndSede_IdAndFechaAndEstado(
                idServicio, idSede, fecha, EstadoCita.PROGRAMADA)) {
            ocupadasPorHora.merge(cita.getHoraInicio(), 1, Integer::sum);
        }

        LocalDateTime ahora = LocalDateTime.now();
        Map<LocalTime, HorarioDisponible> horarios = new TreeMap<>();
        for (CapacidadAtencion franja : franjasDelDia) {
            // RN3: capacidad calculada de la franja (pares empleado + espacio)
            int cupo = cupoPorFranja.getOrDefault(franja.getId(), 0);
            if (cupo <= 0) {
                continue;
            }
            LocalTime inicio = franja.getHoraInicio();
            while (true) {
                LocalTime fin = inicio.plusMinutes(minutos);
                // Fin de la franja o paso de medianoche
                if (!fin.isAfter(inicio) || fin.isAfter(franja.getHoraFin())) {
                    break;
                }
                int libres = cupo - ocupadasPorHora.getOrDefault(inicio, 0);
                boolean futuro = LocalDateTime.of(fecha, inicio).isAfter(ahora);
                if (libres > 0 && futuro && ventanas.permite(inicio, fin)) {
                    horarios.putIfAbsent(inicio, new HorarioDisponible(inicio, fin, libres));
                }
                inicio = fin;
            }
        }

        if (horarios.isEmpty()) {
            return sinHorarios(idServicio, idSede, fecha, minutos,
                    "No hay horarios disponibles para la selección");
        }
        return new DisponibilidadResponse(idServicio, idSede, fecha, minutos, true, null,
                new ArrayList<>(horarios.values()));
    }

    // Cupo de cada franja = suma de espacios de los empleados que la tienen en su horario.
    // Es el mismo conteo de pares que usa HU-22 al confirmar la cita.
    private Map<Integer, Integer> cupoPorFranja(Integer idServicio, Integer idSede) {
        Map<Integer, Integer> espaciosPorEmpleado = new HashMap<>();
        for (EspacioEmpleado ee : espacioEmpleadoRepository
                .findByEmpleado_Servicio_IdAndEmpleado_Sede_Id(idServicio, idSede)) {
            espaciosPorEmpleado.merge(ee.getEmpleado().getId(), 1, Integer::sum);
        }

        Map<Integer, Integer> cupos = new HashMap<>();
        Set<String> vistos = new HashSet<>();
        for (HorarioEmpleado h : horarioEmpleadoRepository
                .findByFranja_Servicio_IdAndFranja_Sede_Id(idServicio, idSede)) {
            Integer idFranja = h.getFranja().getId();
            Integer idEmpleado = h.getEmpleado().getId();
            if (vistos.add(idFranja + "-" + idEmpleado)) {
                cupos.merge(idFranja, espaciosPorEmpleado.getOrDefault(idEmpleado, 0), Integer::sum);
            }
        }
        return cupos;
    }

    // HU-14: un día de excepción manda sobre la agenda semanal (inactivo = día cerrado,
    // activo = solo su horario). Sin excepción, la agenda semanal activa limita los horarios
    // a los rangos del día; si el servicio no tiene agenda semanal, mandan las franjas de HU-16.
    private Ventanas ventanasDeAgenda(Long idServicioSede, LocalDate fecha) {
        List<Diaexcepcion> excepciones =
                diaExcepcionRepository.findByIdsedeservicioAndFechaBetween(idServicioSede, fecha, fecha);
        if (!excepciones.isEmpty()) {
            Diaexcepcion ex = excepciones.get(0);
            if (!Boolean.TRUE.equals(ex.getActivo())) {
                return Ventanas.cerrada();
            }
            return Ventanas.de(List.of(new Rango(ex.getHorainicio(), ex.getHorafin())));
        }

        List<Agenda> agendas = agendaRepository.findByIdsedeservicioAndActivo(idServicioSede, true);
        if (agendas.isEmpty()) {
            return Ventanas.sinRestriccion();
        }

        Map<Long, DayOfWeek> diasPorId = new HashMap<>();
        for (Dia d : diaRepository.findAll()) {
            DayOfWeek dow = aDayOfWeek(d.getNombre());
            if (dow != null) {
                diasPorId.put(d.getId(), dow);
            }
        }
        List<Rango> rangos = agendas.stream()
                .filter(a -> diasPorId.get(a.getIddia()) == fecha.getDayOfWeek())
                .map(a -> new Rango(a.getHorainicio(), a.getHorafin()))
                .toList();
        // Fecha fuera de los días de atención de la agenda
        return rangos.isEmpty() ? Ventanas.cerrada() : Ventanas.de(rangos);
    }

    private DisponibilidadResponse sinHorarios(Integer idServicio, Integer idSede, LocalDate fecha,
                                               int minutos, String mensaje) {
        return new DisponibilidadResponse(idServicio, idSede, fecha, Math.max(minutos, 0), false, mensaje, List.of());
    }

    private int duracionEnMinutos(Servicio servicio) {
        if (servicio.getDuracion() == null) {
            return 0;
        }
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

    // Nombres de la tabla dia (ej. "Miércoles"), sin tildes ni mayúsculas.
    private DayOfWeek aDayOfWeek(String nombre) {
        if (nombre == null) {
            return null;
        }
        String n = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
        return switch (n) {
            case "lunes" -> DayOfWeek.MONDAY;
            case "martes" -> DayOfWeek.TUESDAY;
            case "miercoles" -> DayOfWeek.WEDNESDAY;
            case "jueves" -> DayOfWeek.THURSDAY;
            case "viernes" -> DayOfWeek.FRIDAY;
            case "sabado" -> DayOfWeek.SATURDAY;
            case "domingo" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }

    private record Rango(LocalTime inicio, LocalTime fin) {
    }

    // Rangos de la agenda de HU-14 en los que se puede atender ese día.
    // rangos = null significa que la agenda no restringe nada.
    private record Ventanas(boolean bloqueada, List<Rango> rangos) {

        static Ventanas cerrada() {
            return new Ventanas(true, List.of());
        }

        static Ventanas sinRestriccion() {
            return new Ventanas(false, null);
        }

        static Ventanas de(List<Rango> rangos) {
            return new Ventanas(false, rangos);
        }

        boolean permite(LocalTime inicio, LocalTime fin) {
            if (rangos == null) {
                return true;
            }
            for (Rango r : rangos) {
                if (r.inicio() != null && r.fin() != null
                        && !inicio.isBefore(r.inicio()) && !fin.isAfter(r.fin())) {
                    return true;
                }
            }
            return false;
        }
    }
}
