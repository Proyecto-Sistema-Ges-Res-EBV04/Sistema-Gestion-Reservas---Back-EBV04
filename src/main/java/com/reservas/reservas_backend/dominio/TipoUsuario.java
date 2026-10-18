package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// La restricción única evita dos tipos con el mismo nombre (el código busca el rol por nombre).
// Tiene nombre fijo para que coincida con scripts/bd/01_restricciones_unicas.sql.
@Entity
@Table(name = "tipousuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_tipousuario_nombre",
                columnNames = {"nombre"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String nombre;
}
