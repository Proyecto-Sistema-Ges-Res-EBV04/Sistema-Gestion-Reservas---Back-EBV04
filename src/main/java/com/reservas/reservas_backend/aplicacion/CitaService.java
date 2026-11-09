package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Cita;
import com.reservas.reservas_backend.dominio.Empresa;
import com.reservas.reservas_backend.dominio.EstadoCita;
import com.reservas.reservas_backend.dominio.InicioSesion;
import com.reservas.reservas_backend.dominio.Usuario;
import com.reservas.reservas_backend.infraestructura.CitaRepository;
import com.reservas.reservas_backend.infraestructura.InicioSesionRepository;
import com.reservas.reservas_backend.integracion.CitaRecibidaDetalleResponse;
import com.reservas.reservas_backend.integracion.CitaAjenaException;
import com.reservas.reservas_backend.integracion.CitaNoCancelableException;
import com.reservas.reservas_backend.integracion.CitaNoEncontradaException;
import com.reservas.reservas_backend.integracion.CitaResponse;
import com.reservas.reservas_backend.integracion.ConsultaCitasInvalidaException;
import com.reservas.reservas_backend.integracion.EstadoConsultaCita;
import com.reservas.reservas_backend.integracion.MisCitasResponse;
import com.reservas.reservas_backend.integracion.CitaDetalleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final UsuarioContextoService usuarioContexto;
    private final EmpresaContextoService empresaContexto;
    private final InicioSesionRepository inicioSesionRepository;

    @Autowired
    public CitaService(CitaRepository citaRepository, UsuarioContextoService usuarioContexto,
                       EmpresaContextoService empresaContexto, InicioSesionRepository inicioSesionRepository) {
        this.citaRepository = citaRepository;
        this.usuarioContexto = usuarioContexto;
        this.empresaContexto = empresaContexto;
        this.inicioSesionRepository = inicioSesionRepository;
    }

    // HU-23: Consultar mis citas.
    // RN8: solo lectura; el estado próxima/realizada se calcula para mostrar, no se guarda.
    @Transactional(readOnly = true)
    public MisCitasResponse consultarMisCitas(LocalDate fechaDesde, LocalDate fechaHasta, Integer idEmpresa,
                                              Integer idServicio, EstadoConsultaCita estado) {
        // RN1/RN2: solo el Usuario autenticado y solo sus propias citas.
        Usuario usuario = usuarioContexto.obtenerUsuarioAutenticado();

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new ConsultaCitasInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }

        // RN5/RN6: los filtros son opcionales y se combinan entre sí.
        LocalDateTime ahora = LocalDateTime.now();
        List<CitaResponse> citas = new ArrayList<>();
        for (Cita cita : citaRepository.findByUsuario_IdOrderByFechaDescHoraInicioDesc(usuario.getId())) {
            EstadoConsultaCita estadoCita = calcularEstado(cita, ahora);
            if (fechaDesde != null && cita.getFecha().isBefore(fechaDesde)) {
                continue;
            }
            if (fechaHasta != null && cita.getFecha().isAfter(fechaHasta)) {
                continue;
            }
            if (idEmpresa != null && !idEmpresa.equals(cita.getServicio().getEmpresa().getId())) {
                continue;
            }
            if (idServicio != null && !idServicio.equals(cita.getServicio().getId())) {
                continue;
            }
            if (estado != null && estado != estadoCita) {
                continue;
            }
            citas.add(CitaResponse.desde(cita, estadoCita));
        }

        // RN7: mensaje informativo cuando no hay resultados.
        String mensaje = null;
        if (citas.isEmpty()) {
            boolean hayFiltros = fechaDesde != null || fechaHasta != null || idEmpresa != null
                    || idServicio != null || estado != null;
            mensaje = hayFiltros
                    ? "No hay citas que cumplan los filtros seleccionados"
                    : "No tienes citas para mostrar";
        }
        return new MisCitasResponse(citas.size(), mensaje, citas);
    }
        // HU-24: Consultar detalle de una cita propia. Solo lectura (RN7).
    @Transactional(readOnly = true)
    public CitaDetalleResponse consultarDetalleCita(Integer idCita) {
        // RN1: solo un Usuario autenticado.
        Usuario usuario = usuarioContexto.obtenerUsuarioAutenticado();

        // RN6: si no existe o es de otra cuenta, la respuesta es la misma y no revela nada.
        Cita cita = citaRepository.findById(idCita)
                .filter(c -> c.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new CitaNoEncontradaException("La cita no existe"));

        // RN5: próxima, realizada o cancelada, con el mismo cálculo de HU-23.
        return CitaDetalleResponse.desde(cita, calcularEstado(cita, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public MisCitasResponse consultarCitasRecibidas(Integer idSede, Integer idServicio, LocalDate fechaDesde,
                                                    LocalDate fechaHasta, EstadoConsultaCita estado) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new ConsultaCitasInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }

        LocalDateTime ahora = LocalDateTime.now();
        List<CitaResponse> citas = new ArrayList<>();
        for (Cita cita : citaRepository.findByServicio_Empresa_IdOrderByFechaDescHoraInicioDesc(empresa.getId())) {
            EstadoConsultaCita estadoCita = calcularEstado(cita, ahora);
            if (idSede != null && !idSede.equals(cita.getSede().getId())) {
                continue;
            }
            if (idServicio != null && !idServicio.equals(cita.getServicio().getId())) {
                continue;
            }
            if (fechaDesde != null && cita.getFecha().isBefore(fechaDesde)) {
                continue;
            }
            if (fechaHasta != null && cita.getFecha().isAfter(fechaHasta)) {
                continue;
            }
            if (estado != null && estado != estadoCita) {
                continue;
            }
            citas.add(CitaResponse.desde(cita, estadoCita));
        }

        String mensaje = null;
        if (citas.isEmpty()) {
            boolean hayFiltros = idSede != null || idServicio != null || fechaDesde != null
                    || fechaHasta != null || estado != null;
            mensaje = hayFiltros
                    ? "No hay citas que cumplan los filtros seleccionados"
                    : "No hay citas recibidas para mostrar";
        }
        return new MisCitasResponse(citas.size(), mensaje, citas);
    }

    @Transactional(readOnly = true)
    public CitaRecibidaDetalleResponse consultarDetalleCitaRecibida(Integer idCita) {
        Empresa empresa = empresaContexto.obtenerEmpresaAutenticada();

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("La cita no existe"));

        if (!cita.getServicio().getEmpresa().getId().equals(empresa.getId())) {
            throw new CitaNoEncontradaException("La cita no existe");
        }

        InicioSesion cuenta = inicioSesionRepository
                .findFirstByIdUsuarioAndTipoUsuario_NombreIgnoreCase(cita.getUsuario().getId(), "USUARIO")
                .orElse(null);
        String correo = cuenta != null ? cuenta.getCorreoElectronico() : null;

        return CitaRecibidaDetalleResponse.desde(cita, calcularEstado(cita, LocalDateTime.now()), correo);
    }

    // HU-25: Cancelar cita.
    @Transactional
    public CitaResponse cancelarCita(Integer idCita) {
        // RN1: solo un Usuario autenticado.
        Usuario usuario = usuarioContexto.obtenerUsuarioAutenticado();

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("La cita no existe"));

        // RN2: solo citas de su propia cuenta.
        if (!cita.getUsuario().getId().equals(usuario.getId())) {
            throw new CitaAjenaException("No puedes cancelar una cita que no es tuya");
        }

        // RN3/RN5/RN9: se valida al confirmar que siga programada y que sea futura.
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new CitaNoCancelableException("La cita ya fue cancelada o realizada y no puede cancelarse");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (!LocalDateTime.of(cita.getFecha(), cita.getHoraInicio()).isAfter(ahora)) {
            throw new CitaNoCancelableException("Solo se pueden cancelar citas futuras");
        }

        // RN6/RN7/RN8: se conserva el registro; HU-22 solo cuenta citas programadas, así se libera el cupo.
        cita.setEstado(EstadoCita.CANCELADA);
        cita.setFechaModificacion(ahora);
        Cita guardada = citaRepository.save(cita);
        return CitaResponse.desde(guardada, EstadoConsultaCita.CANCELADA);
    }

    // RN3: una cita programada cuya hora de fin ya pasó se muestra como realizada.
    private EstadoConsultaCita calcularEstado(Cita cita, LocalDateTime ahora) {
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            return EstadoConsultaCita.CANCELADA;
        }
        if (cita.getEstado() == EstadoCita.REALIZADA
                || !LocalDateTime.of(cita.getFecha(), cita.getHoraFin()).isAfter(ahora)) {
            return EstadoConsultaCita.REALIZADA;
        }
        return EstadoConsultaCita.PROXIMA;
    }
}
