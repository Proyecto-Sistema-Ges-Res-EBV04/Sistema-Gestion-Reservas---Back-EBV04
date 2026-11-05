package com.reservas.reservas_backend.aplicacion;

import com.reservas.reservas_backend.dominio.Sede;
import com.reservas.reservas_backend.infraestructura.BusquedaEmpresaRepository;
import com.reservas.reservas_backend.integracion.BusquedaEmpresasResponse;
import com.reservas.reservas_backend.integracion.BusquedaEmpresasResponse.EmpresaEncontrada;
import com.reservas.reservas_backend.integracion.BusquedaEmpresasResponse.SedeEncontrada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

// HU-18: Buscar empresas (segunda vía de descubrimiento; reutiliza el enfoque de HU-19).
@Service
public class BusquedaEmpresaService {

    private final BusquedaEmpresaRepository busquedaRepository;

    @Autowired
    public BusquedaEmpresaService(BusquedaEmpresaRepository busquedaRepository) {
        this.busquedaRepository = busquedaRepository;
    }

    @Transactional(readOnly = true)
    public BusquedaEmpresasResponse buscar(String nombre, String municipio) {

        List<Sede> filas = busquedaRepository.buscar(patron(nombre), patron(municipio));

        // Una fila por sede -> se agrupa por Empresa (RN4)
        Map<Integer, List<Sede>> porEmpresa = filas.stream()
                .collect(Collectors.groupingBy(
                        sd -> sd.getEmpresa().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<EmpresaEncontrada> empresas = porEmpresa.values().stream()
                .map(grupo -> new EmpresaEncontrada(
                        grupo.get(0).getEmpresa().getId(),
                        grupo.get(0).getEmpresa().getNombre(),
                        grupo.stream()
                                .map(sd -> new SedeEncontrada(
                                        sd.getId(),
                                        sd.getNombre(),
                                        sd.getDireccion(),
                                        sd.getMunicipio(),
                                        sd.getPais()))
                                .toList()))
                .toList();

        // RN6: sin coincidencias, se informa
        String mensaje = empresas.isEmpty() ? "No se encontraron Empresas" : null;

        return new BusquedaEmpresasResponse(mensaje, empresas.size(), empresas);
    }

    private String patron(String texto) {
        if (texto == null || texto.isBlank()) {
            return "%";
        }
        return "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
