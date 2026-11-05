package com.reservas.reservas_backend.integracion;

import java.util.List;

// HU-18 (RN4/RN5): solo información pública de la Empresa y de sus sedes activas.
// No incluye NIT, teléfono ni datos de la cuenta.
public record BusquedaEmpresasResponse(
        String mensaje,
        int total,
        List<EmpresaEncontrada> empresas) {

    public record EmpresaEncontrada(
            Integer id,
            String nombre,
            List<SedeEncontrada> sedes) {
    }

    public record SedeEncontrada(
            Integer id,
            String nombre,
            String direccion,
            String municipio,
            String pais) {
    }
}