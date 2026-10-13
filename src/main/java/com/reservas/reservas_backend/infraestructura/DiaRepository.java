package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Dia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaRepository extends JpaRepository<Dia, Long> {
}
