package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Cita;
import com.reservas.reservas_backend.dominio.Usuario;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRecibidaDetalleResponse(
        Integer id,
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
        String nombreUsuario,
        String correoUsuario,
        String celularUsuario,
        String empleado,
        String espacio) {

    public static CitaRecibidaDetalleResponse desde(Cita cita, EstadoConsultaCita estado, String correoUsuario) {
        return new CitaRecibidaDetalleResponse(
                cita.getId(),
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
                nombreCompleto(cita.getUsuario()),
                correoUsuario,
                cita.getUsuario().getCelular(),
                cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : null,
                cita.getRecurso() != null ? cita.getRecurso().getIdentificador() : null);
    }

    private static String nombreCompleto(Usuario usuario) {
        String[] partes = {usuario.getPrimerNombre(), usuario.getSegundoNombre(),
                usuario.getPrimerApellido(), usuario.getSegundoApellido()};
        StringBuilder nombre = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                if (nombre.length() > 0) {
                    nombre.append(" ");
                }
                nombre.append(parte.trim());
            }
        }
        return nombre.toString();
    }
}
