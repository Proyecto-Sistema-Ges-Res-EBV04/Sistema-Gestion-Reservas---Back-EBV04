package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

// HU-16 (RN1-RN6): capacidad máxima de citas/recursos reservables simultáneamente
// para una combinación de servicio, sede y horario (día + rango horario).
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
