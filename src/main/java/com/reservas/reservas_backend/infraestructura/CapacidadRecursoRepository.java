package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.CapacidadRecurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapacidadRecursoRepository extends JpaRepository<CapacidadRecurso, Integer> {

    List<CapacidadRecurso> findByCapacidadAtencion_Id(Integer idCapacidadAtencion);

    void deleteByCapacidadAtencion_Id(Integer idCapacidadAtencion);
}
