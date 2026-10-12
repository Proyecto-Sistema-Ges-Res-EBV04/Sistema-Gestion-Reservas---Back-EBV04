package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * NOTA sobre RN3: la regla de negocio pide mostrar una "identificación" en el
 * perfil de Empresa/Negocio. En el esquema real de la base de datos NO existe
 * ninguna columna de identificación/NIT (ni en "negocio" ni en "usuario").
 * Por ahora se usa el id interno del negocio como identificación (igual que
 * el "ID" que pide la RN2 para el perfil de Usuario). Confirmen con el
 * equipo/profesor si esto es lo que se espera o si falta agregar una columna
 * real de identificación tributaria en la base de datos.
 */
@Data
@AllArgsConstructor
public class PerfilNegocioResponse {

    private Integer id;
    private String nombre;
    private String razonSocial;
    private String correo;
    private String telefono;
    private String celular;
    private List<SedeResponse> sedes;
}
