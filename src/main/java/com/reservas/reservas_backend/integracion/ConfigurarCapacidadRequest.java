package com.reservas.reservas_backend.integracion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

// HU-16: asignación de horarios (franjas) y espacios (recursos) al personal de un servicio en una sede.
// RN8: no hay campo de capacidad; se calcula a partir de estas asignaciones.
@Data
public class ConfigurarCapacidadRequest {

    @NotNull(message = "Debes enviar el personal con sus horarios y espacios")
    @Valid
    private List<AsignacionEmpleado> empleados;

    @Data
    public static class AsignacionEmpleado {
        // RN2: empleado ya asociado al servicio y la sede...
        private Integer idEmpleado;

        // ...o personal adicional agregado durante el flujo (mínimo nombre y cargo o función).
        private String nombre;
        private String cargo;

        // RN3: uno o varios horarios (ids de franjas).
        private List<Integer> idsFranjas;

        // RN5: uno o varios espacios (ids de recursos).
        private List<Integer> idsEspacios;
    }
}
