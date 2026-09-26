package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
<<<<<<< HEAD
=======
import java.time.LocalDateTime;
>>>>>>> fe52423636cf8b7ac74955e161b13613a7862192

@Entity
@Table(name = "sede")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

<<<<<<< HEAD
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "idnegocio", nullable = false)
    private Integer idNegocio;

    @Column(length = 200)
    private String direccion;

    @Column(name = "idmunicipio", nullable = false)
    private Integer idMunicipio;
=======
    @ManyToOne
    @JoinColumn(name = "idempresa", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(nullable = false, length = 20)
    private String telefono;

    // RN6 (HU-09): la sede queda activa al finalizar correctamente su registro.
    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;
>>>>>>> fe52423636cf8b7ac74955e161b13613a7862192
}
