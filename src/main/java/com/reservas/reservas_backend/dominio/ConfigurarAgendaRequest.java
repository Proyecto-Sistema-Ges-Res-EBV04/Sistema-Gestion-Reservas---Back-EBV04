package com.reservas.reservas_backend.dominio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.List;

public record ConfigurarAgendaRequest(
        Long idAgenda,                 // 0 o null = crear
        @NotNull Long idSedeServicio,
        List<DiaDto> dias,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        @NotNull @Positive Integer rangoHora,   // en minutos
        Boolean activo) {}
