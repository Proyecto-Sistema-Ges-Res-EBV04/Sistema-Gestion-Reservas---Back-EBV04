package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrarEmpleadoRequest {

    @NotBlank(message = "El nombre del empleado es obligatorio")
    private String nombre;

    @NotBlank(message = "El cargo o función del empleado es obligatorio")
    private String cargo;
}
