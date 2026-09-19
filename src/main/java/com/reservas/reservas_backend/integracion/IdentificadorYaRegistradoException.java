package com.reservas.reservas_backend.integracion;

// HU-01 (documento de identidad) / HU-02 (identificación de Empresa): debe ser único en la plataforma.
public class IdentificadorYaRegistradoException extends RuntimeException {

    public IdentificadorYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
