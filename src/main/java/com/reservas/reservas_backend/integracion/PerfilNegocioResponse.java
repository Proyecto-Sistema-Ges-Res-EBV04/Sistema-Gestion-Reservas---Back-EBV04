package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * HU-06 RN3: perfil de Empresa. Muestra nombre, identificación (NIT),
 * teléfono y correo, más las sedes de la Empresa. Los datos salen de la
 * tabla "empresa" (HU-02).
 */
@Data
@AllArgsConstructor
public class PerfilNegocioResponse {

    private Integer id;
    private String nombre;
    private String identificacion;
    private String correo;
    private String telefono;
    private List<SedeResponse> sedes;
}
