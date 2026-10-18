package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.presentacion.AgendaMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import java.text.Normalizer;
import java.time.*;
import java.util.*;


@Service
public class AgendaService {
    private final AgendaRepository agendaRepository;
    private final DetalleAgendaRepository detalleAgendaRepository;
    private final DiaExcepcionRepository diaExcepcionRepository;
    private final DiaRepository diaRepository;
    private final AgendaMapper mapper;
    private final FestivosService festivosService;
    private final InicioSesionRepository inicioSesionRepository;

    @Value("${agenda.rango-maximo-dias:31}")
    private int rangoMaximoDias;
    /** Cuántos días hacia adelante se construye la agenda. */
    @Value("${agenda.horizonte-dias:30}")
    private int horizonteDias;

    // Ids de la tabla tipousuario (pon los reales: SELECT id, nombre FROM tipousuario;)
    private static final long TIPO_CLIENTE = 4L;
    private static final Long[] TIPOS_ADMINISTRAN_AGENDA = {1L, 2L, 3L};

    public AgendaService(AgendaRepository agendaRepository, DetalleAgendaRepository detalleAgendaRepository,
                         DiaExcepcionRepository diaExcepcionRepository, DiaRepository diaRepository,
                         AgendaMapper mapper, FestivosService festivosService,
                         InicioSesionRepository inicioSesionRepository) {
        this.agendaRepository = agendaRepository;
        this.detalleAgendaRepository = detalleAgendaRepository;
        this.diaExcepcionRepository = diaExcepcionRepository;
        this.diaRepository = diaRepository;
        this.mapper = mapper;
        this.festivosService = festivosService;
        this.inicioSesionRepository = inicioSesionRepository;
    }

    // =====================================================================
    // 1. VER AGENDA NEGOCIO EMPLEADO
    // =====================================================================
    @Transactional(readOnly = true)
    public List<HorarioDisponibleDto> verAgendaNegocioEmpleado(Long idSedeServicio,
                                                               LocalDate fechaDesde,
                                                               LocalDate fechaHasta) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        // Rango: nunca antes de hoy, y con tope máximo
        LocalDate desde = (fechaDesde == null || fechaDesde.isBefore(hoy)) ? hoy : fechaDesde;
        LocalDate hasta = (fechaHasta != null)
                ? fechaHasta
                : desde.plusDays(Math.min(horizonteDias, rangoMaximoDias));

        if (hasta.isBefore(desde)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "fechaHasta no puede ser anterior a fechaDesde");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > rangoMaximoDias) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El rango máximo es de " + rangoMaximoDias + " días");
        }

        List<Agenda> agendas = agendaRepository.findByIdsedeservicioAndActivo(idSedeServicio, true);
        List<Diaexcepcion> excepciones =
                diaExcepcionRepository.findByIdsedeservicioAndFechaBetween(idSedeServicio, desde, hasta);

        Map<LocalDate, Diaexcepcion> excepcionPorFecha = new HashMap<>();
        excepciones.forEach(e -> excepcionPorFecha.putIfAbsent(e.getFecha(), e));
        Map<Long, DayOfWeek> diaSemana = mapaDias();

        // 1) Construir los huecos posibles dentro del rango
        List<HorarioDisponibleDto> candidatos = new ArrayList<>();
        for (LocalDate f = desde; !f.isAfter(hasta); f = f.plusDays(1)) {
            Diaexcepcion ex = excepcionPorFecha.get(f);
            if (ex != null) {
                // Con excepción: manda su horario si está activa; si está inactiva el día queda cerrado
                if (Boolean.TRUE.equals(ex.getActivo())) {
                    candidatos.addAll(generar(f, ex.getHorainicio(), ex.getHorafin(),
                            valor(ex.getRangohora()), null, ex.getId()));
                }
                continue;
            }
            for (Agenda a : agendas) {
                if (diaSemana.get(a.getIddia()) == f.getDayOfWeek()) {
                    candidatos.addAll(generar(f, a.getHorainicio(), a.getHorafin(),
                            minutos(a.getRangohora()), a.getId(), null));
                }
            }
        }

        // 2) Huecos ya tomados (cita en estado distinto de 2)
        Set<String> ocupados = new HashSet<>();
        List<Long> idsAgenda = agendas.stream().map(Agenda::getId).toList();
        if (!idsAgenda.isEmpty()) {
            detalleAgendaRepository
                    .findByIdagendaInAndFechaBetweenAndIdestadocitaNot(idsAgenda, desde, hasta, 2L)
                    .forEach(d -> ocupados.add(clave(d.getIdagenda(), null, d.getFecha(), d.getHora())));
        }
        List<Long> idsExcepcion = excepciones.stream().map(Diaexcepcion::getId).toList();
        if (!idsExcepcion.isEmpty()) {
            detalleAgendaRepository
                    .findByIddiaexcepcionInAndFechaBetweenAndIdestadocitaNot(idsExcepcion, desde, hasta, 2L)
                    .forEach(d -> ocupados.add(clave(null, d.getIddiaexcepcion(), d.getFecha(), d.getHora())));
        }

        // 3) Quitar tomados y horas que ya pasaron hoy
        candidatos.removeIf(h ->
                ocupados.contains(clave(h.idAgenda(), h.idDiaExcepcion(), h.fecha(), h.horaInicio()))
                        || (h.fecha().equals(hoy) && h.horaInicio().isBefore(ahora)));

        candidatos.sort(Comparator.comparing(HorarioDisponibleDto::fecha)
                .thenComparing(HorarioDisponibleDto::horaInicio));
        return candidatos;
    }

    // =====================================================================
    // 2. CONFIGURAR AGENDA SERVICIO EMPLEADO
    // =====================================================================
    @Transactional
    public boolean configurarAgendaServicioEmpleado(ConfigurarAgendaRequest req) {
        // Solo ciertos tipos de usuario pueden configurar la agenda
        if (!tipoPermitido(req.Correo(), TIPOS_ADMINISTRAN_AGENDA)) {
            return false;
        }
        if (!horarioValido(req.idSedeServicio(), req.horaInicio(), req.horaFin(), req.rangoHora())) {
            return false;
        }
        boolean activo = !Boolean.FALSE.equals(req.activo());
        long idAgenda = valor(req.idAgenda());

        // ---- Crear ----
        if (idAgenda == 0) {
            if (req.dias() == null || req.dias().isEmpty()) {
                return false;
            }
            // Se valida todo antes de guardar nada
            for (DiaDto dia : req.dias()) {
                if (dia.id() == null || !diaRepository.existsById(dia.id())) {
                    return false;
                }
                if (activo && agendaRepository.existeSolape(req.idSedeServicio(), dia.id(),
                        req.horaInicio(), req.horaFin(), true, 0L)) {
                    return false;
                }
            }
            List<Agenda> nuevas = req.dias().stream()
                    .map(d -> mapper.aAgenda(req, d.id()))
                    .toList();
            agendaRepository.saveAll(nuevas);
            return true;
        }

        // ---- Modificar ----
        Optional<Agenda> existente = agendaRepository.findById(idAgenda);
        if (existente.isEmpty()) {
            return false;
        }
        if (detalleAgendaRepository.existsByIdagendaAndIdestadocitaNot(idAgenda, 2L)) {
            return false;
        }
        Agenda agenda = existente.get();
        Long idDia = agenda.getIddia();
        if (req.dias() != null && !req.dias().isEmpty()) {
            if (req.dias().size() > 1) {
                return false;
            }
            idDia = req.dias().get(0).id();
            if (idDia == null || !diaRepository.existsById(idDia)) {
                return false;
            }
        }
        if (activo && agendaRepository.existeSolape(req.idSedeServicio(), idDia,
                req.horaInicio(), req.horaFin(), true, idAgenda)) {
            return false;
        }
        mapper.aplicar(agenda, req, idDia);
        agendaRepository.save(agenda);
        return true;
    }

    // =====================================================================
    // 3. CONFIGURAR DÍA FESTIVO (día de excepción)
    // =====================================================================
    @Transactional
    public boolean configurarDiaFestivo(ConfigurarDiaFestivoRequest req) {
        // Solo ciertos tipos de usuario pueden configurar festivos
        if (!tipoPermitido(req.Correo(), TIPOS_ADMINISTRAN_AGENDA)) {
            return false;
        }
        if (req.fecha() == null
                || !horarioValido(req.idSedeServicio(), req.horaInicio(), req.horaFin(), req.rangoHora())) {
            return false;
        }
        // La fecha debe ser un festivo oficial del país de la sede
        String pais = festivosService.paisDeSedeServicio(req.idSedeServicio());
        if (!festivosService.esFestivo(pais, req.fecha())) {
            return false;
        }
        long idDiaFestivo = valor(req.idDiaFestivo());

        // ---- Crear ----
        if (idDiaFestivo == 0) {
            if (diaExcepcionRepository.existsByIdsedeservicioAndFecha(req.idSedeServicio(), req.fecha())) {
                return false;
            }
            diaExcepcionRepository.save(mapper.aDiaExcepcion(req));
            return true;
        }

        // ---- Modificar ----
        Optional<Diaexcepcion> existente = diaExcepcionRepository.findById(idDiaFestivo);
        if (existente.isEmpty()) {
            return false;
        }
        if (detalleAgendaRepository.existsByIddiaexcepcionAndIdestadocitaNot(idDiaFestivo, 2L)) {
            return false;
        }
        if (diaExcepcionRepository.existsByIdsedeservicioAndFechaAndIdNot(
                req.idSedeServicio(), req.fecha(), idDiaFestivo)) {
            return false;
        }
        Diaexcepcion dia = existente.get();
        mapper.aplicar(dia, req);
        diaExcepcionRepository.save(dia);
        return true;
    }

    // =====================================================================
    // 4. MODIFICAR CITA USUARIO
    // =====================================================================
    @Transactional
    public ResultadoCitaDto modificarCitaUsuario(ModificarCitaRequest req) {
        LocalDateTime ahora = LocalDateTime.now();
        if (valor(req.idDetalleAgenda()) == 0) {
            return crearCita(req, ahora);
        }
        return liberarCita(req.idDetalleAgenda(), ahora);
    }

    @Transactional(readOnly = true)
    public List<CitaUsuarioDto> listarCitasReservadasUsuario(ListarCitasUsuarioRequest req) {
        if (req.fechaDesde() != null && req.fechaHasta() != null
                && req.fechaHasta().isBefore(req.fechaDesde())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "fechaHasta no puede ser anterior a fechaDesde");
        }
        if (!inicioSesionRepository.existsById(Math.toIntExact(req.idIniciosesion()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe");
        }

        // Citas reservadas (estado 3), con el rango opcional
        List<Detalleagenda> citas = detalleAgendaRepository
                .findByIdiniciosesionAndIdestadocitaOrderByFechaAscHoraAsc(req.idIniciosesion(), 3L)
                .stream()
                .filter(c -> req.fechaDesde() == null || !c.getFecha().isBefore(req.fechaDesde()))
                .filter(c -> req.fechaHasta() == null || !c.getFecha().isAfter(req.fechaHasta()))
                .toList();
        if (citas.isEmpty()) return List.of();

        // Se traen las agendas y excepciones de una vez (evita una consulta por cada cita)
        Map<Long, Agenda> agendas = agendaRepository.findAllById(
                        citas.stream().map(Detalleagenda::getIdagenda)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Agenda::getId, a -> a));
        Map<Long, Diaexcepcion> excepciones = diaExcepcionRepository.findAllById(
                        citas.stream().map(Detalleagenda::getIddiaexcepcion)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Diaexcepcion::getId, e -> e));

        List<CitaUsuarioDto> resultado = new ArrayList<>();
        for (Detalleagenda c : citas) {
            Long idSedeServicio = null;
            long minutos = 0;
            if (c.getIdagenda() != null && agendas.containsKey(c.getIdagenda())) {
                Agenda a = agendas.get(c.getIdagenda());
                idSedeServicio = a.getIdsedeservicio();
                minutos = minutos(a.getRangohora());
            } else if (c.getIddiaexcepcion() != null && excepciones.containsKey(c.getIddiaexcepcion())) {
                Diaexcepcion e = excepciones.get(c.getIddiaexcepcion());
                idSedeServicio = e.getIdsedeservicio();
                minutos = valor(e.getRangohora());
            }
            resultado.add(mapper.aCitaUsuario(c, idSedeServicio, minutos));
        }
        return resultado;
    }

    private ResultadoCitaDto crearCita(ModificarCitaRequest req, LocalDateTime ahora) {
        long idAgenda = valor(req.idAgenda());
        long idExcepcion = valor(req.idDiaExcepcion());

        if ((idAgenda == 0) == (idExcepcion == 0)) {
            return mapper.error("Debe enviar idAgenda o idDiaExcepcion (solo uno de los dos)");
        }
        if (req.fecha() == null || req.hora() == null) {
            return mapper.error("La fecha y la hora de la cita son obligatorias");
        }
        // La fecha y la hora no pueden haber pasado (si es hoy, la hora debe ser posterior a la actual)
        if (LocalDateTime.of(req.fecha(), req.hora()).isBefore(ahora)) {
            return mapper.error("No se puede agendar en un horario que ya pasó");
        }

        // El inicio de sesión debe existir y ser de tipo cliente
        Optional<InicioSesion> sesion = valor(req.idIniciosesion()) == 0
                ? Optional.empty()
                : inicioSesionRepository.findById(Math.toIntExact(req.idIniciosesion()));
        if (sesion.isEmpty()) {
            return mapper.error("El usuario no existe");
        }
        if (tipoDe(sesion.get()) != TIPO_CLIENTE) {
            return mapper.error("Solo un usuario de tipo cliente puede tener citas");
        }

        // El horario debe ser un hueco real de la agenda o del día de excepción
        Optional<HorarioDisponibleDto> hueco = buscarHueco(idAgenda, idExcepcion, req.fecha(), req.hora());
        if (hueco.isEmpty()) {
            return mapper.error("El horario no corresponde a la agenda o al día de excepción indicado");
        }

        // El hueco no debe estar tomado por otra persona
        boolean ocupado = idAgenda != 0
                ? detalleAgendaRepository.existsByIdagendaAndFechaAndHoraAndIdestadocitaNot(
                idAgenda, req.fecha(), req.hora(), 2L)
                : detalleAgendaRepository.existsByIddiaexcepcionAndFechaAndHoraAndIdestadocitaNot(
                idExcepcion, req.fecha(), req.hora(), 2L);
        if (ocupado) {
            return mapper.error("Ya hay una cita reservada en ese horario");
        }

        // El usuario no puede tener otra cita que se cruce (todas las empresas)
        if (usuarioTieneCruce(req.idIniciosesion(), req.fecha(),
                hueco.get().horaInicio(), hueco.get().horaFin())) {
            return mapper.error("El usuario ya tiene una cita que se cruza con ese horario");
        }

        Detalleagenda guardada = detalleAgendaRepository.save(mapper.aDetalle(req, ahora));
        return mapper.exito(guardada.getId(), "Cita creada");
    }

    /** Pasa la cita de estado 3 a 2 y actualiza la fecha de modificación. */
    private ResultadoCitaDto liberarCita(Long idDetalleAgenda, LocalDateTime ahora) {
        Optional<Detalleagenda> opt = detalleAgendaRepository.findById(idDetalleAgenda);
        if (opt.isEmpty()) {
            return mapper.error("La cita no existe");
        }
        Detalleagenda cita = opt.get();
        if (valor(cita.getIdestadocita()) != 3L) {
            return mapper.error("Solo se pueden modificar citas en estado 3");
        }
        cita.setIdestadocita(2L);
        cita.setFechamodificacion(ahora);
        detalleAgendaRepository.save(cita);
        return mapper.exito(cita.getId(), "Cita modificada");
    }

    // =====================================================================
    // Utilidades privadas
    // =====================================================================

    private List<HorarioDisponibleDto> generar(LocalDate fecha, LocalTime inicio, LocalTime fin,
                                               long rangoMin, Long idAgenda, Long idExcepcion) {
        List<HorarioDisponibleDto> huecos = new ArrayList<>();
        if (inicio == null || fin == null || rangoMin <= 0) return huecos;
        for (LocalTime t = inicio; Duration.between(t, fin).toMinutes() >= rangoMin; t = t.plusMinutes(rangoMin)) {
            huecos.add(mapper.aHorario(fecha, t, rangoMin, idAgenda, idExcepcion));
        }
        return huecos;
    }

    private boolean horarioValido(Long idSedeServicio, LocalTime inicio, LocalTime fin, Integer rango) {
        return idSedeServicio != null && idSedeServicio > 0
                && inicio != null && fin != null && rango != null
                && rango > 0 && rango < 1440
                && inicio.isBefore(fin);
    }

    /** Busca el hueco exacto (fecha y hora) dentro de la agenda o del día de excepción. */
    private Optional<HorarioDisponibleDto> buscarHueco(long idAgenda, long idExcepcion,
                                                       LocalDate fecha, LocalTime hora) {
        List<HorarioDisponibleDto> huecos;
        if (idAgenda != 0) {
            Optional<Agenda> opt = agendaRepository.findById(idAgenda);
            if (opt.isEmpty() || !Boolean.TRUE.equals(opt.get().getActivo())) return Optional.empty();
            Agenda a = opt.get();
            // Si ese día hay excepción para el servicio, la agenda normal no aplica
            if (diaExcepcionRepository.existsByIdsedeservicioAndFecha(a.getIdsedeservicio(), fecha)) return Optional.empty();
            if (mapaDias().get(a.getIddia()) != fecha.getDayOfWeek()) return Optional.empty();
            huecos = generar(fecha, a.getHorainicio(), a.getHorafin(), minutos(a.getRangohora()), a.getId(), null);
        } else {
            Optional<Diaexcepcion> opt = diaExcepcionRepository.findById(idExcepcion);
            if (opt.isEmpty() || !Boolean.TRUE.equals(opt.get().getActivo())) return Optional.empty();
            Diaexcepcion e = opt.get();
            if (!fecha.equals(e.getFecha())) return Optional.empty();
            huecos = generar(fecha, e.getHorainicio(), e.getHorafin(), valor(e.getRangohora()), null, e.getId());
        }
        return huecos.stream().filter(h -> h.horaInicio().equals(hora)).findFirst();
    }

    /** ¿El usuario ya tiene una cita viva ese día cuyo rango se cruza con el nuevo? */
    private boolean usuarioTieneCruce(Long idIniciosesion, LocalDate fecha,
                                      LocalTime inicioNueva, LocalTime finNueva) {
        List<Detalleagenda> citas = detalleAgendaRepository
                .findByIdiniciosesionAndFechaAndIdestadocitaNot(idIniciosesion, fecha, 2L);
        for (Detalleagenda c : citas) {
            long minutos;
            if (c.getIdagenda() != null) {
                minutos = agendaRepository.findById(c.getIdagenda())
                        .map(a -> minutos(a.getRangohora())).orElse(0L);
            } else if (c.getIddiaexcepcion() != null) {
                minutos = diaExcepcionRepository.findById(c.getIddiaexcepcion())
                        .map(e -> valor(e.getRangohora())).orElse(0L);
            } else {
                continue;
            }
            LocalTime inicio = c.getHora();
            LocalTime fin = inicio.plusMinutes(minutos);
            if (inicioNueva.isBefore(fin) && finNueva.isAfter(inicio)) return true;
        }
        return false;
    }

    /** id del día -> día de la semana, leyendo el nombre desde la tabla dia (Lunes, Martes, ...). */
    private Map<Long, DayOfWeek> mapaDias() {
        Map<Long, DayOfWeek> mapa = new HashMap<>();
        for (Dia d : diaRepository.findAll()) {
            DayOfWeek dow = aDiaSemana(d.getNombre());
            if (dow != null) mapa.put(d.getId(), dow);
        }
        return mapa;
    }

    private DayOfWeek aDiaSemana(String nombre) {
        if (nombre == null) return null;
        // Se quitan las tildes, por eso los nombres de abajo van sin tilde
        String n = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
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

    private String clave(Long idAgenda, Long idExcepcion, LocalDate fecha, LocalTime hora) {
        return (idAgenda != null ? "A" + idAgenda : "E" + idExcepcion) + "|" + fecha + "|" + hora;
    }

    private long minutos(LocalTime rango) {
        return rango == null ? 0 : rango.getHour() * 60L + rango.getMinute();
    }

    private long valor(Long v) {
        return v == null ? 0L : v;
    }

    // ---------------------------------------------------------------------
    // Tipo de usuario (se obtiene desde InicioSesion)
    // ---------------------------------------------------------------------

    /**
     * Id del tipo de usuario de un inicio de sesión.
     * AJUSTA ESTA LÍNEA al getter real de tu clase InicioSesion
     * (escribe "is.get" y presiona Ctrl+Espacio para ver los que existen).
     */
    private long tipoDe(InicioSesion is) {
        TipoUsuario tipo = is.getTipoUsuario();   // <-- cambia por el getter real
        return tipo == null ? 0L : tipo.getId();
    }

    /** ¿El usuario autenticado (por correo) es de uno de los tipos permitidos? */
    private boolean tipoPermitido(String correo, Long... permitidos) {
        return inicioSesionRepository.findByCorreoElectronico(correo)
                .map(is -> Arrays.asList(permitidos).contains(tipoDe(is)))
                .orElse(false);
    }
}
