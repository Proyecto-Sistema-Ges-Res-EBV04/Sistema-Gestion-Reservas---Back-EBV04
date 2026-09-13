package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}