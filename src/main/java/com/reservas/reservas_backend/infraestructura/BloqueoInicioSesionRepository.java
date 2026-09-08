package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.BloqueoInicioSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BloqueoInicioSesionRepository extends JpaRepository<BloqueoInicioSesion, Integer> {

    Optional<BloqueoInicioSesion> findByInicioSesion_Id(Integer idInicioSesion);
}