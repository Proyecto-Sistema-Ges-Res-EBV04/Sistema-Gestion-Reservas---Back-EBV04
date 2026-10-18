package com.reservas.reservas_backend.integracion;

// HU-16: validaciones de negocio de la configuración de capacidad de atención
// (RN4 espacio sin horario, RN9 franja sin capacidad, RN10 espacio en uso, franjas
// superpuestas, empleado/recurso/franja que no pertenece al servicio+sede, etc.).
public class CapacidadInvalidaException extends RuntimeException {

    public CapacidadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
