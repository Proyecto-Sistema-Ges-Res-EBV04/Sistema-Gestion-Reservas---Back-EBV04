package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    boolean existsByNombre(String nombre);

    boolean existsByIdentificacion(String identificacion);
}
