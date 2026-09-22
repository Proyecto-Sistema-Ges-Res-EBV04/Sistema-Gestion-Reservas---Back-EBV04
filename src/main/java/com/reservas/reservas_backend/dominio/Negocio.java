package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Representa una cuenta de tipo Negocio/Empresa (tabla "negocio").
 * Se relaciona con InicioSesion por idIniciaSesion (columna idiniciosesion),
 * NO por idUsuario: la persona natural (Usuario) que registró la cuenta
 * sigue existiendo aparte, este registro es la info propia del negocio.
 */
@Entity
@Table(name = "negocio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 250)
    private String nombre;

    @Column(name = "razonsocial", length = 300)
    private String razonSocial;

    @Column(name = "idtiponegocio", nullable = false)
    private Integer idTipoNegocio;

    @Column(length = 12)
    private String telefono;

    @Column(length = 16)
    private String celular;

    @Column(name = "idiniciosesion", nullable = false)
    private Integer idIniciaSesion;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fechamodificacion")
    private LocalDateTime fechaModificacion;
}
