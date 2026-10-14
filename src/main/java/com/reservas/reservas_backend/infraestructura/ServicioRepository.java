package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    boolean existsByEmpresa_Id(Integer idEmpresa);

    List<Servicio> findByEmpresa_Id(Integer idEmpresa);
}
