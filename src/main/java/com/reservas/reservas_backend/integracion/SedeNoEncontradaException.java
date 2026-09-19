package com.reservas.reservas_backend.integracion;

public class SedeNoEncontradaException extends RuntimeException {
    public SedeNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
