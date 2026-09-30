package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "sede")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "idnegocio", nullable = false)
    private Integer idNegocio;

    @Column(length = 200)
    private String direccion;

    @Column(name = "idmunicipio", nullable = false)
    private Integer idMunicipio;
}
