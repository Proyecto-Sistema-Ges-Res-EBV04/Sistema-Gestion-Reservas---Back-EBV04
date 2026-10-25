package com.reservas.reservas_backend.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * idDetalleAgenda = 0 o null -> crea la cita (usa idAgenda O idDiaExcepcion, fecha y hora).
 * idDetalleAgenda != 0       -> pasa la cita de estado 3 a 2.
 * Las fechas de creación y modificación las pone el servidor.
 */
public record ModificarCitaRequest(
        Long idDetalleAgenda,
        Long idIniciosesion,
        Long estadoCita,
        Long idAgenda,
        Long idDiaExcepcion,
        LocalDate fecha,
        LocalTime hora,
        String descripcion) {}
