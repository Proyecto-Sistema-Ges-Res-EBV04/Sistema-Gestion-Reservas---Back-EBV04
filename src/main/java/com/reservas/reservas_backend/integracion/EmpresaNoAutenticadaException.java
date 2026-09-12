package com.reservas.reservas_backend.integracion;

// RN1 (HU-09/HU-11): solo una Empresa autenticada puede ejecutar estas acciones.
public class EmpresaNoAutenticadaException extends RuntimeException {
    public EmpresaNoAutenticadaException(String mensaje) {
        super(mensaje);
    }
}
