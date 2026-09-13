package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// HU-16 (RN7): un recurso/puesto asignado a una configuración de capacidad, con un
// empleado responsable opcional (RN7: no es obligatorio un empleado por cada recurso).
@Entity
@Table(name = "capacidadrecurso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idcapacidadatencion", nullable = false)
    private CapacidadAtencion capacidadAtencion;

    @ManyToOne
    @JoinColumn(name = "idrecurso", nullable = false)
    private Recurso recurso;

    @ManyToOne
    @JoinColumn(name = "idempleado")
    private Empleado empleado;
}
