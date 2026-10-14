package com.reservas.reservas_backend.integracion;

// Datos de la cita que no se pueden agendar (servicio o sede invalidos, fecha pasada, hora fuera de la franja).
public class DatosCitaInvalidosException extends RuntimeException {
    public DatosCitaInvalidosException(String mensaje) {
        super(mensaje);
    }
}