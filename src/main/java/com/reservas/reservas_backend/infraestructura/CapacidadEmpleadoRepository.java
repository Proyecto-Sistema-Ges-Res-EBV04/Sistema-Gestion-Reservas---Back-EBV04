package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.CapacidadEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapacidadEmpleadoRepository extends JpaRepository<CapacidadEmpleado, Integer> {

    List<CapacidadEmpleado> findByCapacidadAtencion_Id(Integer idCapacidadAtencion);

    void deleteByCapacidadAtencion_Id(Integer idCapacidadAtencion);
}
