package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import com.reservas.reservas_backend.dominio.CapacidadEmpleado;
import com.reservas.reservas_backend.dominio.CapacidadRecurso;
import com.reservas.reservas_backend.dominio.DiaSemana;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CapacidadResponse {

    private Integer id;
    private Integer idServicio;
    private Integer idSede;
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer capacidad;
    private List<RecursoAsignadoResponse> recursos;
    private List<EmpleadoLimiteResponse> empleados;

    public static CapacidadResponse desde(CapacidadAtencion capacidadAtencion,
                                           List<CapacidadRecurso> capacidadRecursos,
                                           List<CapacidadEmpleado> capacidadEmpleados) {
        List<RecursoAsignadoResponse> recursos = new ArrayList<>();
        for (CapacidadRecurso capacidadRecurso : capacidadRecursos) {
            recursos.add(new RecursoAsignadoResponse(
                    capacidadRecurso.getRecurso().getId(),
                    capacidadRecurso.getRecurso().getIdentificador(),
                    capacidadRecurso.getEmpleado() != null ? capacidadRecurso.getEmpleado().getId() : null
            ));
        }

        List<EmpleadoLimiteResponse> empleados = new ArrayList<>();
        for (CapacidadEmpleado capacidadEmpleado : capacidadEmpleados) {
            empleados.add(new EmpleadoLimiteResponse(
                    capacidadEmpleado.getEmpleado().getId(),
                    capacidadEmpleado.getEmpleado().getNombre(),
                    capacidadEmpleado.getLimiteSimultaneo()
            ));
        }

        return new CapacidadResponse(
                capacidadAtencion.getId(),
                capacidadAtencion.getServicio().getId(),
                capacidadAtencion.getSede().getId(),
                capacidadAtencion.getDiaSemana(),
                capacidadAtencion.getHoraInicio(),
                capacidadAtencion.getHoraFin(),
                capacidadAtencion.getCapacidad(),
                recursos,
                empleados
        );
    }

    @Data
    @AllArgsConstructor
    public static class RecursoAsignadoResponse {
        private Integer idRecurso;
        private String identificador;
        private Integer idEmpleadoResponsable;
    }

    @Data
    @AllArgsConstructor
    public static class EmpleadoLimiteResponse {
        private Integer idEmpleado;
        private String nombre;
        private Integer limiteSimultaneo;
    }
}
