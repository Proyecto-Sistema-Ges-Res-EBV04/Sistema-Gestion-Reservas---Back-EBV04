package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

// HU-22: datos que elige el Usuario para previsualizar o agendar una cita.
public record AgendarCitaRequest(
        @NotNull(message = "El servicio es obligatorio") Integer idServicio,
        @NotNull(message = "La sede es obligatoria") Integer idSede,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "La hora es obligatoria") LocalTime hora) {
}
