package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Cita;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

// HU-23 (RN4): información principal de la cita.
@Data
@AllArgsConstructor
public class CitaResponse {

    private Integer id;
    private Integer idServicio;
    private String servicio;
    private Integer idEmpresa;
    private String empresa;
    private Integer idSede;
    private String sede;
    private String direccionSede;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoConsultaCita estado;

    public static CitaResponse desde(Cita cita, EstadoConsultaCita estado) {
        return new CitaResponse(
                cita.getId(),
                cita.getServicio().getId(),
                cita.getServicio().getNombre(),
                cita.getServicio().getEmpresa().getId(),
                cita.getServicio().getEmpresa().getNombre(),
                cita.getSede().getId(),
                cita.getSede().getNombre(),
                cita.getSede().getDireccion(),
                cita.getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                estado
        );
    }
}
