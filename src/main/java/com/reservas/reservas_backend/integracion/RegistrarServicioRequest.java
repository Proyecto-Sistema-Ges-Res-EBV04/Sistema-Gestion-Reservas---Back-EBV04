package com.reservas.reservas_backend.integracion;

import com.reservas.reservas_backend.dominio.UnidadDuracion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

// HU-11: el servicio se registra completo en una sola petición (lo que el front envía al
// confirmar desde la revisión, RN11): datos generales + por cada sede sus espacios y su personal.
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

    // RN4: al menos una sede.
    @NotEmpty(message = "Debes seleccionar al menos una sede")
    @Valid
    private List<SedeDelServicio> sedes;

    @Data
    public static class SedeDelServicio {

        @NotNull(message = "Cada sede seleccionada debe indicar su idSede")
        private Integer idSede;

        // RN9: identificadores de los espacios de esta sede (ej. "Cabina 1").
        private List<String> espacios;

        // RN5: al menos un empleado por sede (se valida en ServicioService con un mensaje por sede).
        @Valid
        private List<EmpleadoDelServicio> empleados;
    }

    @Data
    public static class EmpleadoDelServicio {

        // RN6
        @NotBlank(message = "El nombre del empleado es obligatorio")
        private String nombre;

        @NotBlank(message = "El cargo o función del empleado es obligatorio")
        private String cargo;

        // RN7/RN8: uno o varios espacios de la misma sede, por su identificador.
        private List<String> espacios;
    }
}
