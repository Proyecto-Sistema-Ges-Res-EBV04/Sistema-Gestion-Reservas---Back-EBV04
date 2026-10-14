package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Sede;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SedeResponse {

    private Integer id;
    private String nombre;
    private String direccion;
    private String municipio;
    private String telefono;
    private Boolean activa;

    public static SedeResponse desde(Sede sede) {
        return new SedeResponse(
                sede.getId(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getMunicipio(),
                sede.getTelefono(),
                sede.getActiva()
        );
    }
}
