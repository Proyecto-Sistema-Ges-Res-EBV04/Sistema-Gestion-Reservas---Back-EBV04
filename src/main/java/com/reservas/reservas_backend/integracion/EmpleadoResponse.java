package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Empleado;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmpleadoResponse {

    private Integer id;
    private String nombre;
    private String cargo;
    private Integer idSede;

    public static EmpleadoResponse desde(Empleado empleado) {
        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getCargo(),
                empleado.getSede().getId()
        );
    }
}
