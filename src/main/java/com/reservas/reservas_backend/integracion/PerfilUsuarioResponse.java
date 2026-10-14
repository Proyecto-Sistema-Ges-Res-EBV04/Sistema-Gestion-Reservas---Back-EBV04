package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PerfilUsuarioResponse {

    private Integer id;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String correo;
    private String direccion;
    private Integer idMunicipio;
    private String celular;
    private LocalDateTime fechaNacimiento;
}
