package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.Recurso;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecursoResponse {

    private Integer id;
    private String identificador;
    private Integer idSede;

    public static RecursoResponse desde(Recurso recurso) {
        return new RecursoResponse(
                recurso.getId(),
                recurso.getIdentificador(),
                recurso.getSede().getId()
        );
    }
}
