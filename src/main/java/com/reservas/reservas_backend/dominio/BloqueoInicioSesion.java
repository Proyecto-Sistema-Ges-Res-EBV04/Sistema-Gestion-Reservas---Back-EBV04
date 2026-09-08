package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "bloqueoiniciosesion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoInicioSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "idiniciosesion")
    private InicioSesion inicioSesion;

    @Column(nullable = false)
    private Integer contador = 0;

    @Column(nullable = false)
    private Boolean bloqueado = false;

    @Column(name = "fechadesbloqueo")
    private LocalDateTime fechaDesbloqueo;
}