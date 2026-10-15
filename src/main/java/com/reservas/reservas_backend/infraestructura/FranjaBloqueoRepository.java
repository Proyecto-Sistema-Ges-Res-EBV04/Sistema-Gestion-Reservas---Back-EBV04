package com.reservas.reservas_backend.infraestructura;

import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Repository propio de HU-22 sobre la entidad CapacidadAtencion, para no modificar
// el repository de HU-16. Debe llamarse dentro de una transaccion.
public interface FranjaBloqueoRepository extends JpaRepository<CapacidadAtencion, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CapacidadAtencion c WHERE c.id = :id")
    Optional<CapacidadAtencion> bloquearPorId(@Param("id") Integer id);
}