package com.reservas.reservas_backend.dominio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record ConfigurarDiaFestivoRequest(
        Long idDiaFestivo,             // 0 o null = crear
        @NotNull Long idSedeServicio,
        @NotNull LocalDate fecha,
        Boolean activo,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        @NotNull @Positive Integer rangoHora,
        String Correo) {}   // en minutos
