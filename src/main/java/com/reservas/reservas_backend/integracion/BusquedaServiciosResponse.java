package com.reservas.reservas_backend.integracion;

import java.util.List;

public record BusquedaServiciosResponse(
        String mensaje,
        int total,
        List<ServicioEncontrado> servicios) {

    public record ServicioEncontrado(
            Integer id,
            String nombre,
            String descripcion,
            Integer empresaId,
            String empresa,
            List<SedeEncontrada> sedes) {
    }

    public record SedeEncontrada(
            Integer id,
            String nombre,
            String direccion,
            String municipio) {
    }
}
