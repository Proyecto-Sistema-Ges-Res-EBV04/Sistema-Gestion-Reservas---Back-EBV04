package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NegocioRepository extends JpaRepository<Negocio, Integer> {

    Optional<Negocio> findByIdIniciaSesion(Integer idIniciaSesion);
}
