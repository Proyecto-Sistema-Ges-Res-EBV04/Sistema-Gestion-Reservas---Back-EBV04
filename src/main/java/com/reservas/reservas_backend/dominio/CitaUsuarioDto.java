package com.reservas.reservas_backend.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaUsuarioDto(
        Long idDetalleAgenda,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        int duracionMinutos,
        String descripcion,
        Long idEstadoCita,
        Long idAgenda,
        Long idDiaExcepcion,
        Long idSedeServicio) {}