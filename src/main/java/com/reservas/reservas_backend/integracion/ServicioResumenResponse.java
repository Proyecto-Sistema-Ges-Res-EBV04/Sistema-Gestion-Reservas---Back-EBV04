package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.UnidadDuracion;
import lombok.AllArgsConstructor;
import lombok.Data;

// HU-06 RN3: datos principales de un servicio para el perfil de Empresa.
// El detalle completo (sedes, empleados, recursos) se consulta en GET /api/servicios/{id}.
@Data
@AllArgsConstructor
public class ServicioResumenResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer duracion;
    private UnidadDuracion unidadDuracion;

    public static ServicioResumenResponse desde(Servicio servicio) {
        return new ServicioResumenResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracion(),
                servicio.getUnidadDuracion()
        );
    }
}
