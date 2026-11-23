package com.reservas.reservas_backend.integracion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// HU-21: horarios con cupos de un servicio en una sede para una fecha (RN1).
// Solo informa: no crea citas ni aparta cupos (RN7).
public record DisponibilidadResponse(
        Integer idServicio,
        Integer idSede,
        LocalDate fecha,
        int duracionMinutos,
        boolean disponible,
        // RN5/RN6: motivo cuando no hay horarios para la selección; null si los hay.
        String mensaje,
        List<HorarioDisponible> horarios) {

    // Un horario reservable con los cupos que quedan libres (RN3/RN4).
    public record HorarioDisponible(
            LocalTime horaInicio,
            LocalTime horaFin,
            int cuposDisponibles) {
    }
}
