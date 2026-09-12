package com.reservas.reservas_backend.integracion;

public class ServicioNoEncontradoException extends RuntimeException {
    public ServicioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
