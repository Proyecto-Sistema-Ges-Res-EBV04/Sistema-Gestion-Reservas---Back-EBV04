package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Detalleagenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface DetalleAgendaRepository extends JpaRepository<Detalleagenda, Long> {

    // ¿Tiene registros en un estado distinto al indicado? (se usa con estado 2)
    boolean existsByIdagendaAndIdestadocitaNot(Long idagenda, Long idestadocita);

    boolean existsByIddiaexcepcionAndIdestadocitaNot(Long iddiaexcepcion, Long idestadocita);

    // ¿Ya hay una cita viva en ese hueco?
    boolean existsByIdagendaAndFechaAndHoraAndIdestadocitaNot(Long idagenda, LocalDate fecha, LocalTime hora, Long idestadocita);

    boolean existsByIddiaexcepcionAndFechaAndHoraAndIdestadocitaNot(Long iddiaexcepcion, LocalDate fecha, LocalTime hora, Long idestadocita);

    // Citas vivas en un rango de fechas (para quitar huecos tomados al listar)
    List<Detalleagenda> findByIdagendaInAndFechaBetweenAndIdestadocitaNot(
            Collection<Long> idagendas, LocalDate desde, LocalDate hasta, Long idestadocita);

    List<Detalleagenda> findByIddiaexcepcionInAndFechaBetweenAndIdestadocitaNot(
            Collection<Long> iddiaexcepciones, LocalDate desde, LocalDate hasta, Long idestadocita);

    List<Detalleagenda> findByIdiniciosesionAndFechaAndIdestadocitaNot(
            Long idiniciosesion, LocalDate fecha, Long idestadocita);
    List<Detalleagenda> findByIdiniciosesionAndIdestadocitaOrderByFechaAscHoraAsc(
            Long idiniciosesion, Long idestadocita);
    List<Detalleagenda> findByIdiniciosesionOrderByFechaAscHoraAsc(Long idIniciosesion);
}
