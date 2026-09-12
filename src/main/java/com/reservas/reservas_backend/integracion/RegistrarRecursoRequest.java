package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrarRecursoRequest {

    @NotBlank(message = "El identificador del recurso es obligatorio")
    private String identificador;
}
