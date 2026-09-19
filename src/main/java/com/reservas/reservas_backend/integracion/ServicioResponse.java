package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.UnidadDuracion;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ServicioResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer duracion;
    private UnidadDuracion unidadDuracion;
    private List<SedeResponse> sedes;
    private List<EmpleadoResponse> empleados;
    private List<RecursoResponse> recursos;

    public static ServicioResponse desde(Servicio servicio, List<SedeResponse> sedes,
                                          List<EmpleadoResponse> empleados, List<RecursoResponse> recursos) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracion(),
                servicio.getUnidadDuracion(),
                sedes,
                empleados,
                recursos
        );
    }
}
