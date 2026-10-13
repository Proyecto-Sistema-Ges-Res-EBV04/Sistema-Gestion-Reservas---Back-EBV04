package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrarRecursoRequest {

    @NotBlank(message = "El identificador del recurso es obligatorio")
    private String identificador;

    // HU-16 (RN10): opcional; si no se envía el espacio es de uso exclusivo.
    private Boolean exclusivo;
}
