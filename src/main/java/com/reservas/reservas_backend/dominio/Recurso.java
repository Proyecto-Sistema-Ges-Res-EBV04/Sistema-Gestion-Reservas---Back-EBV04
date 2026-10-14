package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// RN7/RN8/RN9/RN10 (HU-11): recurso o puesto/lugar de atención, independiente del personal.
@Entity
@Table(name = "recurso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idservicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idsede", nullable = false)
    private Sede sede;

    @Column(nullable = false, length = 100)
    private String identificador;

    // HU-16 (RN10): un espacio exclusivo no puede usarlo más de un empleado dentro de la misma franja.
    // El default en BD permite agregar la columna a tablas que ya tienen registros.
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean exclusivo = true;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;
}
