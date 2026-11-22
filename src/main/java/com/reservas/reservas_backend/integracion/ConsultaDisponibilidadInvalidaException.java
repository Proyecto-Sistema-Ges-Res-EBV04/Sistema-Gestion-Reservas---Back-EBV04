package com.reservas.reservas_backend.integracion;

// HU-21: la consulta de disponibilidad no tiene los datos mínimos (ej. sin fecha o fecha pasada).
public class ConsultaDisponibilidadInvalidaException extends RuntimeException {
    public ConsultaDisponibilidadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
