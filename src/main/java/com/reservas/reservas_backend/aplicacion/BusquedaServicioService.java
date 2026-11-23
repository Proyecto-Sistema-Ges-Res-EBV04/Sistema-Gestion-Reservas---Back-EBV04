package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Servicio;
import com.reservas.reservas_backend.dominio.ServicioSede;
import com.reservas.reservas_backend.dominio.UnidadDuracion;
import com.reservas.reservas_backend.infraestructura.BusquedaServicioRepository;
import com.reservas.reservas_backend.infraestructura.CapacidadAtencionRepository;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse.SedeEncontrada;
import com.reservas.reservas_backend.integracion.BusquedaServiciosResponse.ServicioEncontrado;
import com.reservas.reservas_backend.integracion.DetalleServicioResponse;
import com.reservas.reservas_backend.integracion.DetalleServicioResponse.EmpresaPrestadora;
import com.reservas.reservas_backend.integracion.DetalleServicioResponse.SedeOferta;
import com.reservas.reservas_backend.integracion.ServicioNoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BusquedaServicioService {

    private final BusquedaServicioRepository busquedaRepository;
    private final CapacidadAtencionRepository capacidadRepository;

    @Autowired
    public BusquedaServicioService(BusquedaServicioRepository busquedaRepository,
                                   CapacidadAtencionRepository capacidadRepository) {
        this.busquedaRepository = busquedaRepository;
        this.capacidadRepository = capacidadRepository;
    }

    @Transactional(readOnly = true)
    public BusquedaServiciosResponse buscar(String palabra, Integer idEmpresa,
                                            Integer idSede, String municipio, String pais) {

        List<ServicioSede> filas = busquedaRepository.buscar(
                patron(palabra),
                idEmpresa == null ? 0 : idEmpresa,
                idSede == null ? 0 : idSede,
                patron(municipio),
                codigoPais(pais));

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
                                    ss.getSede().getMunicipio(),
                                    ss.getSede().getPais()))
                            .toList();
                    return new ServicioEncontrado(
                            s.getId(),
                            s.getNombre(),
                            s.getDescripcion(),
                            s.getDuracion(),
                            s.getUnidadDuracion(),
                            duracionEnMinutos(s),
                            s.getEmpresa().getId(),
                            s.getEmpresa().getNombre(),
                            sedes);
                })
                .toList();

        // RN6: sin coincidencias, se informa
        String mensaje = servicios.isEmpty() ? "No se encontraron resultados" : null;

        return new BusquedaServiciosResponse(mensaje, servicios.size(), servicios);
    }

    // HU-20: ficha pública de un servicio identificado desde la búsqueda (RN1).
    // Solo lectura: no crea ni confirma citas (RN6).
    @Transactional(readOnly = true)
    public DetalleServicioResponse consultarDetalle(Integer idServicio) {
        List<ServicioSede> oferta = busquedaRepository.ofertaPublica(idServicio);

        // RN1: si el servicio no hace parte de la oferta pública (no existe, sin sedes activas
        // o Empresa con registro incompleto) no hay ficha que mostrar.
        if (oferta.isEmpty()) {
            throw new ServicioNoEncontradoException(
                    "El servicio " + idServicio + " no existe o no está disponible en la oferta pública");
        }

        // RN4: una sede es reservable si tiene al menos una franja con capacidad configurada (HU-14/HU-16).
        Set<Integer> sedesReservables = capacidadRepository.sedesConCapacidad(idServicio);

        List<SedeOferta> sedes = oferta.stream()
                .map(ServicioSede::getSede)
                .map(sd -> new SedeOferta(
                        sd.getId(),
                        sd.getNombre(),
                        sd.getDireccion(),
                        sd.getMunicipio(),
                        sd.getTelefono(),
                        sedesReservables.contains(sd.getId())))
                .toList();

        boolean reservable = sedes.stream().anyMatch(SedeOferta::reservable);
        String mensaje = reservable ? null : "Este servicio aún no está disponible para agendar";

        Servicio s = oferta.get(0).getServicio();
        return new DetalleServicioResponse(
                s.getId(),
                s.getNombre(),
                s.getDescripcion(),
                s.getDuracion(),
                s.getUnidadDuracion().name(),
                new EmpresaPrestadora(s.getEmpresa().getId(), s.getEmpresa().getNombre()),
                reservable,
                mensaje,
                sedes);
    }

    private Integer duracionEnMinutos(Servicio servicio) {
        if (servicio.getUnidadDuracion() == UnidadDuracion.HORAS) {
            return servicio.getDuracion() * 60;
        }
        return servicio.getDuracion();
    }

    private String codigoPais(String pais) {
        if (pais == null || pais.isBlank()) {
            return "";
        }
        return pais.trim().toUpperCase(Locale.ROOT);
    }

    private String patron(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        return "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
