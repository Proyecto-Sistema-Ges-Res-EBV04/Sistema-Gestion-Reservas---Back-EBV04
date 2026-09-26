package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import com.reservas.reservas_backend.dominio.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CapacidadAtencionRepository extends JpaRepository<CapacidadAtencion, Integer> {

    List<CapacidadAtencion> findByServicio_IdAndSede_Id(Integer idServicio, Integer idSede);

    Optional<CapacidadAtencion> findByServicio_IdAndSede_IdAndDiaSemanaAndHoraInicioAndHoraFin(
            Integer idServicio, Integer idSede, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin);

    boolean existsByServicio_Empresa_Id(Integer idEmpresa);
}
