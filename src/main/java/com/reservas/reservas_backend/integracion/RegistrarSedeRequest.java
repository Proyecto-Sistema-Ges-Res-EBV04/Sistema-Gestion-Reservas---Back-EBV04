package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrarSedeRequest {

    @NotBlank(message = "El nombre de la sede es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El municipio o ciudad es obligatorio")
    private String municipio;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^(\\+?\\d{1,3}[- ]?)?\\d{7,10}$", message = "El teléfono no tiene un formato válido")
    private String telefono;
}
