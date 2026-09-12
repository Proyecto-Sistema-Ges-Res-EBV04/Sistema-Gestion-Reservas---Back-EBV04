package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad mínima: HU-02 (Registrar cuenta de empresa) es responsabilidad de otro
 * integrante del equipo y agregará el resto de los datos de registro (NIT, correo
 * de contacto, etc.). Esta tabla existe ya para que Sede y Servicio (HU-09/HU-11)
 * tengan a qué Empresa asociarse.
 */
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

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;
}
