package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sede")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
}
