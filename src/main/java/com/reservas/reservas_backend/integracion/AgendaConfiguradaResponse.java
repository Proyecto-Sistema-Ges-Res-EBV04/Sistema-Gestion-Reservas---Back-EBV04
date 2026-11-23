package com.reservas.reservas_backend.integracion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Consultar agenda configurada (vista de la Empresa).
// - semana: los 7 días con sus rangos de atención (o atiende = false).
// - calendario: cada fecha del rango consultado con su estado, para pintar un calendario.
// - fechasNoDisponibles: atajo con solo las fechas bloqueadas (festivos / días cerrados).
public record AgendaConfiguradaResponse(
        Integer idServicio,
        String servicio,
        Integer idSede,
        String sede,
        Long idSedeServicio,
        boolean configurada,
        String mensaje,
        LocalDate desde,
        LocalDate hasta,
        List<DiaSemanaAtencion> semana,
        List<FechaAgenda> calendario,
        List<FechaAgenda> fechasNoDisponibles) {

    public enum EstadoFecha {
        ATENCION,          // día con agenda normal
        HORARIO_ESPECIAL,  // día de excepción activo: aplica su propio horario
        NO_DISPONIBLE,     // día de excepción inactivo: fecha bloqueada
        SIN_ATENCION       // ese día de la semana no tiene agenda
    }

    public record RangoHorario(Long idAgenda,
                               Long idDiaExcepcion,
                               LocalTime horaInicio,
                               LocalTime horaFin,
                               long duracionMinutos) {
    }

    public record DiaSemanaAtencion(Long idDia,
                                    String dia,
                                    boolean atiende,
                                    List<RangoHorario> rangos) {
    }

    public record FechaAgenda(LocalDate fecha,
                              String dia,
                              EstadoFecha estado,
                              String detalle,
                              List<RangoHorario> rangos) {
    }
}