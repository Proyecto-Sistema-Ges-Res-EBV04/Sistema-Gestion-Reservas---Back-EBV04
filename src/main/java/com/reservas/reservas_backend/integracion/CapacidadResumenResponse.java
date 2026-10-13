package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.DiaSemana;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

// HU-16 (RN13): resumen de la configuración de capacidad. Muestra la capacidad calculada
// de cada franja, qué personal y espacios participan en ella, y las franjas pendientes (RN9).
@Data
@AllArgsConstructor
public class CapacidadResumenResponse {

    private Integer idServicio;
    private Integer idSede;
    // true cuando todas las franjas tienen capacidad mínima de una atención (RN9).
    private boolean completa;
    private List<FranjaCapacidad> franjas;
    private List<EmpleadoCapacidad> empleados;
    private List<FranjaResponse> franjasPendientes;

    @Data
    @AllArgsConstructor
    public static class FranjaCapacidad {
        private Integer idFranja;
        private DiaSemana diaSemana;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        // RN7: suma de los espacios de los empleados que tienen esta franja asignada.
        private Integer capacidad;
        private boolean pendiente;
        private List<ParticipanteFranja> personal;
    }

    @Data
    @AllArgsConstructor
    public static class ParticipanteFranja {
        private Integer idEmpleado;
        private String nombre;
        private List<RecursoResponse> espacios;
    }

    @Data
    @AllArgsConstructor
    public static class EmpleadoCapacidad {
        // null en la previsualización si es personal nuevo que aún no se ha guardado (RN2).
        private Integer idEmpleado;
        private String nombre;
        private String cargo;
        // RN6: cantidad de espacios asignados (0 si no tiene horarios, RN12).
        private Integer capacidadIndividual;
        private List<FranjaResponse> horarios;
        private List<RecursoResponse> espacios;
    }
}
