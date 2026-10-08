package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.ServicioSede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusquedaServicioRepository extends JpaRepository<ServicioSede, Integer> {

    // RN1/RN2/RN5: palabra clave y filtros combinables.
    // RN3 (adaptada al modelo actual): solo sedes activas de empresas con registro completo.
    // Convención: idEmpresa/idSede = 0 significa "sin filtro"; palabra y municipio llegan
    // siempre como patrón en minúsculas ("%texto%", o "%" si no hay filtro).
    @Query("""
        SELECT ss FROM ServicioSede ss
        JOIN FETCH ss.servicio s
        JOIN FETCH ss.sede sd
        JOIN FETCH s.empresa e
        WHERE sd.activa = true
          AND e.registroCompleto = true
          AND (LOWER(s.nombre) LIKE :palabra
               OR LOWER(s.descripcion) LIKE :palabra
               OR LOWER(e.nombre) LIKE :palabra)
          AND (:idEmpresa = 0 OR e.id = :idEmpresa)
          AND (:idSede = 0 OR sd.id = :idSede)
          AND LOWER(sd.municipio) LIKE :municipio
        ORDER BY s.nombre, sd.nombre
        """)
    List<ServicioSede> buscar(@Param("palabra") String palabra,
                              @Param("idEmpresa") Integer idEmpresa,
                              @Param("idSede") Integer idSede,
                              @Param("municipio") String municipio);
}