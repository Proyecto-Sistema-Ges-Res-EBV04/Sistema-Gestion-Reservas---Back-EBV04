package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrarSedeRequest {

    @NotBlank(message = "El nombre de la sede es obligatorio")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El municipio o ciudad es obligatorio")
    private String municipio;

    // RN2 pide país y descripción obligatorios, pero por ahora se aceptan vacíos porque el
    // registro de empresa del front (RegistrarEmpresa2.tsx) todavía no los envía.
    // Cuando el front los mande, agregar @NotBlank a estos dos campos.
    @Size(min = 2, max = 2, message = "El país debe ser un código de 2 letras (ej. CO)")
    private String pais;

    // RN4: máximo 300 caracteres.
    @Size(max = 300, message = "La descripción permite máximo 300 caracteres")
    private String descripcion;

    // RN3: opcional.
    @Size(max = 200, message = "El punto de referencia permite máximo 200 caracteres")
    private String referencia;

    // Opcional: la HU-09 no lo pide. Si llega, debe tener un formato válido.
    @Pattern(regexp = "^(\\+?\\d{1,3}[- ]?)?\\d{7,10}$", message = "El teléfono no tiene un formato válido")
    private String telefono;
}
