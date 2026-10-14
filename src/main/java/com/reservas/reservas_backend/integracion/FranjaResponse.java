package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.CapacidadAtencion;
import com.reservas.reservas_backend.dominio.DiaSemana;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class FranjaResponse {

    private Integer id;
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer capacidad;

    public static FranjaResponse desde(CapacidadAtencion franja) {
        return new FranjaResponse(
                franja.getId(),
                franja.getDiaSemana(),
                franja.getHoraInicio(),
                franja.getHoraFin(),
                franja.getCapacidad()
        );
    }
}
