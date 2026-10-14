package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.EspacioEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EspacioEmpleadoRepository extends JpaRepository<EspacioEmpleado, Integer> {

    List<EspacioEmpleado> findByEmpleado_Servicio_IdAndEmpleado_Sede_Id(Integer idServicio, Integer idSede);

    // DELETE directo (no derivado): se ejecuta de inmediato, antes de insertar la nueva
    // configuración, para no chocar con la restricción única empleado+recurso.
    @Modifying
    @Query("delete from EspacioEmpleado e where e.empleado.id in "
            + "(select emp.id from Empleado emp where emp.servicio.id = :idServicio and emp.sede.id = :idSede)")
    void eliminarPorServicioYSede(@Param("idServicio") Integer idServicio, @Param("idSede") Integer idSede);
}
