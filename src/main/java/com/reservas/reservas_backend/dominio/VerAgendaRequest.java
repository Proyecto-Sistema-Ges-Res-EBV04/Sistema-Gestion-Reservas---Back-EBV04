package com.reservas.reservas_backend.dominio;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VerAgendaRequest(
        @NotNull Long idSedeServicio,
        LocalDate fechaDesde,    // opcional: si no viene, empieza hoy
        LocalDate fechaHasta) {} // opcional: si no viene, se usa el horizonte por defecto