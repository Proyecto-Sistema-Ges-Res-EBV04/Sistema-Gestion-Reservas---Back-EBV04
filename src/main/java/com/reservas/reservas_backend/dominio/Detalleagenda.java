package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "detalleagenda")
public class Detalleagenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private LocalTime hora;
    private Long idiniciosesion;
    private String descripcion;

    // Solo uno de los dos viene lleno (origen del hueco)
    private Long idagenda;
    private Long iddiaexcepcion;

    private Long idestadocita;
    private LocalDateTime fechacreacion;
    private LocalDateTime fechamodificacion;
}
