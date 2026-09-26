package com.reservas.reservas_backend.integracion;

// RN10 (HU-11): personal/recursos se registran sobre un par (Servicio, Sede) ya asociado.
public class AsociacionServicioSedeNoEncontradaException extends RuntimeException {
    public AsociacionServicioSedeNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
