package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.ServicioSede;
import com.reservas.reservas_backend.infraestructura.BusquedaServicioRepository;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse.SedeEncontrada;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse.ServicioEncontrado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusquedaServicioService {

    private final BusquedaServicioRepository busquedaRepository;

    @Autowired
    public BusquedaServicioService(BusquedaServicioRepository busquedaRepository) {
        this.busquedaRepository = busquedaRepository;
    }

    @Transactional(readOnly = true)
    public BusquedaServiciosResponse buscar(String palabra, Integer idEmpresa,
                                            Integer idSede, String municipio) {

        List<ServicioSede> filas = busquedaRepository.buscar(
                patron(palabra),
                idEmpresa == null ? 0 : idEmpresa,
                idSede == null ? 0 : idSede,
                patron(municipio));

        // Una fila por servicio-sede -> se agrupa por servicio (RN4)
        Map<Integer, List<ServicioSede>> porServicio = filas.stream()
                .collect(Collectors.groupingBy(
                        ss -> ss.getServicio().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<ServicioEncontrado> servicios = porServicio.values().stream()
                .map(grupo -> {
                    Servicio s = grupo.get(0).getServicio();
                    List<SedeEncontrada> sedes = grupo.stream()
                            .map(ss -> new SedeEncontrada(
                                    ss.getSede().getId(),
                                    ss.getSede().getNombre(),
                                    ss.getSede().getDireccion(),
                                    ss.getSede().getMunicipio()))
                            .toList();
                    return new ServicioEncontrado(
                            s.getId(),
                            s.getNombre(),
                            s.getDescripcion(),
                            s.getEmpresa().getId(),
                            s.getEmpresa().getNombre(),
                            sedes);
                })
                .toList();

        // RN6: sin coincidencias, se informa
        String mensaje = servicios.isEmpty() ? "No se encontraron resultados" : null;

        return new BusquedaServiciosResponse(mensaje, servicios.size(), servicios);
    }

    private String patron(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        return "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
