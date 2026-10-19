package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sede", indexes = @Index(name = "ix_sede_idempresa", columnList = "idempresa"))
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

    // HU-09 (RN2): país como código ISO de 2 letras (ej. "CO"), el mismo que maneja el front.
    // Se deja nullable en BD para no fallar en bases que ya tienen sedes guardadas.
    @Column(length = 2)
    private String pais;

    // HU-09 (RN2/RN4): máximo 300 caracteres.
    @Column(length = 300)
    private String descripcion;

    // HU-09 (RN3): punto de referencia opcional.
    @Column(length = 200)
    private String referencia;

    // La HU-09 no pide teléfono de sede; queda opcional.
    @Column(length = 20)
    private String telefono;

    // RN6 (HU-09): la sede queda activa al finalizar correctamente su registro.
    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;
}
