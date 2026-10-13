package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "diaexcepcion")
public class Diaexcepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private Boolean activo;
    private LocalTime horainicio;
    private LocalTime horafin;
    private Long rangohora;        // en minutos
    private Long idsedeservicio;
}
