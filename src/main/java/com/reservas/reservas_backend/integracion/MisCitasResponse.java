package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// HU-23: resultado de la consulta. RN7: si no hay citas, "mensaje" explica que no hay nada que mostrar.
@Data
@AllArgsConstructor
public class MisCitasResponse {

    private int total;
    private String mensaje;
    private List<CitaResponse> citas;
}
