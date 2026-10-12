package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// HU-16 (RN3): horario asignado a un empleado, es decir, una franja de atención en la que trabaja.
// La restricción única evita asignar dos veces la misma franja al mismo empleado.
@Entity
@Table(name = "horarioempleado",
        uniqueConstraints = @UniqueConstraint(name = "uk_horarioempleado_franja_empleado",
                columnNames = {"idcapacidadatencion", "idempleado"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idcapacidadatencion", nullable = false)
    private CapacidadAtencion franja;

    @ManyToOne
    @JoinColumn(name = "idempleado", nullable = false)
    private Empleado empleado;
}
