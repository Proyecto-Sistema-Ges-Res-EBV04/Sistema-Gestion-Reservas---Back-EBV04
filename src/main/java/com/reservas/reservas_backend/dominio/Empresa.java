package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// HU-02: Registrar cuenta de Empresa.
@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String nombre;

    // RN1/RN2 (HU-02): identificación de la Empresa (NIT), obligatoria y única en la plataforma.
    @Column(nullable = false, unique = true, length = 30)
    private String identificacion;

    @Column(nullable = false, length = 20)
    private String telefono;

    // RN3/RN4/RN5 (HU-02): true solo cuando ya tiene sede + servicio asociado a una sede + capacidad configurada.
    @Column(name = "registrocompleto", nullable = false)
    private Boolean registroCompleto = false;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;
}
