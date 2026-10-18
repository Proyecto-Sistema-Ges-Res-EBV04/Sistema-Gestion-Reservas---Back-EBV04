package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapacidadAtencionRepository extends JpaRepository<CapacidadAtencion, Integer> {

    List<CapacidadAtencion> findByServicio_IdAndSede_Id(Integer idServicio, Integer idSede);

    // RN9: solo cuenta como configurada una franja con capacidad mínima de una atención.
    boolean existsByServicio_Empresa_IdAndCapacidadGreaterThan(Integer idEmpresa, Integer capacidad);
}
