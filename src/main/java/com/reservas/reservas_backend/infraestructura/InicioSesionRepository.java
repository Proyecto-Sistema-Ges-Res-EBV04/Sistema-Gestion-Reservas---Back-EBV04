package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.InicioSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InicioSesionRepository extends JpaRepository<InicioSesion, Integer> {

    Optional<InicioSesion> findByCorreoElectronico(String correoElectronico);

    boolean existsByCorreoElectronico(String correoElectronico);
}
