package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Cita;
import com.reservas.reservas_backend.dominio.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Repository propio de HU-22 sobre Cita, para no modificar el CitaRepository de HU-23.
public interface CitaAgendamientoRepository extends JpaRepository<Cita, Integer> {

    // Citas que ocupan cupo en esa fecha y hora (RN4)
    List<Cita> findByServicio_IdAndSede_IdAndFechaAndHoraInicioAndEstado(
            Integer idServicio, Integer idSede, LocalDate fecha, LocalTime horaInicio, EstadoCita estado);
}