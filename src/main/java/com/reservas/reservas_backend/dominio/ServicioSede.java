package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// RN4/RN5 (HU-11): un servicio se asocia a una o varias sedes de la Empresa.
@Entity
@Table(name = "serviciosede")
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
