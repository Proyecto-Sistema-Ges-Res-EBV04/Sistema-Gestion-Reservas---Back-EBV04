package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Diaexcepcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaExcepcionRepository extends JpaRepository<Diaexcepcion, Long> {

    List<Diaexcepcion> findByIdsedeservicioAndFechaBetween(Long idsedeservicio, LocalDate desde, LocalDate hasta);

    boolean existsByIdsedeservicioAndFecha(Long idsedeservicio, LocalDate fecha);

    boolean existsByIdsedeservicioAndFechaAndIdNot(Long idsedeservicio, LocalDate fecha, Long id);
}
