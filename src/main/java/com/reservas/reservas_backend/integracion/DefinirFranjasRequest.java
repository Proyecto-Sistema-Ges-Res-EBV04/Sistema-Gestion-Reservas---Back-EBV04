package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.DiaSemana;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

// HU-16 (RN1): define las franjas de atención de un servicio en una sede.
// Cada día puede tener uno o varios rangos horarios.
@Data
public class DefinirFranjasRequest {

    @NotEmpty(message = "Debes definir al menos una franja de atención")
    @Valid
    private List<FranjaNueva> franjas;

    @Data
    public static class FranjaNueva {
        @NotNull(message = "El día de la semana de la franja es obligatorio")
        private DiaSemana diaSemana;

        @NotNull(message = "La hora de inicio es obligatoria")
        private LocalTime horaInicio;

        @NotNull(message = "La hora de fin es obligatoria")
        private LocalTime horaFin;
    }
}
