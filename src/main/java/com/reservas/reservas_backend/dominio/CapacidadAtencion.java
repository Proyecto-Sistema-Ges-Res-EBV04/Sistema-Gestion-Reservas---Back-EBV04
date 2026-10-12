package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

// HU-16 (RN1): franja de atención de un servicio en una sede (día + rango horario).
// RN7/RN8: la capacidad no se ingresa manualmente; se calcula sumando los espacios de los
// empleados que tienen la franja asignada (ver CapacidadService). Una franja recién definida
// queda con capacidad 0 hasta que se configure.
@Entity
@Table(name = "capacidadatencion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idservicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idsede", nullable = false)
    private Sede sede;

    @Enumerated(EnumType.STRING)
    @Column(name = "diasemana", nullable = false, length = 20)
    private DiaSemana diaSemana;

    @Column(name = "horainicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horafin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fechamodificacion")
    private LocalDateTime fechaModificacion;
}
