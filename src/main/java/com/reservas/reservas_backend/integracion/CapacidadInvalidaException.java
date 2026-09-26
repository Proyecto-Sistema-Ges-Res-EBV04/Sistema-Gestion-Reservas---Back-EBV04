package com.reservas.reservas_backend.integracion;

// HU-16: validaciones de negocio de la configuración de capacidad de atención
// (RN3 capacidad inválida, RN9 capacidad excede recursos/personal, RN10 límite de
// empleado excedido, recurso/empleado que no pertenece al servicio+sede, etc.).
public class CapacidadInvalidaException extends RuntimeException {

    public CapacidadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
