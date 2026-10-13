package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "agenda")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long iddia;
    private LocalTime horainicio;
    private LocalTime horafin;
    private LocalTime rangohora;   // duración del hueco, ej. 00:30
    private Boolean activo;
    private Long idsedeservicio;
}
