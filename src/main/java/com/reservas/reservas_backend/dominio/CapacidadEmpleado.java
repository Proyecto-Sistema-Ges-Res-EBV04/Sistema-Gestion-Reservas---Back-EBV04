package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// HU-16 (RN8): máximo de recursos/unidades simultáneas que un empleado puede
// atender dentro de una configuración de capacidad (servicio+sede+horario).
@Entity
@Table(name = "capacidadempleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idcapacidadatencion", nullable = false)
    private CapacidadAtencion capacidadAtencion;

    @ManyToOne
    @JoinColumn(name = "idempleado", nullable = false)
    private Empleado empleado;

    @Column(name = "limitesimultaneo", nullable = false)
    private Integer limiteSimultaneo;
}
