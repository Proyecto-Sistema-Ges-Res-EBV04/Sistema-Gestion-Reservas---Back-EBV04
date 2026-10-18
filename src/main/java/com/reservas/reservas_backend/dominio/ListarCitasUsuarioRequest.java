package com.reservas.reservas_backend.dominio;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ListarCitasUsuarioRequest(
        @NotNull Long idIniciosesion,
        LocalDate fechaDesde,    // opcional
        LocalDate fechaHasta) {} // opcional