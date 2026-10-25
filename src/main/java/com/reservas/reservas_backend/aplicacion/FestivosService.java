package com.reservas.reservas_backend.aplicacion;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Valida festivos con la librería Jollyday. */
@Slf4j
@Service
public class FestivosService {

    @Value("${agenda.festivos.pais:co}")
    private String paisPorDefecto;

    /**
     * País de la sede de un servicio.
     * Por ahora devuelve el país por defecto; cuando se conozca la cadena
     * sedeservicio -> sede -> municipio -> departamento -> pais, se resuelve aquí.
     */
    public String paisDeSedeServicio(Long idSedeServicio) {
        return paisPorDefecto;
    }

    public boolean esFestivo(String codigoPais, LocalDate fecha) {
        try {
            HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(codigoPais));
            return manager.isHoliday(fecha);
        } catch (RuntimeException e) {
            log.error("No se pudo consultar los festivos del país '{}': {}", codigoPais, e.getMessage());
            return false;
        }
    }
}
