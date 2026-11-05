package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusquedaEmpresaRepository extends JpaRepository<Sede, Integer> {

    // HU-18 RN2/RN3: nombre de la Empresa y/o municipio de la sede, combinables.
    // Mismo criterio de visibilidad que HU-19: solo sedes activas de Empresas con registro completo.
    // Los parámetros llegan como patrón en minúsculas ("%texto%", o "%" si no hay filtro).
    @Query("""
        SELECT sd FROM Sede sd
        JOIN FETCH sd.empresa e
        WHERE sd.activa = true
          AND e.registroCompleto = true
          AND LOWER(e.nombre) LIKE :nombre
          AND LOWER(sd.municipio) LIKE :municipio
        ORDER BY e.nombre, sd.nombre
        """)
    List<Sede> buscar(@Param("nombre") String nombre,
                      @Param("municipio") String municipio);
}
