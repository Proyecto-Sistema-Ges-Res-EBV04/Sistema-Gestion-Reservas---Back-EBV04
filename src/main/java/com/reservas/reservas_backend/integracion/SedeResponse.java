package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SedeResponse {

    private Integer id;
    private String nombre;
    private String direccion;
    private Integer idMunicipio;
}
