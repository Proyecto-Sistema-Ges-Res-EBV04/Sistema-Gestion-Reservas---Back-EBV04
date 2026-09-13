package com.reservas.reservas_backend.integracion;

// HU-02, RN3/RN4/RN5: no se puede finalizar el registro de Empresa mientras falte
// sede, servicio asociado a una sede, o capacidad de atención configurada.
public class RegistroIncompletoException extends RuntimeException {

    public RegistroIncompletoException(String mensaje) {
        super(mensaje);
    }
}
