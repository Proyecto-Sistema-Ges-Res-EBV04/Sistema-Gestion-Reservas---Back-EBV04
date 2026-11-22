package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.*;
import com.reservas.reservas_backend.infraestructura.*;
import com.reservas.reservas_backend.integracion.ConsultaDisponibilidadInvalidaException;
import com.reservas.reservas_backend.integracion.DisponibilidadResponse;
import com.reservas.reservas_backend.integracion.DisponibilidadResponse.HorarioDisponible;
import com.reservas.reservas_backend.integracion.ServicioNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// HU-21: escenarios de aceptación de "Consultar disponibilidad".
class DisponibilidadServiceTest {

    private static final int ID_SERVICIO = 1;
    private static final int ID_SEDE = 2;
    private static final int ID_SERVICIO_SEDE = 5;
    // Un lunes futuro: la franja de prueba es de LUNES.
    private static final LocalDate LUNES = siguiente(DayOfWeek.MONDAY);

    private BusquedaServicioRepository busquedaRepository;
    private CapacidadAtencionRepository capacidadRepository;
    private HorarioEmpleadoRepository horarioRepository;
    private EspacioEmpleadoRepository espacioRepository;
    private CitaAgendamientoRepository citaRepository;
    private AgendaRepository agendaRepository;
    private DiaExcepcionRepository diaExcepcionRepository;
    private DiaRepository diaRepository;
    private DisponibilidadService service;

    private Servicio servicio;
    private Sede sede;
    private CapacidadAtencion franja;
    private final List<Cita> citas = new ArrayList<>();

    @BeforeEach
    void preparar() {
        busquedaRepository = mock(BusquedaServicioRepository.class);
        capacidadRepository = mock(CapacidadAtencionRepository.class);
        horarioRepository = mock(HorarioEmpleadoRepository.class);
        espacioRepository = mock(EspacioEmpleadoRepository.class);
        citaRepository = mock(CitaAgendamientoRepository.class);
        agendaRepository = mock(AgendaRepository.class);
        diaExcepcionRepository = mock(DiaExcepcionRepository.class);
        diaRepository = mock(DiaRepository.class);
        service = new DisponibilidadService(busquedaRepository, capacidadRepository, horarioRepository,
                espacioRepository, citaRepository, agendaRepository, diaExcepcionRepository, diaRepository);

        Empresa empresa = new Empresa();
        empresa.setId(10);
        empresa.setRegistroCompleto(true);

        servicio = new Servicio();
        servicio.setId(ID_SERVICIO);
        servicio.setEmpresa(empresa);
        servicio.setDuracion(30);
        servicio.setUnidadDuracion(UnidadDuracion.MINUTOS);

        sede = new Sede();
        sede.setId(ID_SEDE);
        sede.setActiva(true);

        ServicioSede ss = new ServicioSede();
        ss.setId(ID_SERVICIO_SEDE);
        ss.setServicio(servicio);
        ss.setSede(sede);
        when(busquedaRepository.ofertaPublica(ID_SERVICIO)).thenReturn(List.of(ss));

        // Franja LUNES 08:00-10:00 con un empleado que atiende 2 espacios -> cupo 2
        franja = new CapacidadAtencion();
        franja.setId(100);
        franja.setServicio(servicio);
        franja.setSede(sede);
        franja.setDiaSemana(DiaSemana.LUNES);
        franja.setHoraInicio(LocalTime.of(8, 0));
        franja.setHoraFin(LocalTime.of(10, 0));
        franja.setCapacidad(2);
        when(capacidadRepository.findByServicio_IdAndSede_Id(ID_SERVICIO, ID_SEDE)).thenReturn(List.of(franja));

        Empleado empleado = new Empleado();
        empleado.setId(7);
        HorarioEmpleado horario = new HorarioEmpleado();
        horario.setFranja(franja);
        horario.setEmpleado(empleado);
        when(horarioRepository.findByFranja_Servicio_IdAndFranja_Sede_Id(ID_SERVICIO, ID_SEDE))
                .thenReturn(List.of(horario));

        EspacioEmpleado e1 = new EspacioEmpleado();
        e1.setEmpleado(empleado);
        EspacioEmpleado e2 = new EspacioEmpleado();
        e2.setEmpleado(empleado);
        when(espacioRepository.findByEmpleado_Servicio_IdAndEmpleado_Sede_Id(ID_SERVICIO, ID_SEDE))
                .thenReturn(List.of(e1, e2));

        when(citaRepository.findByServicio_IdAndSede_IdAndFechaAndEstado(
                eq(ID_SERVICIO), eq(ID_SEDE), any(), eq(EstadoCita.PROGRAMADA))).thenReturn(citas);
        when(diaExcepcionRepository.findByIdsedeservicioAndFechaBetween(anyLong(), any(), any())).thenReturn(List.of());
        when(agendaRepository.findByIdsedeservicioAndActivo(anyLong(), eq(true))).thenReturn(List.of());
        when(diaRepository.findAll()).thenReturn(List.of(dia(1L, "Lunes"), dia(3L, "Miércoles")));
    }

    @Test
    void consultarFechasYHorariosDisponibles() {
        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertTrue(r.disponible());
        assertNull(r.mensaje());
        assertEquals(30, r.duracionMinutos());
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30)),
                r.horarios().stream().map(HorarioDisponible::horaInicio).toList());
        assertTrue(r.horarios().stream().allMatch(h -> h.cuposDisponibles() == 2));
        assertEquals(LocalTime.of(10, 0), r.horarios().get(3).horaFin());
    }

    @Test
    void capacidadAgotadaNoSePresentaYLasOcupadasDescuentanCupos() {
        citas.add(cita(LocalTime.of(8, 0)));
        citas.add(cita(LocalTime.of(8, 0)));
        citas.add(cita(LocalTime.of(9, 0)));

        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertFalse(r.horarios().stream().anyMatch(h -> h.horaInicio().equals(LocalTime.of(8, 0))));
        HorarioDisponible nueve = r.horarios().stream()
                .filter(h -> h.horaInicio().equals(LocalTime.of(9, 0))).findFirst().orElseThrow();
        assertEquals(1, nueve.cuposDisponibles());
    }

    @Test
    void todoOcupadoInformaQueNoHayHorarios() {
        for (LocalTime t = LocalTime.of(8, 0); t.isBefore(LocalTime.of(10, 0)); t = t.plusMinutes(30)) {
            citas.add(cita(t));
            citas.add(cita(t));
        }
        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertFalse(r.disponible());
        assertTrue(r.horarios().isEmpty());
        assertNotNull(r.mensaje());
    }

    @Test
    void fechaFueraDeLosDiasDeAtencionDeLasFranjas() {
        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES.plusDays(1));

        assertFalse(r.disponible());
        assertEquals("No hay franjas habilitadas para reservar en la fecha seleccionada", r.mensaje());
    }

    @Test
    void fechaMarcadaComoNoDisponibleEnLaAgenda() {
        Diaexcepcion cerrado = new Diaexcepcion();
        cerrado.setFecha(LUNES);
        cerrado.setActivo(false);
        when(diaExcepcionRepository.findByIdsedeservicioAndFechaBetween(5L, LUNES, LUNES))
                .thenReturn(List.of(cerrado));

        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertFalse(r.disponible());
        assertEquals("La fecha seleccionada no está habilitada para reservar", r.mensaje());
    }

    @Test
    void diaExcepcionActivoLimitaLosHorarios() {
        Diaexcepcion especial = new Diaexcepcion();
        especial.setFecha(LUNES);
        especial.setActivo(true);
        especial.setHorainicio(LocalTime.of(9, 0));
        especial.setHorafin(LocalTime.of(12, 0));
        when(diaExcepcionRepository.findByIdsedeservicioAndFechaBetween(5L, LUNES, LUNES))
                .thenReturn(List.of(especial));

        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(9, 30)),
                r.horarios().stream().map(HorarioDisponible::horaInicio).toList());
    }

    @Test
    void agendaSemanalLimitaRangosYDias() {
        when(agendaRepository.findByIdsedeservicioAndActivo(5L, true))
                .thenReturn(List.of(agenda(1L, LocalTime.of(8, 0), LocalTime.of(9, 0))));

        DisponibilidadResponse lunes = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(8, 30)),
                lunes.horarios().stream().map(HorarioDisponible::horaInicio).toList());

        // La agenda no atiende miércoles aunque existiera franja ese día
        when(agendaRepository.findByIdsedeservicioAndActivo(5L, true))
                .thenReturn(List.of(agenda(3L, LocalTime.of(8, 0), LocalTime.of(9, 0))));
        DisponibilidadResponse otro = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);
        assertFalse(otro.disponible());
        assertEquals("La fecha seleccionada no está habilitada para reservar", otro.mensaje());
    }

    @Test
    void configuracionIncompletaSinFranjas() {
        when(capacidadRepository.findByServicio_IdAndSede_Id(ID_SERVICIO, ID_SEDE)).thenReturn(List.of());

        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertFalse(r.disponible());
        assertEquals("El servicio aún no tiene agenda o capacidad configurada en esta sede", r.mensaje());
    }

    @Test
    void configuracionIncompletaSinPersonalConEspacios() {
        when(espacioRepository.findByEmpleado_Servicio_IdAndEmpleado_Sede_Id(ID_SERVICIO, ID_SEDE))
                .thenReturn(List.of());

        DisponibilidadResponse r = service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        assertFalse(r.disponible());
        assertTrue(r.horarios().isEmpty());
    }

    @Test
    void consultarNoCreaCitas() {
        service.consultar(ID_SERVICIO, ID_SEDE, LUNES);

        verify(citaRepository, never()).save(any());
        verify(citaRepository, never()).saveAll(any());
    }

    @Test
    void servicioQueNoSeOfreceEnLaSede() {
        assertThrows(ServicioNoEncontradoException.class,
                () -> service.consultar(ID_SERVICIO, 99, LUNES));
    }

    @Test
    void fechaObligatoriaYNoPasada() {
        assertThrows(ConsultaDisponibilidadInvalidaException.class,
                () -> service.consultar(ID_SERVICIO, ID_SEDE, null));
        assertThrows(ConsultaDisponibilidadInvalidaException.class,
                () -> service.consultar(ID_SERVICIO, ID_SEDE, LocalDate.now().minusDays(1)));
    }

    private Cita cita(LocalTime hora) {
        Cita c = new Cita();
        c.setHoraInicio(hora);
        c.setEstado(EstadoCita.PROGRAMADA);
        return c;
    }

    private static Dia dia(Long id, String nombre) {
        Dia d = new Dia();
        d.setId(id);
        d.setNombre(nombre);
        return d;
    }

    private static Agenda agenda(Long idDia, LocalTime inicio, LocalTime fin) {
        Agenda a = new Agenda();
        a.setIddia(idDia);
        a.setHorainicio(inicio);
        a.setHorafin(fin);
        a.setActivo(true);
        a.setIdsedeservicio(5L);
        return a;
    }

    private static LocalDate siguiente(DayOfWeek dia) {
        LocalDate f = LocalDate.now().plusDays(1);
        while (f.getDayOfWeek() != dia) {
            f = f.plusDays(1);
        }
        return f;
    }
}
