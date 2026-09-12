package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Integer> {

    Optional<TipoUsuario> findByNombre(String nombre);
}
