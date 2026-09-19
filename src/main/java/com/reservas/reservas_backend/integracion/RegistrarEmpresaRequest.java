package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// HU-02: Registrar cuenta de Empresa (RN1-RN2, RN6-RN7). La configuración inicial
// (sede/servicio/capacidad, RN3-RN5) se hace después con los endpoints ya existentes
// y se valida con /api/empresas/finalizar-registro.
@Data
public class RegistrarEmpresaRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "El nombre o razón social es obligatorio")
    private String nombre;

    @NotBlank(message = "La identificación de la Empresa es obligatoria")
    private String identificacion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^(\\+?\\d{1,3}[- ]?)?\\d{7,10}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    // RN6: mínimo 8 caracteres, al menos una letra y un número.
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres e incluir al menos una letra y un número")
    private String clave;
}
