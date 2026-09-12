package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// RN7/RN8/RN10 (HU-11): personal registrado de forma independiente por servicio y sede.
@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idservicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idsede", nullable = false)
    private Sede sede;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;
}
