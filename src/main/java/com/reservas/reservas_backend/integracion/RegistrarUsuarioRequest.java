package com.reservas.reservas_backend.integracion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

// HU-01: Registrar cuenta de Usuario (RN1-RN5).
@Data
public class RegistrarUsuarioRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "El nombre es obligatorio")
    private String primerNombre;

    private String segundoNombre;

    private String primerApellido;

    private String segundoApellido;

    @NotBlank(message = "El documento de identidad es obligatorio")
    private String numeroDocumento;

    private String direccion;

    private Integer idMunicipio;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^(\\+?\\d{1,3}[- ]?)?\\d{7,10}$", message = "El teléfono no tiene un formato válido")
    private String celular;

    private LocalDateTime fechaNacimiento;

    // RN4: mínimo 8 caracteres, al menos una letra y un número.
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres e incluir al menos una letra y un número")
    private String clave;
}
