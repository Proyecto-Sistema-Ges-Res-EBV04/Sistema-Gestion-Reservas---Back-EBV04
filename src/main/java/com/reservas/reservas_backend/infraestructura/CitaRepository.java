package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    // HU-23 (RN2): solo las citas de la cuenta del Usuario.
    List<Cita> findByUsuario_IdOrderByFechaDescHoraInicioDesc(Integer idUsuario);
}
