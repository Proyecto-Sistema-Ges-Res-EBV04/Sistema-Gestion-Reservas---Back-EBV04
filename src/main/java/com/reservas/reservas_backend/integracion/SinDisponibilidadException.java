package com.reservas.reservas_backend.integracion;

// Sin cupo, o sin empleado o espacio disponible para la franja elegida.
public class SinDisponibilidadException extends RuntimeException {
    public SinDisponibilidadException(String mensaje) {
        super(mensaje);
    }
}