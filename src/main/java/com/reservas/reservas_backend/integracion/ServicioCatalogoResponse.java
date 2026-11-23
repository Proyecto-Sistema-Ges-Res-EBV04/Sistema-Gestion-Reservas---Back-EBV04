package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.UnidadDuracion;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ServicioCatalogoResponse {
    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String PARCIAL = "PARCIAL";
    public static final String PENDIENTE = "PENDIENTE_DE_CONFIGURACION";

    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer duracion;
    private UnidadDuracion unidadDuracion;
    private String estado;
    private boolean reservable;
    private List<SedeCatalogoResponse> sedes;
}