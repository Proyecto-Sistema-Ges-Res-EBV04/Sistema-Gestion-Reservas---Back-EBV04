package com.reservas.reservas_backend.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

/** Un hueco libre. Viene de una agenda (idAgenda) o de un día de excepción (idDiaExcepcion), nunca de ambos. */
public record HorarioDisponibleDto(
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        int duracionMinutos,
        Long idAgenda,
        Long idDiaExcepcion) {}
