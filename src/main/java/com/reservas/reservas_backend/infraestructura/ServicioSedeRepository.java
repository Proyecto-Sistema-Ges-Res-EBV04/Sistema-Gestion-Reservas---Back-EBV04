package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.ServicioSede;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioSedeRepository extends JpaRepository<ServicioSede, Integer> {

    boolean existsByServicio_IdAndSede_Id(Integer idServicio, Integer idSede);

    List<ServicioSede> findByServicio_Id(Integer idServicio);

    boolean existsByServicio_Empresa_Id(Integer idEmpresa);
}
