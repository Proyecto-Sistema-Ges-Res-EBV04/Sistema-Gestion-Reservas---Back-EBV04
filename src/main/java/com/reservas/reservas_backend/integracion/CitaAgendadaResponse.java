package com.reservas.reservas_backend.integracion;

import java.time.LocalDate;
import java.time.LocalTime;

// HU-22 (RN9). En la previsualización id y estado son null.
public record CitaAgendadaResponse(
        Integer id,
        String estado,
        String empresa,
        String servicio,
        String sede,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer duracion,
        String unidadDuracion,
        String empleado,
        String espacio) {
}
