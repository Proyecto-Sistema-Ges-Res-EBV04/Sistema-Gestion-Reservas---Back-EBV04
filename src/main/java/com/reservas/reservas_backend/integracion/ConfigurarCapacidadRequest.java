package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.DiaSemana;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

// HU-16: Configurar capacidad de atención para una combinación servicio+sede+horario.
@Data
public class ConfigurarCapacidadRequest {

    @NotEmpty(message = "Debes seleccionar al menos un día de la semana")
    private Set<DiaSemana> diasSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    // RN3: entero mayor o igual a 1.
    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser un número entero mayor o igual a 1")
    private Integer capacidad;

    @Valid
    private List<RecursoAsignado> recursos;

    @Valid
    private List<EmpleadoLimite> empleados;

    @Data
    public static class RecursoAsignado {
        @NotNull(message = "El id del recurso es obligatorio")
        private Integer idRecurso;

        // RN7: opcional, no todo recurso necesita un empleado responsable.
        private Integer idEmpleado;
    }

    @Data
    public static class EmpleadoLimite {
        @NotNull(message = "El id del empleado es obligatorio")
        private Integer idEmpleado;

        // RN8: máximo de recursos/unidades simultáneas que puede atender.
        @NotNull(message = "El límite simultáneo del empleado es obligatorio")
        @Positive(message = "El límite simultáneo del empleado debe ser mayor o igual a 1")
        private Integer limiteSimultaneo;
    }
}
