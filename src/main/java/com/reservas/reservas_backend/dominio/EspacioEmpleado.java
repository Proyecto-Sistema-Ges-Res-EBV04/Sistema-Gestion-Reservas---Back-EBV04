package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// HU-16 (RN5/RN11): espacio (recurso) que un empleado atiende en todos sus horarios.
// Un empleado puede atender varios espacios simultáneamente.
@Entity
@Table(name = "espacioempleado",
        uniqueConstraints = @UniqueConstraint(name = "uk_espacioempleado_empleado_recurso",
                columnNames = {"idempleado", "idrecurso"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspacioEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idempleado", nullable = false)
    private Empleado empleado;

    @ManyToOne
    @JoinColumn(name = "idrecurso", nullable = false)
    private Recurso recurso;
}
