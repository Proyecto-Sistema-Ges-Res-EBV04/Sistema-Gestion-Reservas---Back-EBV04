package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// RN4/RN5 (HU-11): un servicio se asocia a una o varias sedes de la Empresa.
// La restricción única evita que el mismo servicio quede asociado dos veces a la misma sede.
// Tiene nombre fijo para que coincida con scripts/bd/01_restricciones_unicas.sql.
@Entity
@Table(name = "serviciosede",
        uniqueConstraints = @UniqueConstraint(name = "uk_serviciosede_servicio_sede",
                columnNames = {"idservicio", "idsede"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioSede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idservicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idsede", nullable = false)
    private Sede sede;
}
