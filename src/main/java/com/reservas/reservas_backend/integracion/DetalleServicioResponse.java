package com.reservas.reservas_backend.integracion;

import java.util.List;

// HU-20: ficha pública de un servicio.
// RN2: nombre, descripción, duración, Empresa prestadora y sedes.
// RN5: no expone personal, espacios ni reservas; solo datos de la oferta pública.
public record DetalleServicioResponse(
        Integer id,
        String nombre,
        String descripcion,
        Integer duracion,
        String unidadDuracion,
        EmpresaPrestadora empresa,
        // RN4: true si al menos una sede puede recibir reservas.
        boolean reservable,
        String mensaje,
        List<SedeOferta> sedes) {

    public record EmpresaPrestadora(
            Integer id,
            String nombre) {
    }

    // RN3: cada sede de la oferta pública, con su propia disponibilidad para agendar.
    // RN6: si reservable = true, desde aquí se puede continuar a consultar disponibilidad (HU-21).
    public record SedeOferta(
            Integer id,
            String nombre,
            String direccion,
            String municipio,
            String telefono,
            boolean reservable) {
    }
}
