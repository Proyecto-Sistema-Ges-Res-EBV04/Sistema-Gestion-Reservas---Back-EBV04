package com.reservas.reservas_backend.integracion;

// HU-25: la cita que se quiere cancelar no existe.
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
