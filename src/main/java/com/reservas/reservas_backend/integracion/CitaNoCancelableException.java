package com.reservas.reservas_backend.integracion;

// HU-25 (RN3/RN9): la cita ya pasó, ya fue cancelada o ya fue realizada.
public class CitaNoCancelableException extends RuntimeException {
    public CitaNoCancelableException(String mensaje) {
        super(mensaje);
    }
}
