package com.reservas.reservas_backend.integracion;

// HU-23: filtros de la consulta de citas que no tienen sentido (ej. rango de fechas invertido).
public class ConsultaCitasInvalidaException extends RuntimeException {
    public ConsultaCitasInvalidaException(String mensaje) {
        super(mensaje);
    }
}
