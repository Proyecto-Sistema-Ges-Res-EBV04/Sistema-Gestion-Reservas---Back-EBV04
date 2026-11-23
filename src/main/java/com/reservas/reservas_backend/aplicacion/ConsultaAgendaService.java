package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Agenda;
import com.reservas.reservas_backend.dominio.Dia;
import com.reservas.reservas_backend.dominio.Diaexcepcion;
import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.ServicioSede;
import com.reservas.reservas_backend.infraestructura.AgendaRepository;
import com.reservas.reservas_backend.infraestructura.DiaExcepcionRepository;
import com.reservas.reservas_backend.infraestructura.DiaRepository;
import com.reservas.reservas_backend.infraestructura.SedeRepository;
import com.reservas.reservas_backend.infraestructura.ServicioRepository;
import com.reservas.reservas_backend.infraestructura.ServicioSedeRepository;
import com.reservas.reservas_backend.integracion.AgendaConfiguradaResponse;
import com.reservas.reservas_backend.integracion.AgendaConfiguradaResponse.DiaSemanaAtencion;
import com.reservas.reservas_backend.integracion.AgendaConfiguradaResponse.EstadoFecha;
import com.reservas.reservas_backend.integracion.AgendaConfiguradaResponse.FechaAgenda;
import com.reservas.reservas_backend.integracion.AgendaConfiguradaResponse.RangoHorario;
import com.reservas.reservas_backend.integracion.AsociacionServicioSedeNoEncontradaException;
import com.reservas.reservas_backend.integracion.SedeNoEncontradaException;
import com.reservas.reservas_backend.integracion.ServicioNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Consultar agenda configurada (Empresa autenticada).
// Solo lectura: muestra exactamente lo guardado en agenda y diaexcepcion (RNF coherencia),
// sin recalcular ni descontar citas.
@Service
public class ConsultaAgendaService {

    private static final String SIN_AGENDA =
            "No existe agenda disponible para esta combinación de servicio y sede";
    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private final ServicioRepository servicioRepository;
    private final SedeRepository sedeRepository;
    private final ServicioSedeRepository servicioSedeRepository;
    private final AgendaRepository agendaRepository;
    private final DiaExcepcionRepository diaExcepcionRepository;
    private final DiaRepository diaRepository;
    private final EmpresaContextoService empresaContexto;

    @Value("${agenda.horizonte-dias:30}")
    private int horizonteDias;
    @Value("${agenda.rango-maximo-dias:31}")
    private int rangoMaximoDias;

    public ConsultaAgendaService(ServicioRepository servicioRepository,
                                 SedeRepository sedeRepository,
                                 ServicioSedeRepository servicioSedeRepository,
                                 AgendaRepository agendaRepository,
                                 DiaExcepcionRepository diaExcepcionRepository,
                                 DiaRepository diaRepository,
                                 EmpresaContextoService empresaContexto) {
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioSedeRepository = servicioSedeRepository;
        this.agendaRepository = agendaRepository;
        this.diaExcepcionRepository = diaExcepcionRepository;
        this.diaRepository = diaRepository;
        this.empresaContexto = empresaContexto;
    }

    @Transactional(readOnly = true)
    public AgendaConfiguradaResponse consultar(Integer idServicio, Integer idSede,
                                               LocalDate fechaDesde, LocalDate fechaHasta) {
        // Escenario "Cambiar servicio o sede": la combinación debe ser de la Empresa del token.
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();
        Servicio servicio = obtenerServicioDeLaEmpresa(idServicio, empresa);
        Sede sede = obtenerSedeDeLaEmpresa(idSede, empresa);
        ServicioSede vinculo = servicioSedeRepository.findByServicio_Id(idServicio).stream()
                .filter(ss -> ss.getSede().getId().equals(idSede))
                .findFirst()
                .orElseThrow(() -> new AsociacionServicioSedeNoEncontradaException(
                        "El servicio " + idServicio + " no está asociado a la sede " + idSede));
        Long idSedeServicio = vinculo.getId().longValue();

        LocalDate desde = fechaDesde != null ? fechaDesde : LocalDate.now();
        LocalDate hasta = fechaHasta != null
                ? fechaHasta
                : desde.plusDays(Math.min(horizonteDias, rangoMaximoDias));
        if (hasta.isBefore(desde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha final no puede ser anterior a la fecha inicial");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > rangoMaximoDias) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El rango máximo de consulta es de " + rangoMaximoDias + " días");
        }

        // Escenario "Agenda pendiente de configuración".
        List<Agenda> agendas = agendaRepository.findByIdsedeservicioAndActivo(idSedeServicio, true);
        if (agendas.isEmpty()) {
            return new AgendaConfiguradaResponse(servicio.getId(), servicio.getNombre(),
                    sede.getId(), sede.getNombre(), idSedeServicio,
                    false, SIN_AGENDA, desde, hasta, List.of(), List.of(), List.of());
        }

        // Tabla dia -> día de la semana (se lee el nombre: Lunes, Martes, ...).
        Map<Long, DayOfWeek> semanaPorIdDia = new HashMap<>();
        Map<DayOfWeek, Dia> diaPorSemana = new EnumMap<>(DayOfWeek.class);
        for (Dia d : diaRepository.findAll()) {
            DayOfWeek dow = aDiaSemana(d.getNombre());
            if (dow != null) {
                semanaPorIdDia.put(d.getId(), dow);
                diaPorSemana.putIfAbsent(dow, d);
            }
        }

        // Rangos de la agenda normal agrupados por día de la semana.
        Map<DayOfWeek, List<RangoHorario>> rangosPorDia = new EnumMap<>(DayOfWeek.class);
        for (Agenda a : agendas) {
            DayOfWeek dow = semanaPorIdDia.get(a.getIddia());
            if (dow == null) {
                continue;
            }
            rangosPorDia.computeIfAbsent(dow, k -> new ArrayList<>())
                    .add(new RangoHorario(a.getId(), null, a.getHorainicio(), a.getHorafin(),
                            minutos(a.getRangohora())));
        }
        rangosPorDia.values().forEach(l -> l.sort(Comparator.comparing(RangoHorario::horaInicio)));

        // Escenario "Consultar agenda configurada" / RNF claridad: los 7 días, con o sin atención.
        List<DiaSemanaAtencion> semana = new ArrayList<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            List<RangoHorario> rangos = rangosPorDia.getOrDefault(dow, List.of());
            Dia dia = diaPorSemana.get(dow);
            semana.add(new DiaSemanaAtencion(dia == null ? null : dia.getId(),
                    nombreDia(dow, dia), !rangos.isEmpty(), rangos));
        }

        // Escenario "Consultar una fecha bloqueada": estado de cada fecha del rango.
        Map<LocalDate, Diaexcepcion> excepcionPorFecha = new HashMap<>();
        diaExcepcionRepository.findByIdsedeservicioAndFechaBetween(idSedeServicio, desde, hasta)
                .forEach(e -> excepcionPorFecha.putIfAbsent(e.getFecha(), e));

        List<FechaAgenda> calendario = new ArrayList<>();
        for (LocalDate f = desde; !f.isAfter(hasta); f = f.plusDays(1)) {
            DayOfWeek dow = f.getDayOfWeek();
            String nombre = nombreDia(dow, diaPorSemana.get(dow));
            Diaexcepcion ex = excepcionPorFecha.get(f);

            // Mismo criterio que AgendaService: la excepción manda sobre la agenda normal.
            if (ex != null) {
                if (Boolean.TRUE.equals(ex.getActivo())) {
                    calendario.add(new FechaAgenda(f, nombre, EstadoFecha.HORARIO_ESPECIAL,
                            "Festivo con horario especial",
                            List.of(new RangoHorario(null, ex.getId(), ex.getHorainicio(), ex.getHorafin(),
                                    ex.getRangohora() == null ? 0L : ex.getRangohora()))));
                } else {
                    calendario.add(new FechaAgenda(f, nombre, EstadoFecha.NO_DISPONIBLE,
                            "Fecha bloqueada: no hay atención", List.of()));
                }
                continue;
            }

            List<RangoHorario> rangos = rangosPorDia.getOrDefault(dow, List.of());
            if (rangos.isEmpty()) {
                calendario.add(new FechaAgenda(f, nombre, EstadoFecha.SIN_ATENCION,
                        "Sin atención los " + nombre.toLowerCase(ES), List.of()));
            } else {
                calendario.add(new FechaAgenda(f, nombre, EstadoFecha.ATENCION, null, rangos));
            }
        }

        List<FechaAgenda> noDisponibles = calendario.stream()
                .filter(c -> c.estado() == EstadoFecha.NO_DISPONIBLE)
                .toList();

        return new AgendaConfiguradaResponse(servicio.getId(), servicio.getNombre(),
                sede.getId(), sede.getNombre(), idSedeServicio,
                true, null, desde, hasta, semana, calendario, noDisponibles);
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

    private String nombreDia(DayOfWeek dow, Dia dia) {
        if (dia != null && dia.getNombre() != null && !dia.getNombre().isBlank()) {
            return dia.getNombre().trim();
        }
        String n = dow.getDisplayName(TextStyle.FULL, ES);
        return n.substring(0, 1).toUpperCase(ES) + n.substring(1);
    }

    private DayOfWeek aDiaSemana(String nombre) {
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

    private long minutos(LocalTime rango) {
        return rango == null ? 0 : rango.getHour() * 60L + rango.getMinute();
    }
}