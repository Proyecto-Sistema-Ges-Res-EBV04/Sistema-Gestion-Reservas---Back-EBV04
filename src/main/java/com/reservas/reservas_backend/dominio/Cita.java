package com.reservas.reservas_backend.dominio;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Reserva de un Usuario para un servicio en una sede (la crea HU-22, la consulta HU-23).
// La Empresa se obtiene a través del servicio.
@Entity
@Table(name = "cita", indexes = @Index(name = "ix_cita_usuario", columnList = "idusuario"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idservicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "idsede", nullable = false)
    private Sede sede;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horainicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horafin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fechamodificacion")
    private LocalDateTime fechaModificacion;

    // HU-22: empleado y espacio asignados al agendar (RN5/RN6). Son null en citas
    // creadas sin personal ni espacios, como los datos de prueba de HU-23.
    @ManyToOne
    @JoinColumn(name = "idempleado")
    private Empleado empleado;

    @ManyToOne
    @JoinColumn(name = "idrecurso")
    private Recurso recurso;
}
