package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Empleado;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class EmpleadoResponse {

    private Integer id;
    private String nombre;
    private String cargo;
    private Integer idSede;
    // HU-11 (RN7): identificadores de los espacios asignados al empleado.
    private List<String> espacios;

    public static EmpleadoResponse desde(Empleado empleado) {
        return desde(empleado, new ArrayList<>());
    }

    public static EmpleadoResponse desde(Empleado empleado, List<String> espacios) {
        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getCargo(),
                empleado.getSede().getId(),
                espacios
        );
    }
}
