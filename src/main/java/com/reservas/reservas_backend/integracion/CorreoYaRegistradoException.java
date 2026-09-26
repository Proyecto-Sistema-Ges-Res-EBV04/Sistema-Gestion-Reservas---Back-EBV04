package com.reservas.reservas_backend.integracion;

// HU-01/HU-02, RN1: el correo debe ser único en toda la plataforma, sin importar el tipo de cuenta.
public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
