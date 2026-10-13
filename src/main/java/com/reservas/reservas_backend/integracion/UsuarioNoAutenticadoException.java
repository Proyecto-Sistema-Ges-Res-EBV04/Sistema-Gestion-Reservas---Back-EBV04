package com.reservas.reservas_backend.integracion;

// RN1 (HU-23): solo un Usuario autenticado puede ejecutar estas acciones.
public class UsuarioNoAutenticadoException extends RuntimeException {
    public UsuarioNoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
