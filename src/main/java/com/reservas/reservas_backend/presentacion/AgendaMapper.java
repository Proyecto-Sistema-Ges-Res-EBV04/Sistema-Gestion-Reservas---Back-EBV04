package com.reservas.reservas_backend.presentacion;

import com.reservas.reservas_backend.dominio.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/** Construye los objetos de respuesta y convierte los request en entidades. */
@Component
public class AgendaMapper {

    // ---------- Respuestas ----------
    public HorarioDisponibleDto aHorario(LocalDate fecha, LocalTime inicio, long minutos,
                                         Long idAgenda, Long idDiaExcepcion) {
        return new HorarioDisponibleDto(fecha, inicio, inicio.plusMinutes(minutos),
                (int) minutos, idAgenda, idDiaExcepcion);
    }

    public ResultadoCitaDto exito(Long idDetalleAgenda, String mensaje) {
        return new ResultadoCitaDto(true, mensaje, idDetalleAgenda);
    }

    public ResultadoCitaDto error(String mensaje) {
        return new ResultadoCitaDto(false, mensaje, null);
    }

    // ---------- Request -> entidad ----------
    public Agenda aAgenda(ConfigurarAgendaRequest req, Long idDia) {
        Agenda a = new Agenda();
        aplicar(a, req, idDia);
        return a;
    }

    public void aplicar(Agenda a, ConfigurarAgendaRequest req, Long idDia) {
        a.setIddia(idDia);
        a.setIdsedeservicio(req.idSedeServicio());
        a.setHorainicio(req.horaInicio());
        a.setHorafin(req.horaFin());
        a.setRangohora(LocalTime.ofSecondOfDay(req.rangoHora() * 60L));
        a.setActivo(!Boolean.FALSE.equals(req.activo()));
    }

    public Diaexcepcion aDiaExcepcion(ConfigurarDiaFestivoRequest req) {
        Diaexcepcion d = new Diaexcepcion();
        aplicar(d, req);
        return d;
    }

    public void aplicar(Diaexcepcion d, ConfigurarDiaFestivoRequest req) {
        d.setIdsedeservicio(req.idSedeServicio());
        d.setFecha(req.fecha());
        d.setHorainicio(req.horaInicio());
        d.setHorafin(req.horaFin());
        d.setRangohora(req.rangoHora().longValue());
        d.setActivo(!Boolean.FALSE.equals(req.activo()));
    }

    public Detalleagenda aDetalle(ModificarCitaRequest req, LocalDateTime ahora) {
        Detalleagenda d = new Detalleagenda();
        boolean porAgenda = req.idAgenda() != null && req.idAgenda() != 0;
        d.setIdagenda(porAgenda ? req.idAgenda() : null);
        d.setIddiaexcepcion(porAgenda ? null : req.idDiaExcepcion());
        d.setIdiniciosesion(req.idIniciosesion());
        d.setFecha(req.fecha());
        d.setHora(req.hora());
        d.setDescripcion(req.descripcion());
        d.setIdestadocita(req.estadoCita() != null ? req.estadoCita() : 2L);
        d.setFechacreacion(ahora);
        d.setFechamodificacion(ahora);
        return d;
    }
}
