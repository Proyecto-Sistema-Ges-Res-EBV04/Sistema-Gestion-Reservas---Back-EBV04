package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// La restricción única evita dos cuentas con el mismo correo (el login busca por correo).
// Tiene nombre fijo para que coincida con scripts/bd/01_restricciones_unicas.sql.
@Entity
@Table(name = "iniciosesion",
        uniqueConstraints = @UniqueConstraint(name = "uk_iniciosesion_correo",
                columnNames = {"correoelectronico"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InicioSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "correoelectronico", nullable = false, length = 120)
    private String correoElectronico;

    @Column(nullable = false, length = 70)
    private String clave;

    @Column(name = "idusuario")
    private Integer idUsuario;

    @ManyToOne
    @JoinColumn(name = "idtipousuario")
    private TipoUsuario tipoUsuario;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fechamodificacion")
    private LocalDateTime fechaModificacion;
}
