package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PerfilResponse {

    private Integer id;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String direccion;
    private Integer idMunicipio;
    private String celular;
    private LocalDateTime fechaNacimiento;
}