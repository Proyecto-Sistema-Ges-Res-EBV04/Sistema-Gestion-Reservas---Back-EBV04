package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SedeRepository extends JpaRepository<Sede, Integer> {

    List<Sede> findByEmpresa_Id(Integer idEmpresa);

    List<Sede> findByEmpresa_IdAndActivaTrue(Integer idEmpresa);
}
