package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // RN1/RN2 (HU-01): documento de identidad, obligatorio y único en toda la plataforma.
    @Column(name = "numerodocumento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "primernombre", length = 100)
    private String primerNombre;

    @Column(name = "segundonombre", length = 100)
    private String segundoNombre;

    @Column(name = "primerapellido", length = 100)
    private String primerApellido;

    @Column(name = "segundoapellido", length = 100)
    private String segundoApellido;

    @Column(length = 70)
    private String direccion;

    @Column(name = "idmunicipio")
    private Integer idMunicipio;

    @Column(length = 12)
    private String celular;

    @Column(name = "fechanacimiento")
    private LocalDateTime fechaNacimiento;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fechamodificacion")
    private LocalDateTime fechaModificacion;
}
