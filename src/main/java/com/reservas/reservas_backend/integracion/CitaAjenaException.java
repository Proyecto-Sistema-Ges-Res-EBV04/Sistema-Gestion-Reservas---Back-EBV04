package com.reservas.reservas_backend.integracion;

// HU-25 (RN2): la cita no pertenece a la cuenta del Usuario.
public class CitaAjenaException extends RuntimeException {
    public CitaAjenaException(String mensaje) {
        super(mensaje);
    }
}
