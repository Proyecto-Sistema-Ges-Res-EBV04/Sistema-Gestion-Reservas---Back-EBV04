package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

    List<Recurso> findByServicio_Id(Integer idServicio);
}
