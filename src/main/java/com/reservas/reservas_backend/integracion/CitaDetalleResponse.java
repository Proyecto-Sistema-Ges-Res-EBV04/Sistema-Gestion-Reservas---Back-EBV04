package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Cita;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

// HU-24 (RN3/RN4): detalle de una cita propia. Amplía CitaResponse con la duración.
// empleado y espacio son null cuando la cita no los tiene asignados.
public record CitaDetalleResponse(
        Integer id,
        Integer idEmpresa,
        String empresa,
        Integer idServicio,
        String servicio,
        Integer idSede,
        String sede,
        String direccionSede,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        long duracionMinutos,
        EstadoConsultaCita estado,
        String empleado,
        String espacio) {

    public static CitaDetalleResponse desde(Cita cita, EstadoConsultaCita estado) {
        return new CitaDetalleResponse(
                cita.getId(),
                cita.getServicio().getEmpresa().getId(),
                cita.getServicio().getEmpresa().getNombre(),
                cita.getServicio().getId(),
                cita.getServicio().getNombre(),
                cita.getSede().getId(),
                cita.getSede().getNombre(),
                cita.getSede().getDireccion(),
                cita.getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                Duration.between(cita.getHoraInicio(), cita.getHoraFin()).toMinutes(),
                estado,
                cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : null,
                cita.getRecurso() != null ? cita.getRecurso().getIdentificador() : null);
    }
}
