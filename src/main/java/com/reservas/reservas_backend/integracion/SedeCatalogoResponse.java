package com.reservas.reservas_backend.integracion;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class SedeCatalogoResponse {
    private Integer id;
    private String nombre;
    private String direccion;
    private boolean activa;
    private boolean configurada;
    private List<String> pendientes;   // qué falta configurar (vacía si está lista)
}