package com.reservas.reservas_backend.integracion;

// RN4 (HU-11): el servicio debe asociarse al menos a una sede ACTIVA de la Empresa.
public class SedeInactivaException extends RuntimeException {
    public SedeInactivaException(String mensaje) {
        super(mensaje);
    }
}
