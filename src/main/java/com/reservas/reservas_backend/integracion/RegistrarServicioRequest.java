package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.UnidadDuracion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class RegistrarServicioRequest {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser mayor a cero")
    private Integer duracion;

    @NotNull(message = "La unidad de duración es obligatoria")
    private UnidadDuracion unidadDuracion;

    @NotEmpty(message = "Debes seleccionar al menos una sede")
    private List<Integer> idsSede;
}
