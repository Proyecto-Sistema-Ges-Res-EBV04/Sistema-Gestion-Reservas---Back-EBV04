package com.reservas.reservas_backend.integracion;

// RN5/RN7/RN9 (HU-11): la configuración de personal y espacios del servicio está incompleta.
public class ServicioInvalidoException extends RuntimeException {
    public ServicioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
