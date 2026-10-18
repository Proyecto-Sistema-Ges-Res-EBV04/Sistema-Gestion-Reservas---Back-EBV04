package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    List<Empleado> findByServicio_Id(Integer idServicio);

    // HU-16 (RN2): personal disponible para un servicio en una sede.
    List<Empleado> findByServicio_IdAndSede_Id(Integer idServicio, Integer idSede);
}
