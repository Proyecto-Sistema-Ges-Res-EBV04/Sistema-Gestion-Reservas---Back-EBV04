package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.HorarioEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HorarioEmpleadoRepository extends JpaRepository<HorarioEmpleado, Integer> {

    List<HorarioEmpleado> findByFranja_Servicio_IdAndFranja_Sede_Id(Integer idServicio, Integer idSede);

    // DELETE directo (no derivado): se ejecuta de inmediato, antes de insertar la nueva
    // configuración, para no chocar con la restricción única franja+empleado.
    @Modifying
    @Query("delete from HorarioEmpleado h where h.franja.id in "
            + "(select f.id from CapacidadAtencion f where f.servicio.id = :idServicio and f.sede.id = :idSede)")
    void eliminarPorServicioYSede(@Param("idServicio") Integer idServicio, @Param("idSede") Integer idSede);
}
