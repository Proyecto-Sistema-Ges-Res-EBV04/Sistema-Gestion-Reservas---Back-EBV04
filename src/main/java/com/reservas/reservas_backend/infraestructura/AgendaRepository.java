package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    List<Agenda> findByIdsedeservicioAndActivo(Long idsedeservicio, Boolean activo);

    /** ¿Ya hay una agenda activa que se cruce con ese horario para el mismo servicio y día? (excluirId = 0 al crear) */
    @Query("""
            select count(a) > 0 from Agenda a
            where a.idsedeservicio = :idSedeServicio
              and a.iddia = :idDia
              and a.activo = :activo
              and a.horainicio < :fin
              and a.horafin > :inicio
              and a.id <> :excluirId
            """)
    boolean existeSolape(@Param("idSedeServicio") Long idSedeServicio,
                         @Param("idDia") Long idDia,
                         @Param("inicio") LocalTime inicio,
                         @Param("fin") LocalTime fin,
                         @Param("activo") Boolean activo,
                         @Param("excluirId") Long excluirId);
}
